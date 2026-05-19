package uk.gov.justice.laa.dstew.payments.notify.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.dstew.payments.notify.model.event.SubmissionEvent;

@DisplayName("NotifyQueueListener")
class NotifyQueueListenerTest {

  private static final UUID VALID_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");

  private final NotifyQueueListener listener = new NotifyQueueListener();

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
    void acceptsValidUuid() {
      assertThatCode(() -> listener.receiveNotifyEvent(new SubmissionEvent(VALID_UUID)))
          .doesNotThrowAnyException();
    }

    @Test
    void swallowsMissingSubmissionIdWithoutThrowing() {
      assertThatCode(() -> listener.receiveNotifyEvent(new SubmissionEvent(null)))
          .doesNotThrowAnyException();
    }

    @Test
    void swallowsNullEventWithoutThrowing() {
      assertThatCode(() -> listener.receiveNotifyEvent(null)).doesNotThrowAnyException();
    }
  }
}
