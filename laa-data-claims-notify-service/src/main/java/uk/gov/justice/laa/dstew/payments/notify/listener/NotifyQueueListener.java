package uk.gov.justice.laa.dstew.payments.notify.listener;

import static java.util.Objects.isNull;

import io.awspring.cloud.sqs.annotation.SqsListener;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.dstew.payments.notify.model.event.SubmissionEvent;

/**
 * Listener for messages which are sent to the Notify queue.
 *
 * <p>The SNS subscription that feeds this queue has a filter policy on the {@code
 * SubmissionEventType} attribute, so only {@code SUBMISSION_VALIDATION_SUCCEEDED} events are
 * delivered. This consumer trusts that filter and only validates the body.
 *
 * <p>Payloads missing a submission UUID are acknowledged without downstream action — redelivery
 * would not change the outcome. Jackson parse failures (including malformed UUIDs) bubble up so
 * Spring Cloud AWS can apply queue retry / DLQ policy.
 */
@Slf4j
@Component
public class NotifyQueueListener {

  @SqsListener("${app.sqs.notify-queue-name}")
  public void receiveNotifyEvent(SubmissionEvent event) {
    parseSubmissionId(event)
        .ifPresent(id -> log.info("Received notify event for submission_id={}", id));
  }

  static Optional<UUID> parseSubmissionId(SubmissionEvent event) {
    if (isNull(event)) {
      log.error("Discarding notify event: payload is null");
      return Optional.empty();
    }
    UUID submissionId = event.submissionId();
    if (isNull(submissionId)) {
      log.error("Discarding notify event: submission_id is missing");
      return Optional.empty();
    }
    return Optional.of(submissionId);
  }
}
