package uk.gov.justice.laa.dstew.payments.notify.model.event;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("SubmissionEvent JSON binding")
class SubmissionEventTest {

  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  void deserialisesSnakeCasePayload() throws Exception {
    String json =
        """
        { "submission_id": "11111111-1111-1111-1111-111111111111" }
        """;

    SubmissionEvent event = mapper.readValue(json, SubmissionEvent.class);

    assertThat(event.getSubmissionId()).isEqualTo("11111111-1111-1111-1111-111111111111");
  }

  @Test
  void deserialisesPayloadWithMissingFieldAsNull() throws Exception {
    SubmissionEvent event = mapper.readValue("{}", SubmissionEvent.class);
    assertThat(event.getSubmissionId()).isNull();
  }
}
