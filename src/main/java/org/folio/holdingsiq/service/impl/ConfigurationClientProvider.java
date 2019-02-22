package org.folio.holdingsiq.service.impl;

import org.folio.rest.client.ConfigurationsClient;

/**
 * Class used to create ConfigurationsClient, to allow mocking of client in unit tests
 */
public class ConfigurationClientProvider {

  public ConfigurationsClient createClient(String url, int port, String tenantId, String apiToken) {
    return new ConfigurationsClient(url, port, tenantId, apiToken);
  }

}
