package uk.gov.justice.laa.dstew.payments.notify.config.rest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.codec.ClientCodecConfigurer;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.support.WebClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.HttpServiceGroup;
import org.springframework.web.service.registry.ImportHttpServices;
import uk.gov.justice.laa.dstew.payments.notify.client.DataClaimsRestClient;

@Configuration
@ImportHttpServices(
    types = {
      DataClaimsRestClient.class,
    },
    clientType = HttpServiceGroup.ClientType.WEB_CLIENT)
public class WebClientConfiguration {

  @Bean
  public WebClientHttpServiceGroupConfigurer groupConfigurer(final ClaimsApiProperties properties) {
    return groups ->
        groups.forEachClient(
            (spec, webClientBuilder) -> {
              webClientBuilder.exchangeStrategies(
                  ExchangeStrategies.builder()
                      .codecs(ClientCodecConfigurer::defaultCodecs)
                      .build());
              webClientBuilder.baseUrl(properties.url());
              webClientBuilder.defaultHeader(HttpHeaders.AUTHORIZATION, properties.accessToken());
            });
  }
}
