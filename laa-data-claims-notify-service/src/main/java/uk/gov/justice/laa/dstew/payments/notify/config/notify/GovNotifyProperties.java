package uk.gov.justice.laa.dstew.payments.notify.config.notify;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.gov-notify")
public class GovNotifyProperties {

  private String key;
}
