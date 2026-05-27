package uk.gov.justice.laa.dstew.payments.notify.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;
import uk.gov.justice.laa.dstew.payments.notify.client.DataClaimsRestClient;
import uk.gov.justice.laa.dstew.payments.notify.model.event.SubmissionEvent;
import uk.gov.justice.laa.dstew.payments.notify.service.NotifyEmailService;
import uk.gov.service.notify.NotificationClientException;

@DisplayName("NotifyQueueListener")
class NotifyQueueListenerTest {

  private static final UUID VALID_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");

  private final DataClaimsRestClient claimsClient = mock(DataClaimsRestClient.class);
  private final NotifyEmailService notifyEmailService = mock(NotifyEmailService.class);
  private final MeterRegistry meterRegistry = new SimpleMeterRegistry();
  private final NotifyQueueListener listener =
      new NotifyQueueListener(claimsClient, notifyEmailService, meterRegistry);

  private double counter(String name) {
    return meterRegistry.get(name).counter().count();
  }

  @Nested
  @DisplayName("parseSubmissionId")
  class ParseSubmissionId {

    @Test
    void returnsUuidForValidEvent() {
      Optional<UUID> result =
          NotifyQueueListener.parseSubmissionId(new SubmissionEvent(VALID_UUID));
      assertThat(result).contains(VALID_UUID);
    }

    @Test
    void returnsEmptyForNullEvent() {
      assertThat(NotifyQueueListener.parseSubmissionId(null)).isEmpty();
    }

    @Test
    void returnsEmptyForNullSubmissionId() {
      assertThat(NotifyQueueListener.parseSubmissionId(new SubmissionEvent(null))).isEmpty();
    }
  }

  @Nested
  @DisplayName("receiveNotifyEvent")
  class ReceiveNotifyEvent {

    @Test
    void fetchesAndSendsEmailForValidUuid() throws NotificationClientException {
      SubmissionResponse response =
          new SubmissionResponse()
              .submissionId(VALID_UUID)
              .officeAccountNumber("OFFICE-1")
              .submissionPeriod("JUL-2025")
              .areaOfLaw(AreaOfLaw.LEGAL_HELP)
              .status(SubmissionStatus.VALIDATION_SUCCEEDED);
      when(claimsClient.getSubmission(VALID_UUID)).thenReturn(response);

      assertThatCode(() -> listener.receiveNotifyEvent(new SubmissionEvent(VALID_UUID)))
          .doesNotThrowAnyException();
      verify(claimsClient).getSubmission(VALID_UUID);
      verify(notifyEmailService).sendValidationSuccessEmail(response);
      assertThat(counter("notify.event.received")).isEqualTo(1.0);
      assertThat(counter("notify.processing.success")).isEqualTo(1.0);
      assertThat(counter("notify.processing.failed")).isEqualTo(0.0);
    }

    @Test
    void bubblesNotFoundSoSqsRedelivers() throws NotificationClientException {
      when(claimsClient.getSubmission(any()))
          .thenThrow(
              WebClientResponseException.create(404, "Not Found", null, new byte[0], null, null));

      assertThatThrownBy(() -> listener.receiveNotifyEvent(new SubmissionEvent(VALID_UUID)))
          .isInstanceOf(WebClientResponseException.class);
      verify(notifyEmailService, never()).sendValidationSuccessEmail(any());
      assertThat(counter("notify.event.received")).isEqualTo(1.0);
      assertThat(counter("notify.processing.failed")).isEqualTo(1.0);
      assertThat(counter("notify.processing.success")).isEqualTo(0.0);
    }

    @Test
    void rethrowsServerErrorSoSqsRedelivers() throws NotificationClientException {
      when(claimsClient.getSubmission(any()))
          .thenThrow(
              WebClientResponseException.create(
                  500, "Server Error", null, new byte[0], null, null));

      assertThatThrownBy(() -> listener.receiveNotifyEvent(new SubmissionEvent(VALID_UUID)))
          .isInstanceOf(WebClientResponseException.class);
      verify(notifyEmailService, never()).sendValidationSuccessEmail(any());
      assertThat(counter("notify.processing.failed")).isEqualTo(1.0);
    }

    @Test
    void bubblesNotificationClientExceptionSoSqsRedelivers() throws NotificationClientException {
      SubmissionResponse response = new SubmissionResponse().submissionId(VALID_UUID);
      when(claimsClient.getSubmission(VALID_UUID)).thenReturn(response);
      doThrow(new NotificationClientException("boom"))
          .when(notifyEmailService)
          .sendValidationSuccessEmail(response);

      assertThatThrownBy(() -> listener.receiveNotifyEvent(new SubmissionEvent(VALID_UUID)))
          .isInstanceOf(NotificationClientException.class);
      assertThat(counter("notify.processing.failed")).isEqualTo(1.0);
      assertThat(counter("notify.processing.success")).isEqualTo(0.0);
    }

    @Test
    void skipsEnrichmentForMissingSubmissionId() throws NotificationClientException {
      assertThatCode(() -> listener.receiveNotifyEvent(new SubmissionEvent(null)))
          .doesNotThrowAnyException();
      verify(claimsClient, never()).getSubmission(any());
      verify(notifyEmailService, never()).sendValidationSuccessEmail(any());
      assertThat(counter("notify.event.received")).isEqualTo(1.0);
      assertThat(counter("notify.processing.success")).isEqualTo(0.0);
      assertThat(counter("notify.processing.failed")).isEqualTo(0.0);
    }

    @Test
    void skipsEnrichmentForNullEvent() throws NotificationClientException {
      assertThatCode(() -> listener.receiveNotifyEvent(null)).doesNotThrowAnyException();
      verify(claimsClient, never()).getSubmission(any());
      verify(notifyEmailService, never()).sendValidationSuccessEmail(any());
      assertThat(counter("notify.event.received")).isEqualTo(1.0);
    }
  }
}
