package uk.gov.justice.laa.dstew.payments.notify.config.rest;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.codec.ClientCodecConfigurer;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.support.WebClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.HttpServiceGroup;
import org.springframework.web.service.registry.ImportHttpServices;
import uk.gov.justice.laa.dstew.payments.notify.client.DataClaimsRestClient;

/**
 * Configuration class for setting up the REST client for interacting with the Claims API.
 *
 * @author Jamie Briggs
 */
@Configuration
@EnableConfigurationProperties({ClaimsApiProperties.class})
@ImportHttpServices(
    types = {
      DataClaimsRestClient.class,
    },
    clientType = HttpServiceGroup.ClientType.WEB_CLIENT)
public class WebClientConfiguration {

  /**
   * Configures a WebClient-based HTTP service group by applying common settings such as exchange
   * strategies, base URL, and default authorization header.
   *
   * @param properties the configuration properties containing the base URL and authorization token
   *     for the Claims API
   * @return a {@link WebClientHttpServiceGroupConfigurer} that applies the specified configuration
   *     settings to each client in the service group
   */
  @Bean
  public WebClientHttpServiceGroupConfigurer groupConfigurer(final ClaimsApiProperties properties) {
    return groups ->
        groups.forEachClient(
            (spec, webClientBuilder) -> {
              webClientBuilder.exchangeStrategies(
                  ExchangeStrategies.builder()
                      .codecs(ClientCodecConfigurer::defaultCodecs)
                      .build());
              webClientBuilder.baseUrl(properties.getUrl());
              webClientBuilder.defaultHeader(
                  HttpHeaders.AUTHORIZATION, properties.getAccessToken());
            });
  }
}
