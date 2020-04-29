package org.folio.holdingsiq.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import io.vertx.core.Context;

import org.folio.holdingsiq.model.Configuration;
import org.folio.holdingsiq.model.ConfigurationError;
import org.folio.holdingsiq.model.OkapiData;

public interface ConfigurationService {

  CompletableFuture<Configuration> retrieveConfiguration(OkapiData okapiData);

  CompletableFuture<List<ConfigurationError>> verifyCredentials(Configuration configuration, Context vertxContext,
                                                                OkapiData okapiData);
}
