package uk.gov.justice.laa.dstew.payments.notify.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.dstew.payments.notify.client.DataClaimsRestClient;
import uk.gov.justice.laa.dstew.payments.notify.model.event.SubmissionEvent;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.SendEmailResponse;

/**
 * End-to-end test: a message published to a LocalStack SQS queue is picked up by the {@link
 * NotifyQueueListener} via Spring Cloud AWS autoconfig and bound to {@link SubmissionEvent}.
 */
@SpringBootTest
@DisplayName("NotifyQueueListener (integration)")
class NotifyQueueListenerIntegrationTest {

  private static final String QUEUE_NAME = "notify-queue-it";
  private static final UUID VALID_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final String EXPECTED_TEMPLATE_ID = "00000000-0000-0000-0000-000000000000";
  private static final String EXPECTED_RECIPIENT = "provider.user@example.com";
  private static final String EXPECTED_OFFICE = "OFFICE-1";
  private static final String EXPECTED_PERIOD = "JUL-2025";

  private static final LocalStackContainer LOCALSTACK =
      new LocalStackContainer(DockerImageName.parse("localstack/localstack:4.14"))
          .withServices(SQS);

  private static SqsClient sqsClient;
  private static String queueUrl;

  static {
    if (DockerClientFactory.instance().isDockerAvailable()) {
      LOCALSTACK.start();
      sqsClient =
          SqsClient.builder()
              .endpointOverride(LOCALSTACK.getEndpointOverride(SQS))
              .region(Region.of(LOCALSTACK.getRegion()))
              .credentialsProvider(
                  StaticCredentialsProvider.create(
                      AwsBasicCredentials.create(
                          LOCALSTACK.getAccessKey(), LOCALSTACK.getSecretKey())))
              .build();
      queueUrl = sqsClient.createQueue(b -> b.queueName(QUEUE_NAME)).queueUrl();
    }
  }

  @DynamicPropertySource
  static void awsProps(DynamicPropertyRegistry registry) {
    registry.add("spring.cloud.aws.endpoint", () -> LOCALSTACK.getEndpoint().toString());
    registry.add("spring.cloud.aws.region.static", LOCALSTACK::getRegion);
    registry.add("spring.cloud.aws.credentials.access-key", LOCALSTACK::getAccessKey);
    registry.add("spring.cloud.aws.credentials.secret-key", LOCALSTACK::getSecretKey);
    registry.add("app.sqs.notify-queue-name", () -> QUEUE_NAME);
  }

  @MockitoSpyBean private NotifyQueueListener listener;
  @MockitoBean private DataClaimsRestClient claimsClient;
  @MockitoBean private NotificationClient notificationClient;

  @BeforeAll
  static void requireDocker() {
    Assumptions.assumeTrue(
        DockerClientFactory.instance().isDockerAvailable(),
        "Docker is not available, skipping the tests.");
  }

  @BeforeEach
  void stubCollaborators() throws Exception {
    when(claimsClient.getSubmission(any()))
        .thenReturn(
            new SubmissionResponse()
                .submissionId(VALID_UUID)
                .officeAccountNumber(EXPECTED_OFFICE)
                .submissionPeriod(EXPECTED_PERIOD)
                .areaOfLaw(AreaOfLaw.LEGAL_HELP)
                .providerUserId(EXPECTED_RECIPIENT)
                .numberOfClaims(2));
    SendEmailResponse sendResponse = mock(SendEmailResponse.class);
    when(notificationClient.sendEmail(anyString(), any(), anyMap(), anyString()))
        .thenReturn(sendResponse);
  }

  @AfterEach
  void resetSpy() {
    Mockito.reset(listener);
  }

  @Test
  @DisplayName("a valid submission_id payload is delivered and triggers a Notify send")
  void deliversValidSubmissionEventToListener() throws Exception {
    sqsClient.sendMessage(
        b -> b.queueUrl(queueUrl).messageBody("{\"submission_id\":\"" + VALID_UUID + "\"}"));

    ArgumentCaptor<SubmissionEvent> captor = ArgumentCaptor.forClass(SubmissionEvent.class);
    verify(listener, timeout(5000)).receiveNotifyEvent(captor.capture());
    assertThat(captor.getValue().submissionId()).isEqualTo(VALID_UUID);

    Map<String, Object> expectedPersonalisation = new LinkedHashMap<>();
    expectedPersonalisation.put("office_account", EXPECTED_OFFICE);
    expectedPersonalisation.put("submission_period", EXPECTED_PERIOD);
    expectedPersonalisation.put("area_of_law", "Legal help");
    expectedPersonalisation.put("total_claims", "2");
    expectedPersonalisation.put("submission_url", "https://sabc.com/submission/" + VALID_UUID);

    verify(notificationClient, timeout(5000))
        .sendEmail(
            eq(EXPECTED_TEMPLATE_ID),
            eq(EXPECTED_RECIPIENT),
            eq(expectedPersonalisation),
            eq(VALID_UUID.toString()));
  }
}
