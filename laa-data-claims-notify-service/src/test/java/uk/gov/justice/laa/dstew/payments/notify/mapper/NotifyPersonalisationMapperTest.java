package uk.gov.justice.laa.dstew.payments.notify.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;

@DisplayName("NotifyPersonalisationMapper")
class NotifyPersonalisationMapperTest {

  private static final UUID SUBMISSION_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final String SABC_URL = "http://localhost:8082";

  private final NotifyPersonalisationMapper mapper = new NotifyPersonalisationMapper(SABC_URL);

  @Test
  void mapsAllFieldsWhenPresent() {
    SubmissionResponse submission =
        new SubmissionResponse()
            .submissionId(SUBMISSION_ID)
            .officeAccountNumber("OFFICE-1")
            .submissionPeriod("JUL-2025")
            .areaOfLaw(AreaOfLaw.LEGAL_HELP);

    Map<String, Object> personalisation = mapper.toPersonalisation(submission);

    assertThat(personalisation)
        .containsEntry("submission_url", SABC_URL + "/submission/" + SUBMISSION_ID)
        .containsEntry("office_account", "OFFICE-1")
        .containsEntry("submission_period", "JUL-2025")
        .containsEntry("area_of_law", "Legal help");
  }

  @Test
  void substitutesEmptyStringForNullStringFields() {
    SubmissionResponse submission = new SubmissionResponse().submissionId(SUBMISSION_ID);

    Map<String, Object> personalisation = mapper.toPersonalisation(submission);

    assertThat(personalisation)
        .containsEntry("submission_url", SABC_URL + "/submission/" + SUBMISSION_ID)
        .containsEntry("office_account", "")
        .containsEntry("submission_period", "")
        .containsEntry("area_of_law", "");
  }

  @Test
  void serialisesNullSubmissionIdAsBaseUrl() {
    SubmissionResponse submission = new SubmissionResponse();

    Map<String, Object> personalisation = mapper.toPersonalisation(submission);

    assertThat(personalisation).containsEntry("submission_url", SABC_URL);
  }

  @Test
  void usesAreaOfLawDisplayValueNotEnumName() {
    SubmissionResponse submission =
        new SubmissionResponse().submissionId(SUBMISSION_ID).areaOfLaw(AreaOfLaw.LEGAL_HELP);

    Map<String, Object> personalisation = mapper.toPersonalisation(submission);

    assertThat(personalisation.get("area_of_law"))
        .isEqualTo("Legal help")
        .isNotEqualTo(AreaOfLaw.LEGAL_HELP.name());
  }
}
