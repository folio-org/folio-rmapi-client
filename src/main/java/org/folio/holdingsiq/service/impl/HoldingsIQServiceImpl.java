package org.folio.holdingsiq.service.impl;

import java.util.concurrent.CompletableFuture;

import io.vertx.core.Vertx;

import org.folio.holdingsiq.model.Proxies;
import org.folio.holdingsiq.model.RootProxyCustomLabels;
import org.folio.holdingsiq.service.CommonHoldingsService;
import org.folio.holdingsiq.service.HoldingsIQService;

public class HoldingsIQServiceImpl extends CommonHoldingsService implements HoldingsIQService {

  public HoldingsIQServiceImpl(String customerId, String apiKey, String baseURI, Vertx vertx) {
    super(customerId, apiKey, baseURI, vertx);
  }

  @Override
  public CompletableFuture<Object> verifyCredentials() {
    return getRequest(constructURL(VENDORS_PATH + "?search=zz12&offset=1&orderby=vendorname&count=1"), Object.class);
  }

  @Override
  public CompletableFuture<Proxies> retrieveProxies() {
    return getRequest(constructURL("proxies"), Proxies.class);
  }

  public CompletableFuture<RootProxyCustomLabels> retrieveRootProxyCustomLabels() {
    return this.getRequest(constructURL(""), RootProxyCustomLabels.class);
  }

  @Override
  public CompletableFuture<RootProxyCustomLabels> updateRootProxyCustomLabels(RootProxyCustomLabels rootProxyCustomLabels) {
    final String path = "";

    return this.putRequest(constructURL(path), rootProxyCustomLabels)
      .thenCompose(updatedRootProxy -> this.retrieveRootProxyCustomLabels());
  }

}
