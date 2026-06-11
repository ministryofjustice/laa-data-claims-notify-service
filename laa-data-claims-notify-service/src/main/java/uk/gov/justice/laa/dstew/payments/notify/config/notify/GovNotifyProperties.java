package uk.gov.justice.laa.dstew.payments.notify.config.notify;

import java.util.UUID;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Bound from {@code app.gov-notify.*} in {@code application.yml}. */
@ConfigurationProperties(prefix = "app.gov-notify")
public record GovNotifyProperties(String key, UUID successfulSubmissionTemplateId) {}
