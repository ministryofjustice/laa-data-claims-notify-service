package uk.gov.justice.laa.dstew.payments.notify.model.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public record SubmissionEvent(@JsonProperty("submission_id") UUID submissionId) {}
