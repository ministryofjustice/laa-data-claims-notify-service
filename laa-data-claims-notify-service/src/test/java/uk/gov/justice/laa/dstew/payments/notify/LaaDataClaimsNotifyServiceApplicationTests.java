package uk.gov.justice.laa.dstew.payments.notify;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    properties = {
      "spring.cloud.aws.sqs.enabled=false", // Disable AWS SQS functionality
      "spring.cloud.aws.region.static=eu-west-2"
    })
class LaaDataClaimsNotifyServiceApplicationTests {

  @Test
  void contextLoads() {
    // empty due to only testing context load
  }
}
