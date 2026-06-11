package uk.gov.justice.laa.dstew.payments.notify.service;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.dstew.payments.notify.config.notify.GovNotifyProperties;
import uk.gov.justice.laa.dstew.payments.notify.mapper.NotifyPersonalisationMapper;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotifyEmailService {

  private final NotificationClient notificationClient;
  private final NotifyPersonalisationMapper personalisationMapper;
  private final GovNotifyProperties govNotifyProperties;

  public void sendValidationSuccessEmail(SubmissionResponse submission)
      throws NotificationClientException {
    String recipient = submission.getProviderUserId();
    Map<String, Object> personalisation = personalisationMapper.toPersonalisation(submission);

    SendEmailResponse response =
        notificationClient.sendEmail(
            govNotifyProperties.successfulSubmissionTemplateId().toString(),
            recipient,
            personalisation,
            String.valueOf(submission.getSubmissionId()));

    log.info(
        "Sent GOV Notify email: submission_id={} notify_response_id={} template_id={}",
        submission.getSubmissionId(),
        response.getNotificationId(),
        response.getTemplateId());
  }
}
