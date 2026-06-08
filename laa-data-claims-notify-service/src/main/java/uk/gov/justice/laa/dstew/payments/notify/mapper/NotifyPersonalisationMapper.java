package uk.gov.justice.laa.dstew.payments.notify.mapper;

import static java.util.Objects.isNull;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;

@Component
public class NotifyPersonalisationMapper {

  private final String sabcBaseUrl;

  public NotifyPersonalisationMapper(@Value("${app.sabc.url}") String sabcBaseUrl) {
    this.sabcBaseUrl = sabcBaseUrl;
  }

  public Map<String, Object> toPersonalisation(SubmissionResponse submission) {
    Map<String, Object> personalisation = new LinkedHashMap<>();
    personalisation.put("office_account", nullSafe(submission.getOfficeAccountNumber()));
    personalisation.put("submission_period", nullSafe(submission.getSubmissionPeriod()));
    personalisation.put("area_of_law", areaOfLawValue(submission.getAreaOfLaw()));
    personalisation.put("submission_url", sabcBaseUrl);
    if (!isNull(submission.getSubmissionId())) {
      personalisation.put(
          "submission_url", sabcBaseUrl + "/submission/" + submission.getSubmissionId());
    }
    return personalisation;
  }

  private static String nullSafe(String value) {
    return isNull(value) ? "" : value;
  }

  private static String areaOfLawValue(AreaOfLaw areaOfLaw) {
    return isNull(areaOfLaw) ? "" : areaOfLaw.getValue();
  }
}
