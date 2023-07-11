package org.folio.holdingsiq.service;

import java.util.concurrent.CompletableFuture;

import org.folio.holdingsiq.model.DeltaReport;
import org.folio.holdingsiq.model.DeltaReportStatus;
import org.folio.holdingsiq.model.Holdings;
import org.folio.holdingsiq.model.HoldingsLoadStatus;
import org.folio.holdingsiq.model.HoldingsLoadTransactionStatus;
import org.folio.holdingsiq.model.HoldingsTransactionIdsList;
import org.folio.holdingsiq.model.TransactionId;

public interface LoadService {

  /**
   * Populates holdings data to a staging area
   */
  CompletableFuture<Void> populateHoldings();

  /**
   * Populates holdings data to a staging area with force parameter
   */
  CompletableFuture<Void> populateHoldingsForce();

  /**
   * Create a new staging area and populate it with holdings data.
   * @return id of created staging area (i.e. transaction id)
   */
  CompletableFuture<TransactionId> populateHoldingsTransaction();

  /**
   * Retrieve the status of a holdings snapshot.
   * The response reveals if the holdings snapshot is in progress, completed or has failed
   * @return loading status.
   */
  CompletableFuture<HoldingsLoadStatus> getLoadingStatus();

  /**
   * Retrieve the status of a holdings snapshot by transactionId.
   * The response reveals if the holdings snapshot is in progress, completed or has failed
   * @return loading status of transaction.
   */
  CompletableFuture<HoldingsLoadTransactionStatus> getTransactionStatus(String transactionId);

  /**
   * Retrieve a list of all available transactions
   * @return status of all available transaction
   */
  CompletableFuture<HoldingsTransactionIdsList> getTransactions();

  /**
   * Download customer holdings resources from the staged area
   *
   * @param count The maximum number of results to return. Count can not exceed 5000.
   * @param offset Page offset.
   * @return holdings result.
   */
  CompletableFuture<Holdings> loadHoldings(int count, int offset);

  /**
   * Download customer holdings from transaction.
   * @param transactionId id of transaction
   * @param count The maximum number of results to return. Count can not exceed 4000.
   * @param offset Page offset.
   * @return holdings result.
   */
  CompletableFuture<Holdings> loadHoldingsTransaction(String transactionId, int count, int offset);

  /**
   * Download list of holding changes from delta report.
   * @param deltaReportId id of delta report
   * @param count The maximum number of results to return. Count can not exceed 4000.
   * @param offset Page offset.
   * @return List of changes.
   */
  CompletableFuture<DeltaReport> loadDeltaReport(String deltaReportId, int count, int offset);

  /**
   * Retrieve the status of a delta report by deltaReportId.
   * The response reveals if delta report creation is in progress, completed or has failed
   * @return status of delta report.
   */
  CompletableFuture<DeltaReportStatus> getDeltaReportStatus(String deltaReportId);

  /**
   * Creates delta report with changes between snapshots with previousSnapshotId and currentSnapshotId.
   * @param currentSnapshotId changes will be calculated up to transaction with currentSnapshotId
   * @param previousSnapshotId changes will be calculated starting from transaction with previousSnapshotId
   * @return Delta report id
   */
  CompletableFuture<String> populateDeltaReport(String currentSnapshotId, String previousSnapshotId);
}
