package uk.gov.justice.laa.dstew.payments.notify.config.notify;

import java.util.EnumMap;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties holder for interfacing with the GOV.UK Notify service.
 *
 * <p>This class is used to configure and provide access to properties required for communication
 * with the GOV.UK Notify service, including API keys and template mappings. It is designed to be
 * loaded automatically from application properties using the prefix "app.gov-notify".
 *
 * <p>Loading template IDs this way via configuration helps keep them secret.
 *
 * <p>Properties:
 *
 * <ul>
 *   <li>key: The API key used to authenticate with the GOV.UK Notify service.
 *   <li>templates: A mapping of {@link GovNotifyTemplate} enumerations to their corresponding *
 *       template identifiers in the GOV.UK Notify system.
 * </ul>
 *
 * @author Jamie Briggs
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.gov-notify")
public class GovNotifyProperties {

  private String key;
  private EnumMap<GovNotifyTemplate, String> templates = new EnumMap<>(GovNotifyTemplate.class);
}
