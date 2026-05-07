package uk.gov.justice.laa.dstew.payments.notify.listener;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.eventstream.Message;

@Slf4j
@Component
public class NotifyQueueListener {

  @SqsListener("${app.sqs.notify-queue-name}")
  public void receiveNotifyEvent(Message message){
    log.debug("Received message: {}", message);
  }
}
