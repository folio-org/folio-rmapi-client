package org.folio.holdingsiq.service.impl;

import java.util.concurrent.CompletableFuture;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.Json;

import org.folio.holdingsiq.model.Configuration;
import org.folio.holdingsiq.model.Holdings;
import org.folio.holdingsiq.model.HoldingsLoadStatus;
import org.folio.holdingsiq.service.LoadService;

public class LoadServiceImpl implements LoadService {

  private HoldingsRequestHelper holdingsRequestHelper;
  private Vertx vertx;

  public LoadServiceImpl(Configuration config, Vertx vertx) {
    this.vertx = vertx;
    holdingsRequestHelper = new HoldingsRequestHelper(config, vertx);
  }

  @Override
  public CompletableFuture<Void> populateHoldings() {
    return postHoldings(holdingsRequestHelper.constructURL("holdings"));
  }

  public CompletableFuture<HoldingsLoadStatus> getLoadingStatus() {
    return holdingsRequestHelper.getRequest(holdingsRequestHelper.constructURL("holdings/status"), HoldingsLoadStatus.class);
  }

  @Override
  public CompletableFuture<Holdings> loadHoldings(int count, int offset) {
    String path = "holdings?format=kbart2&count=" + count + "&offset=" + offset;
    return holdingsRequestHelper.getRequest(holdingsRequestHelper.constructURL(path), Holdings.class);
  }

  private CompletableFuture<Void> postHoldings(String query) {

    CompletableFuture<Void> future = new CompletableFuture<>();

    HttpClient httpClient = vertx.createHttpClient();
    final HttpClientRequest request = httpClient.postAbs(query);

    holdingsRequestHelper.addRequestHeaders(request);

    executeHoldingsRequest(query, future, httpClient, request);

    String encodedBody = Json.encodePrettily("");
    request.end(encodedBody);
    return future;
  }

  private void executeHoldingsRequest(String query, CompletableFuture<Void> future, HttpClient httpClient,
                                      HttpClientRequest request) {
    request.handler(response -> response.bodyHandler(body -> {
      httpClient.close();
      if (response.statusCode() == 202 || response.statusCode() == 409) {
        future.complete(null);
      } else {
        holdingsRequestHelper.handleRMAPIError(response, query, body, future);
      }
    })).exceptionHandler(future::completeExceptionally);
  }
}
