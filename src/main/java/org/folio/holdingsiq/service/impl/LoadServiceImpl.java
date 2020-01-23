package org.folio.holdingsiq.service.impl;

import java.util.concurrent.CompletableFuture;

import org.folio.holdingsiq.model.Configuration;
import org.folio.holdingsiq.model.DeltaReport;
import org.folio.holdingsiq.model.DeltaReportParams;
import org.folio.holdingsiq.model.DeltaReportStatus;
import org.folio.holdingsiq.model.Holdings;
import org.folio.holdingsiq.model.HoldingsLoadStatus;
import org.folio.holdingsiq.model.HoldingsLoadTransactionStatus;
import org.folio.holdingsiq.model.HoldingsTransactionIdsList;
import org.folio.holdingsiq.model.TransactionId;
import org.folio.holdingsiq.service.LoadService;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.Json;

public class LoadServiceImpl implements LoadService {

  private HoldingsRequestHelper holdingsRequestHelper;
  private Vertx vertx;

  public LoadServiceImpl(Configuration config, Vertx vertx) {
    this.vertx = vertx;
    holdingsRequestHelper = new HoldingsRequestHelper(config, vertx);
  }

  @Override
  public CompletableFuture<Void> populateHoldings() {
    return postRequestWithoutBody(holdingsRequestHelper.constructURL("holdings"))
      .thenAccept(response -> {});
  }

  @Override
  public CompletableFuture<TransactionId> populateHoldingsTransaction() {
    return postRequestWithoutBody(holdingsRequestHelper.constructURL("reports/holdings?format=kbart2"), TransactionId.class);
  }

  @Override
  public CompletableFuture<HoldingsLoadStatus> getLoadingStatus() {
    return holdingsRequestHelper.getRequest(holdingsRequestHelper.constructURL("holdings/status"), HoldingsLoadStatus.class);
  }

  @Override
  public CompletableFuture<HoldingsLoadTransactionStatus> getTransactionStatus(String transactionId) {
    return holdingsRequestHelper.getRequest(holdingsRequestHelper.constructURL("reports/holdings/transactions/"+transactionId+"/status"), HoldingsLoadTransactionStatus.class);
  }

  @Override
  public CompletableFuture<HoldingsTransactionIdsList> getTransactions() {
    return holdingsRequestHelper.getRequest(holdingsRequestHelper.constructURL("reports/holdings/transactions"), HoldingsTransactionIdsList.class);
  }

  @Override
  public CompletableFuture<Holdings> loadHoldings(int count, int offset) {
    String path = "holdings?format=kbart2&count=" + count + "&offset=" + offset;
    return holdingsRequestHelper.getRequest(holdingsRequestHelper.constructURL(path), Holdings.class);
  }
  @Override
  public CompletableFuture<Holdings> loadHoldingsTransaction(String transactionId, int count, int offset) {
    String path = "reports/holdings/transactions/"+transactionId+"?format=kbart2&count=" + count + "&offset=" + offset;
    return holdingsRequestHelper.getRequest(holdingsRequestHelper.constructURL(path), Holdings.class);
  }

  @Override
  public CompletableFuture<DeltaReport> loadDeltaReport(String deltaReportId, int count, int offset) {
    String path = "reports/holdings/deltas/"+ deltaReportId +"?format=kbart2&count=" + count + "&offset=" + offset;
    return holdingsRequestHelper.getRequest(holdingsRequestHelper.constructURL(path), DeltaReport.class);
  }

  @Override
  public CompletableFuture<DeltaReportStatus> getDeltaReportStatus(String deltaReportId) {
    return holdingsRequestHelper.getRequest(holdingsRequestHelper.constructURL("reports/holdings/deltas/"+deltaReportId+"/status"), DeltaReportStatus.class);
  }

  @Override
  public CompletableFuture<String> populateDeltaReport(String currentSnapshotId, String previousSnapshotId) {
    DeltaReportParams postData = DeltaReportParams
      .builder()
    .currentSnapshotId(currentSnapshotId)
    .previousSnapshotId(previousSnapshotId).build();
    return holdingsRequestHelper.postRequest(holdingsRequestHelper.constructURL("reports/holdings/deltas"), postData,  String.class);
  }

  private <T> CompletableFuture<T> postRequestWithoutBody(String query, Class<T> responseType) {
    return postRequestWithoutBody(query)
      .thenApply(response -> Json.decodeValue(response, responseType));
  }
  private CompletableFuture<String> postRequestWithoutBody(String query) {

    CompletableFuture<String> future = new CompletableFuture<>();

    HttpClient httpClient = vertx.createHttpClient();
    final HttpClientRequest request = httpClient.postAbs(query);

    holdingsRequestHelper.addRequestHeaders(request);

    executeHoldingsRequest(query, future, httpClient, request);

    String encodedBody = Json.encodePrettily("");
    request.end(encodedBody);
    return future;
  }

  private void executeHoldingsRequest(String query, CompletableFuture<String> future, HttpClient httpClient,
                                      HttpClientRequest request) {
    request.handler(response -> response.bodyHandler(body -> {
      httpClient.close();
      if (response.statusCode() == 202 || response.statusCode() == 409) {
        future.complete(body.toString());
      } else {
        holdingsRequestHelper.handleRMAPIError(response, query, body, future);
      }
    })).exceptionHandler(future::completeExceptionally);
  }
}
