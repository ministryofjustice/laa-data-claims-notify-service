package uk.gov.justice.laa.dstew.payments.notify.model.event;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Body of a notify-queue message. Matches the {@code SubmissionValidationMessage} payload published
 * by {@code laa-data-claims-api}. Event-type filter is handled by the SNS subscription filter
 * policy on {@code SubmissionEventType} (see cloud-platform-environments).
 */
public record SubmissionEvent(@JsonProperty("submission_id") String submissionId) {}
