package org.folio.holdingsiq.service.impl;

import static org.folio.holdingsiq.service.impl.HoldingsRequestHelper.VENDORS_PATH;

import java.util.concurrent.CompletableFuture;

import io.vertx.core.Vertx;

import org.folio.holdingsiq.model.Configuration;
import org.folio.holdingsiq.model.Proxies;
import org.folio.holdingsiq.model.RootProxyCustomLabels;
import org.folio.holdingsiq.service.HoldingsIQService;

public class HoldingsIQServiceImpl implements HoldingsIQService {

  private HoldingsRequestHelper holdingsRequestHelper;

  public HoldingsIQServiceImpl(Configuration config, Vertx vertx) {
    holdingsRequestHelper = new HoldingsRequestHelper(config, vertx);
  }

  @Override
  public CompletableFuture<Object> verifyCredentials() {
    return holdingsRequestHelper.getRequest(holdingsRequestHelper.constructURL(VENDORS_PATH + "?search=zz12&offset=1&orderby=vendorname&count=1"), Object.class);
  }

  @Override
  public CompletableFuture<Proxies> retrieveProxies() {
    return holdingsRequestHelper.getRequest(holdingsRequestHelper.constructURL("proxies"), Proxies.class);
  }

  public CompletableFuture<RootProxyCustomLabels> retrieveRootProxyCustomLabels() {
    return holdingsRequestHelper.getRequest(holdingsRequestHelper.constructURL(""), RootProxyCustomLabels.class);
  }

  @Override
  public CompletableFuture<RootProxyCustomLabels> updateRootProxyCustomLabels(RootProxyCustomLabels rootProxyCustomLabels) {
    final String path = "";

    return holdingsRequestHelper.putRequest(holdingsRequestHelper.constructURL(path), rootProxyCustomLabels)
      .thenCompose(updatedRootProxy -> this.retrieveRootProxyCustomLabels());
  }

}
