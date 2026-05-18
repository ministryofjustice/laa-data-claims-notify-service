package uk.gov.justice.laa.dstew.payments.notify.config.notify;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    properties = {
      "spring.cloud.aws.sqs.enabled=false", // Disable AWS SQS functionality
      "app.gov-notify.key=123",
      "app.gov-notify.templates.example-email=acb95826-cb75-447f-9512-52a30c734dc2",
      "app.gov-notify.templates.example-email-two=e34f6b37-c300-41ab-bdaa-1e01c5d50b7a"
    })
@DisplayName("GovNotifyProperties Test")
class GovNotifyPropertiesTest {

  @Autowired private GovNotifyProperties govNotifyProperties;

  @Test
  @DisplayName("Should populate email properties")
  void shouldPopulateEmailProperties() {
    assertThat(govNotifyProperties.getKey()).isEqualTo("123");
    assertThat(govNotifyProperties.getTemplates().get(GovNotifyTemplate.EXAMPLE_EMAIL))
        .isEqualTo("acb95826-cb75-447f-9512-52a30c734dc2");
    assertThat(govNotifyProperties.getTemplates().get(GovNotifyTemplate.EXAMPLE_EMAIL_TWO))
        .isEqualTo("e34f6b37-c300-41ab-bdaa-1e01c5d50b7a");
  }
}
