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

/**
 * Configuration for AWS SQS client. The main goal of this configuration is to ensure that the
 * application still works when running in a local environment without trying to autoload
 * configuration for a localstack instance. Uses autoconfiguration to create the SQS client for
 * non-local environments.
 *
 * @author Jamie Briggs
 */
@Configuration
public class SqsConfig {

  /**
   * Uses property set access and secret key for local environments.
   *
   * @param accessKey the AWS access key
   * @param secretKey the AWS secret key
   * @param endpoint the AWS endpoint
   * @return the SQS client
   */
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

  /**
   * Creates and configures an {@link SqsClient} for interacting with AWS Simple Queue Service
   * (SQS). This method sets up the SQS client with a specified AWS region and default credentials
   * provider. The bean is active in non-test and non-wiremock profiles.
   *
   * @param region the AWS region where the SQS service is located, typically specified in the
   *     application configuration
   * @return an instance of {@link SqsClient} configured with the specified AWS region and default
   *     credentials provider
   */
  @Bean
  @Profile("!local")
  public SqsClient sqsClient(@Value("${spring.cloud.aws.region.static}") String region) {
    return SqsClient.builder()
        .region(Region.of(region))
        .credentialsProvider(DefaultCredentialsProvider.builder().build())
        .build();
  }
}
