package uk.gov.justice.laa.dstew.payments.notify.config.rest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/** Base class for API properties. */
@Getter
@Setter
@AllArgsConstructor
public abstract class ApiProperties {

  private final String url;
  private final String accessToken;
}
