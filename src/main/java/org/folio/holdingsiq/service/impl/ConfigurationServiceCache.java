package org.folio.holdingsiq.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import io.vertx.core.Context;

import org.folio.cache.VertxCache;
import org.folio.holdingsiq.model.Configuration;
import org.folio.holdingsiq.model.ConfigurationError;
import org.folio.holdingsiq.model.OkapiData;
import org.folio.holdingsiq.service.ConfigurationService;

public class ConfigurationServiceCache implements ConfigurationService {

  private ConfigurationService configurationService;

  private VertxCache<String, Configuration> configurationCache;


  public ConfigurationServiceCache(ConfigurationService configurationService,
      VertxCache<String, Configuration> configurationCache) {
    this.configurationService = configurationService;
    this.configurationCache = configurationCache;
  }

  @Override
  public CompletableFuture<Configuration> retrieveConfiguration(OkapiData okapiData) {
    return configurationCache
      .getValueOrLoad(okapiData.getTenant(),
        () -> configurationService.retrieveConfiguration(okapiData)
      );
  }

  @Override
  public CompletableFuture<List<ConfigurationError>> verifyCredentials(Configuration configuration, Context vertxContext,
                                                                       OkapiData okapiData) {
    if(configuration.getConfigValid() != null && configuration.getConfigValid()){
      return CompletableFuture.completedFuture(Collections.emptyList());
    }
    return configurationService.verifyCredentials(configuration, vertxContext, okapiData)
      .thenCompose(errors -> {
        if(errors.isEmpty()){
          configurationCache.putValue(okapiData.getTenant(),
            configuration.toBuilder().configValid(true).build());
        }
        return CompletableFuture.completedFuture(errors);
      });
  }

}
