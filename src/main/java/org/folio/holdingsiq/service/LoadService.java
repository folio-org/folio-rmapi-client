package org.folio.holdingsiq.service;

import java.util.concurrent.CompletableFuture;

import org.folio.holdingsiq.model.Holdings;
import org.folio.holdingsiq.model.HoldingsLoadStatus;

public interface LoadService {

  /**
   * Populates holdings data to a staging area
   */
  CompletableFuture<Void> populateHoldings();


  /**
   * Retrieve the status of a holdings snapshot.
   * The response reveals if the holdings snapshot is in progress, completed or has failed
   * @return loading status.
   */
  CompletableFuture<HoldingsLoadStatus> getLoadingStatus();

  /**
   * Download customer holdings resources from the staged area
   *
   * @param count The maximum number of results to return. Count can not exceed 5000.
   * @param offset Page offset.
   * @return holdings result.
   */
  CompletableFuture<Holdings> loadHoldings(int count, int offset);
}
