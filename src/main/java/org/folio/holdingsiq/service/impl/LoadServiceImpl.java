package org.folio.holdingsiq.service.impl;

import java.util.concurrent.CompletableFuture;

import io.vertx.core.Vertx;

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

public class LoadServiceImpl implements LoadService {

  private final HoldingsRequestHelper holdingsRequestHelper;

  public LoadServiceImpl(Configuration config, Vertx vertx) {
    this.holdingsRequestHelper = new HoldingsRequestHelper(config, vertx);
  }

  @Override
  public CompletableFuture<Void> populateHoldings() {
    return holdingsRequestHelper.postRequest(holdingsRequestHelper.constructURL("holdings"), String.class)
      .thenAccept(response -> {});
  }

  @Override
  public CompletableFuture<Void> populateHoldingsForce() {
    return holdingsRequestHelper.postRequest(holdingsRequestHelper.constructURL("holdings?force=true"), String.class)
      .thenAccept(response -> {});
  }

  @Override
  public CompletableFuture<TransactionId> populateHoldingsTransaction() {
    return holdingsRequestHelper.postRequest(holdingsRequestHelper.constructURL("reports/holdings?format=kbart2"), TransactionId.class);
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
    DeltaReportParams postData = DeltaReportParams.builder()
      .currentSnapshotId(currentSnapshotId)
      .previousSnapshotId(previousSnapshotId).build();
    return holdingsRequestHelper.postRequest(holdingsRequestHelper.constructURL("reports/holdings/deltas"), postData,  String.class);
  }

}
