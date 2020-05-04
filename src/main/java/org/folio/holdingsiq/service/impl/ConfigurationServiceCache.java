package org.folio.holdingsiq.service.impl;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.concurrent.CompletableFuture.completedFuture;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import io.vertx.core.Context;

import org.folio.cache.VertxCache;
import org.folio.holdingsiq.model.Configuration;
import org.folio.holdingsiq.model.ConfigurationError;
import org.folio.holdingsiq.model.OkapiData;
import org.folio.holdingsiq.service.ConfigurationService;
import org.folio.util.TokenUtils;

public class ConfigurationServiceCache implements ConfigurationService {

  private final ConfigurationService configurationService;

  private final VertxCache<String, Configuration> configurationCache;


  public ConfigurationServiceCache(ConfigurationService configurationService,
      VertxCache<String, Configuration> configurationCache) {
    this.configurationService = configurationService;
    this.configurationCache = configurationCache;
  }

  @Override
  public CompletableFuture<Configuration> retrieveConfiguration(OkapiData okapiData) {
    return TokenUtils.fetchUserInfo(okapiData.getApiToken())
      .thenCompose(userInfo -> configurationCache.getValueOrLoad(
          userInfo.getUserId(),
          () -> configurationService.retrieveConfiguration(okapiData))
      );
  }

  @Override
  public CompletableFuture<List<ConfigurationError>> verifyCredentials(Configuration configuration, Context vertxContext,
                                                                       OkapiData okapiData) {
    if (configuration.getConfigValid() != null && configuration.getConfigValid()) {
      return completedFuture(emptyList());
    }

    return configurationService.verifyCredentials(configuration, vertxContext, okapiData)
      .thenCompose(errors -> {
        if (!errors.isEmpty()) {
          return completedFuture(errors);
        }

        return storeConfigurationInCache(configuration, okapiData)
          .thenApply(v -> Collections.<ConfigurationError>emptyList())
          .exceptionally(t -> singletonList(new ConfigurationError(t.getMessage())));
      });
  }

  private CompletableFuture<Void> storeConfigurationInCache(Configuration config, OkapiData okapiData) {
    return TokenUtils.fetchUserInfo(okapiData.getApiToken())
      .thenAccept(userInfo -> configurationCache.putValue(
          userInfo.getUserId(),
          config.toBuilder().configValid(true).build())
      );
  }

}
