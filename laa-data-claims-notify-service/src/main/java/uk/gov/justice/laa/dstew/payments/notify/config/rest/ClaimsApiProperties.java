package uk.gov.justice.laa.dstew.payments.notify.config.rest;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.claims-api")
public final class ClaimsApiProperties extends ApiProperties {

  public ClaimsApiProperties(String url, String accessToken) {
    super(url, accessToken);
  }
}
