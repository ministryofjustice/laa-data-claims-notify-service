package uk.gov.justice.laa.dstew.payments.notify.client;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import uk.gov.justice.laa.dstew.payments.notify.helper.MockServerIntegrationTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.cloud.aws.sqs.enabled=false", // Disable AWS SQS functionality
    })
@AutoConfigureMockMvc(addFilters = false)
//@Import(WebMvcTestConfig.class)
public class DataClaimsRestClientIntegrationTest extends MockServerIntegrationTest {

  protected DataClaimsRestClient dataClaimsRestClient;

  @BeforeEach
  void setUp() {
    dataClaimsRestClient = createClient(DataClaimsRestClient.class);
  }

  @Test
  @DisplayName("Example test for DataClaimsRestClient")
  void exampleTest() {
    assertThat(true).isTrue();
  }
}
