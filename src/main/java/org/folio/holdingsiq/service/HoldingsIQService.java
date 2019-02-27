package org.folio.holdingsiq.service;

import java.util.concurrent.CompletableFuture;

import org.folio.holdingsiq.model.Proxies;
import org.folio.holdingsiq.model.RootProxyCustomLabels;

public interface HoldingsIQService {

  CompletableFuture<Object> verifyCredentials();
  CompletableFuture<Proxies> retrieveProxies();
  CompletableFuture<RootProxyCustomLabels> retrieveRootProxyCustomLabels();
  CompletableFuture<RootProxyCustomLabels> updateRootProxyCustomLabels(RootProxyCustomLabels rootProxyCustomLabels);

}
