package uk.gov.justice.laa.dstew.payments.notify.config.notify;

import java.util.UUID;
import lombok.Getter;

@Getter
public enum GovNotifyTemplate {
  EXAMPLE_EMAIL(UUID.fromString("00000000-0000-0000-0000-000000000000")),
  EXAMPLE_EMAIL_TWO(UUID.fromString("00000000-0000-0000-0000-000000000001"));

  private final UUID templateId;

  GovNotifyTemplate(UUID templateId) {
    this.templateId = templateId;
  }
}
