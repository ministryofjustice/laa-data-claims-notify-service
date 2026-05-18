package uk.gov.justice.laa.dstew.payments.notify.config.notify;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.service.notify.NotificationClient;

@Configuration
public class GovNotifyConfig {

  @Bean
  public NotificationClient notificationClient(GovNotifyProperties govNotifyProperties) {
    return new NotificationClient(govNotifyProperties.key());
  }
}
