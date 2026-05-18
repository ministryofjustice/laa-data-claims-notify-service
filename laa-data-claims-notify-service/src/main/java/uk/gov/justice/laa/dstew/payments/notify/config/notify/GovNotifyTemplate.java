package uk.gov.justice.laa.dstew.payments.notify.config.notify;

/**
 * Enum representing available templates for GovNotify. Values should be added to {@code
 * application.yml} as follows:
 *
 * <pre>{@code
 * app:
 *   gov-notify:
 *     templates:
 *       example-email: ${NOTIFY_EXAMPLE_EMAIL_TEMPLATE_ID:00000000-0000-0000-0000-000000000000}
 *       example-email-two: ${NOTIFY_EXAMPLE_EMAIL_TEMPLATE_ID:00000000-0000-0000-0000-000000000001}
 * }</pre>
 */
public enum GovNotifyTemplate {
  // UPDATE THIS IN FUTURE ONCE EMAILS HAVE BEEN ADDED TO GOV NOTIFY - JAMIE BRIGGS
  EXAMPLE_EMAIL,
  EXAMPLE_EMAIL_TWO;
}
