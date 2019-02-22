package org.folio.holdingsiq.model;

import io.vertx.core.shareddata.Shareable;
import lombok.Builder;
import lombok.Value;

/**
 * Contains the RM API connection details from mod-configuration.
 */
@Value
@Builder(toBuilder = true)
public final class Configuration implements Shareable {

  private final String customerId;
  private final String apiKey;
  private final String url;
  private final Boolean configValid;

}
