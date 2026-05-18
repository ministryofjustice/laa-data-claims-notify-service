package uk.gov.justice.laa.dstew.payments.notify.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;

import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import uk.gov.justice.laa.dstew.payments.notify.model.event.SubmissionEvent;

/**
 * End-to-end test: a message published to a LocalStack SQS queue is picked up by the {@link
 * NotifyQueueListener} via Spring Cloud AWS autoconfig and bound to {@link SubmissionEvent}.
 */
@SpringBootTest
@DisplayName("NotifyQueueListener (integration)")
class NotifyQueueListenerIntegrationTest {

  private static final String QUEUE_NAME = "notify-queue-it";
  private static final UUID VALID_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");

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

  @BeforeAll
  static void requireDocker() {
    Assumptions.assumeTrue(
        DockerClientFactory.instance().isDockerAvailable(),
        "Docker is not available, skipping the tests.");
  }

  @AfterEach
  void resetSpy() {
    Mockito.reset(listener);
  }

  @Test
  @DisplayName("a valid submission_id payload is delivered and parsed into a SubmissionEvent")
  void deliversValidSubmissionEventToListener() {
    sqsClient.sendMessage(
        b -> b.queueUrl(queueUrl).messageBody("{\"submission_id\":\"" + VALID_UUID + "\"}"));

    ArgumentCaptor<SubmissionEvent> captor = ArgumentCaptor.forClass(SubmissionEvent.class);
    verify(listener, timeout(5000)).receiveNotifyEvent(captor.capture());
    assertThat(captor.getValue().submissionId()).isEqualTo(VALID_UUID);
  }
}
