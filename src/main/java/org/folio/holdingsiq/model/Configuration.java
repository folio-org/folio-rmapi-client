package org.folio.holdingsiq.model;

import io.vertx.core.shareddata.Shareable;
import lombok.Builder;
import lombok.Value;

/**
 * Contains the RM API connection details from mod-configuration.
 */
@Value
@Builder(toBuilder = true)
public class Configuration implements Shareable {

  String customerId;
  String apiKey;
  String url;
  Boolean configValid;

}
