package uk.gov.justice.laa.dstew.payments.notify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LaaDataClaimsNotifyServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(LaaDataClaimsNotifyServiceApplication.class, args);
  }
}
