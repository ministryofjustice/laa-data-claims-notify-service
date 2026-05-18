package uk.gov.justice.laa.dstew.payments.notify.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockserver.matchers.Times.unlimited;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;

@SpringBootTest(
    properties = {"spring.cloud.aws.sqs.enabled=false", "app.claims-api.access-token=test-token"})
@DisplayName("DataClaimsRestClient (integration)")
class DataClaimsRestClientIntegrationTest {

  private static final UUID SUBMISSION_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

  private static final DockerImageName MOCKSERVER_IMAGE =
      DockerImageName.parse("mockserver/mockserver")
          .withTag("mockserver-" + MockServerClient.class.getPackage().getImplementationVersion());

  private static final MockServerContainer MOCK_SERVER =
      new MockServerContainer(MOCKSERVER_IMAGE)
          .waitingFor(
              Wait.forHttp("/mockserver/status")
                  .withMethod("PUT")
                  .forStatusCode(200)
                  .withStartupTimeout(Duration.ofSeconds(60)));

  static {
    MOCK_SERVER.start();
  }

  private static MockServerClient mockServerClient;

  @DynamicPropertySource
  static void claimsApiProps(DynamicPropertyRegistry registry) {
    registry.add("app.claims-api.url", MOCK_SERVER::getEndpoint);
  }

  @Autowired private DataClaimsRestClient client;

  @BeforeAll
  static void initMockServerClient() {
    mockServerClient = new MockServerClient(MOCK_SERVER.getHost(), MOCK_SERVER.getServerPort());
  }

  @AfterEach
  void resetExpectations() {
    mockServerClient.reset();
  }

  @Test
  @DisplayName("200 returns the parsed submission and sends the auth header")
  void returnsSubmissionOn200() {
    mockServerClient
        .when(
            request()
                .withMethod("GET")
                .withPath("/api/v1/submissions/" + SUBMISSION_ID)
                .withHeader("Authorization", "test-token"),
            unlimited())
        .respond(
            response()
                .withStatusCode(200)
                .withHeader("Content-Type", "application/json")
                .withBody(
                    "{"
                        + "\"submission_id\":\""
                        + SUBMISSION_ID
                        + "\","
                        + "\"office_account_number\":\"OFFICE-1\","
                        + "\"submission_period\":\"JUL-2025\","
                        + "\"area_of_law\":\"LEGAL HELP\","
                        + "\"status\":\"VALIDATION_SUCCEEDED\""
                        + "}"));

    SubmissionResponse response = client.getSubmission(SUBMISSION_ID);

    assertThat(response.getSubmissionId()).isEqualTo(SUBMISSION_ID);
    assertThat(response.getOfficeAccountNumber()).isEqualTo("OFFICE-1");
    assertThat(response.getSubmissionPeriod()).isEqualTo("JUL-2025");
    assertThat(response.getAreaOfLaw()).isEqualTo(AreaOfLaw.LEGAL_HELP);
    assertThat(response.getStatus()).isEqualTo(SubmissionStatus.VALIDATION_SUCCEEDED);
  }

  @Test
  @DisplayName("404 bubbles as WebClientResponseException so SQS can retry / DLQ")
  void bubblesNotFoundError() {
    mockServerClient
        .when(request().withMethod("GET").withPath("/api/v1/submissions/" + SUBMISSION_ID))
        .respond(response().withStatusCode(404));

    assertThatThrownBy(() -> client.getSubmission(SUBMISSION_ID))
        .isInstanceOf(WebClientResponseException.NotFound.class);
  }

  @Test
  @DisplayName("500 bubbles as WebClientResponseException so SQS can retry / DLQ")
  void bubblesServerError() {
    mockServerClient
        .when(request().withMethod("GET").withPath("/api/v1/submissions/" + SUBMISSION_ID))
        .respond(response().withStatusCode(500));

    assertThatThrownBy(() -> client.getSubmission(SUBMISSION_ID))
        .isInstanceOf(WebClientResponseException.class);
  }
}
