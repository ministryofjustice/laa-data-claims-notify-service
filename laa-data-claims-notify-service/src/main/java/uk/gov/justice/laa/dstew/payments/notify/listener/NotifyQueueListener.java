package uk.gov.justice.laa.dstew.payments.notify.listener;

import static java.util.Objects.isNull;

import io.awspring.cloud.sqs.annotation.SqsListener;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.dstew.payments.notify.client.DataClaimsRestClient;
import uk.gov.justice.laa.dstew.payments.notify.model.event.SubmissionEvent;
import uk.gov.justice.laa.dstew.payments.notify.service.NotifyEmailService;
import uk.gov.service.notify.NotificationClientException;

/**
 * Listener for messages which are sent to the Notify queue.
 *
 * <p>The SNS subscription that feeds this queue has a filter policy on the {@code
 * SubmissionEventType} attribute, so only {@code SUBMISSION_VALIDATION_SUCCEEDED} events are
 * delivered. This consumer trusts that filter and only validates the body.
 *
 * <p>Payloads missing a submission UUID are acknowledged without downstream action — redelivery
 * would not change the outcome. All claims-api failures (404, 5xx, timeout), Jackson parse
 * failures, and {@link NotificationClientException}s bubble so Spring Cloud AWS applies the queue
 * retry / DLQ policy.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotifyQueueListener {

  private final DataClaimsRestClient claimsClient;
  private final NotifyEmailService notifyEmailService;

  @SqsListener("${app.sqs.notify-queue-name}")
  public void receiveNotifyEvent(SubmissionEvent event) throws NotificationClientException {
    Optional<UUID> submissionId = parseSubmissionId(event);
    if (submissionId.isPresent()) {
      enrichAndNotify(submissionId.get());
    }
  }

  private void enrichAndNotify(UUID submissionId) throws NotificationClientException {
    SubmissionResponse response = claimsClient.getSubmission(submissionId);
    log.info(
        "Enriched notify event: submission_id={} office={} period={} area_of_law={} status={}",
        response.getSubmissionId(),
        response.getOfficeAccountNumber(),
        response.getSubmissionPeriod(),
        response.getAreaOfLaw(),
        response.getStatus());
    notifyEmailService.sendValidationSuccessEmail(response);
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
