package uk.gov.justice.laa.dstew.payments.notify.listener;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.eventstream.Message;

/**
 * Listener for messages which are sent to the Notify queue.
 *
 * <p>Messages arrive in this queue as follows:
 *
 * <ol>
 *   <li>An upstream service publishes a message to an SNS topic.
 *   <li>The topic is subscribed to a queue filtering for {@code SUBMISSION_VALIDATION_SUCCEEDED}
 *       messages.
 *   <li>{@link NotifyQueueListener} processes the message.
 * </ol>
 *
 * @author Jamie Briggs
 */
@Slf4j
@Component
public class NotifyQueueListener {

  @SqsListener("${app.sqs.notify-queue-name}")
  public void receiveNotifyEvent(Message message) {
    log.debug("Received message: {}", message);
  }
}
