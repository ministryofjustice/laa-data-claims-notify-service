package uk.gov.justice.laa.dstew.payments.notify.config.rest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ApiProperties {

  private final String url;
  private final String accessToken;
}
