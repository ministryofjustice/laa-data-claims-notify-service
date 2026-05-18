package uk.gov.justice.laa.dstew.payments.notify.config;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
public class SqsConfig {

  @Bean
  @Profile("local")
  public SqsClient sqsClientLocal(
      @Value("${spring.cloud.aws.region.static}") String region,
      @Value("${spring.cloud.aws.credentials.access-key}") String accessKey,
      @Value("${spring.cloud.aws.credentials.secret-key}") String secretKey,
      @Value("${spring.cloud.aws.endpoint}") String endpoint) {

    return SqsClient.builder()
        .region(Region.of(region))
        .credentialsProvider(
            StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
        .endpointOverride(URI.create(endpoint))
        .build();
  }

  @Bean
  @Profile("!local")
  public SqsClient sqsClient(@Value("${spring.cloud.aws.region.static}") String region) {
    return SqsClient.builder()
        .region(Region.of(region))
        .credentialsProvider(DefaultCredentialsProvider.builder().build())
        .build();
  }
}
