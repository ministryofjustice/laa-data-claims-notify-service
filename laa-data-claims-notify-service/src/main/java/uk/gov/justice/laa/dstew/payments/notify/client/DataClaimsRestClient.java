package uk.gov.justice.laa.dstew.payments.notify.client;

import org.springframework.web.service.annotation.HttpExchange;

/**
 * REST Service interface for interacting with the Claims API.
 *
 * @author Jamie Briggs
 */
@HttpExchange("/api/v1")
public interface DataClaimsRestClient {}
