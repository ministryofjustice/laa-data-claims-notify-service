package uk.gov.justice.laa.dstew.payments.notify.client;

import java.util.UUID;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;

@HttpExchange("/api/v1")
public interface DataClaimsRestClient {

  @GetExchange("/submissions/{id}")
  SubmissionResponse getSubmission(@PathVariable("id") UUID id);
}
