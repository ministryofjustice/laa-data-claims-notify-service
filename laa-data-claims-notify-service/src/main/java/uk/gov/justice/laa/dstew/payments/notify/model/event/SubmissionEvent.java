package uk.gov.justice.laa.dstew.payments.notify.model.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Body of a notify-queue message. Matches the {@code SubmissionValidationMessage} payload published
 * by {@code laa-data-claims-api}. Event-type filter is handled by the SNS subscription filter
 * policy on {@code SubmissionEventType} (see cloud-platform-environments).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionEvent {

  @JsonProperty("submission_id")
  private String submissionId;
}
