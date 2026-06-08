package uk.gov.justice.laa.dstew.payments.notify.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.dstew.payments.notify.config.notify.GovNotifyProperties;
import uk.gov.justice.laa.dstew.payments.notify.mapper.NotifyPersonalisationMapper;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

@DisplayName("NotifyEmailService")
class NotifyEmailServiceTest {

  private static final UUID SUBMISSION_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final String RECIPIENT = "provider.user@example.com";
  private static final UUID TEMPLATE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final String SABC_URL = "http://localhost:8080";
  private final NotificationClient notificationClient = mock(NotificationClient.class);
  private final NotifyPersonalisationMapper personalisationMapper =
      new NotifyPersonalisationMapper(SABC_URL);
  private final GovNotifyProperties properties = new GovNotifyProperties("test-key", TEMPLATE_ID);
  private final NotifyEmailService service =
      new NotifyEmailService(notificationClient, personalisationMapper, properties);

  @Test
  void sendsEmailWithTemplateRecipientPersonalisationAndReference() throws Exception {
    SubmissionResponse submission =
        new SubmissionResponse()
            .submissionId(SUBMISSION_ID)
            .officeAccountNumber("OFFICE-1")
            .submissionPeriod("JUL-2025")
            .areaOfLaw(AreaOfLaw.LEGAL_HELP)
            .providerUserId(RECIPIENT);
    SendEmailResponse sendResponse = mock(SendEmailResponse.class);
    when(sendResponse.getNotificationId()).thenReturn(UUID.randomUUID());
    when(sendResponse.getTemplateId()).thenReturn(TEMPLATE_ID);
    when(notificationClient.sendEmail(anyString(), anyString(), anyMap(), anyString()))
        .thenReturn(sendResponse);

    service.sendValidationSuccessEmail(submission);

    verify(notificationClient)
        .sendEmail(
            eq(TEMPLATE_ID.toString()),
            eq(RECIPIENT),
            eq(personalisationMapper.toPersonalisation(submission)),
            eq(SUBMISSION_ID.toString()));
  }

  @Test
  void propagatesNotificationClientException() throws Exception {
    SubmissionResponse submission =
        new SubmissionResponse().submissionId(SUBMISSION_ID).providerUserId(RECIPIENT);
    when(notificationClient.sendEmail(anyString(), anyString(), anyMap(), anyString()))
        .thenThrow(new NotificationClientException("boom"));

    assertThatThrownBy(() -> service.sendValidationSuccessEmail(submission))
        .isInstanceOf(NotificationClientException.class)
        .hasMessageContaining("boom");
  }

  @Test
  void passesNullRecipientThroughWhenProviderUserIdIsMissing() throws Exception {
    SubmissionResponse submission = new SubmissionResponse().submissionId(SUBMISSION_ID);
    SendEmailResponse sendResponse = mock(SendEmailResponse.class);
    when(sendResponse.getNotificationId()).thenReturn(UUID.randomUUID());
    when(sendResponse.getTemplateId()).thenReturn(TEMPLATE_ID);
    when(notificationClient.sendEmail(anyString(), any(), anyMap(), anyString()))
        .thenReturn(sendResponse);

    service.sendValidationSuccessEmail(submission);

    verify(notificationClient)
        .sendEmail(eq(TEMPLATE_ID.toString()), eq(null), anyMap(), eq(SUBMISSION_ID.toString()));
  }
}
