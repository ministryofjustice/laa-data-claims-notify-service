package uk.gov.justice.laa.dstew.payments.notify.config.rest;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the Data Stewardship Claims API.
 *
 * @author Jamie Briggs
 */
@ConfigurationProperties(prefix = "app.claims-api")
public final class ClaimsApiProperties extends ApiProperties {

  public ClaimsApiProperties(String url, String accessToken) {
    super(url, accessToken);
  }
}
