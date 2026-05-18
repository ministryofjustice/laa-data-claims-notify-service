package uk.gov.justice.laa.dstew.payments.notify.config.rest;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Bound from {@code app.claims-api.*} in {@code application.yml}. */
@ConfigurationProperties(prefix = "app.claims-api")
public record ClaimsApiProperties(String url, String accessToken) {}
