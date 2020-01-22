package org.folio.holdingsiq.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

import static org.folio.holdingsiq.service.util.TestUtil.mockResponse;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpStatus;
import org.folio.holdingsiq.model.DeltaReport;
import org.folio.holdingsiq.model.DeltaReportStatus;
import org.folio.holdingsiq.model.HoldingsLoadTransactionStatus;
import org.folio.holdingsiq.model.HoldingsTransactionIdsList;
import org.folio.holdingsiq.model.TransactionId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.folio.holdingsiq.model.Holdings;
import org.folio.holdingsiq.model.HoldingsLoadStatus;
import org.folio.holdingsiq.service.LoadService;

import com.fasterxml.jackson.core.JsonProcessingException;

public class LoadServiceImplTest extends HoldingsIQServiceTestConfig {
  private static final String PREVIOUS_TRANSACTION_ID = "abcd3ab0-da4b-4a1f-a004-a9d323e54cde";
  private static final String TRANSACTION_ID = "84113ab0-da4b-4a1f-a004-a9d686e54811";

  private static final String DELTA_ID = "7e3537a0-3f30-4ef8-9470-dd0a87ac1066";
  private LoadService loadService =
    new LoadServiceImpl(HoldingsIQServiceImplTest.CONFIGURATION, mockVertx);

  @Before
  public void setUp() throws IOException {
    setUpStep();
  }

  @After
  public void tearDown() {
    tearDownStep();
  }

  @Test
  public void testPostHoldings() {
    mockResponse(mockResponseBody, mockResponse, "{}", HttpStatus.SC_ACCEPTED);
    CompletableFuture<Void> completableFuture = loadService.populateHoldings();

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).postAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID + "/holdings");
  }

  @Test
  public void testPostHoldingsTransaction() throws ExecutionException, InterruptedException {
    String transactionId = TRANSACTION_ID;
    mockResponse(mockResponseBody, mockResponse, "{\"transactionId\": \"" + transactionId + "\"}", HttpStatus.SC_ACCEPTED);
    CompletableFuture<TransactionId> completableFuture = loadService.populateHoldingsTransaction();

    assertTrue(isCompletedNormally(completableFuture));
    assertEquals(transactionId, completableFuture.get().getTransactionId());
    verify(mockClient).postAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID + "/reports/holdings?format=kbart2");
  }

  @Test
  public void testGetStatusHoldings() {
    mockResponse(mockResponseBody, mockResponse, "{}", HttpStatus.SC_OK);
    CompletableFuture<HoldingsLoadStatus> completableFuture = loadService.getLoadingStatus();

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).getAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID + "/holdings/status");
  }

  @Test
  public void testGetTransactionStatus() {
    mockResponse(mockResponseBody, mockResponse, "{}", HttpStatus.SC_OK);
    CompletableFuture<HoldingsLoadTransactionStatus> completableFuture = loadService.getTransactionStatus(TRANSACTION_ID);

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).getAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID + "/reports/holdings/transactions/"+TRANSACTION_ID+"/status");
  }

  @Test
  public void testGetTransactions() {
    mockResponse(mockResponseBody, mockResponse, "{}", HttpStatus.SC_OK);
    CompletableFuture<HoldingsTransactionIdsList> completableFuture = loadService.getTransactions();

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).getAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID +
      "/reports/holdings/transactions");
  }

  @Test
  public void testGetHoldings() {
    mockResponse(mockResponseBody, mockResponse, "{}", HttpStatus.SC_OK);
    CompletableFuture<Holdings> completableFuture = loadService.loadHoldings(COUNT_FOR_PARAM, PAGE_FOR_PARAM);

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).getAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID +
      "/holdings?format=kbart2&count=" + COUNT_FOR_PARAM + "&offset=" + PAGE_FOR_PARAM);
  }

  @Test
  public void testGetHoldingsTransaction() {
    mockResponse(mockResponseBody, mockResponse, "{}", HttpStatus.SC_OK);
    CompletableFuture<Holdings> completableFuture = loadService.loadHoldingsTransaction(TRANSACTION_ID, COUNT_FOR_PARAM, PAGE_FOR_PARAM);

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).getAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID +
      "/reports/holdings/transactions/"+TRANSACTION_ID+"?format=kbart2&count=" + COUNT_FOR_PARAM + "&offset=" + PAGE_FOR_PARAM);
  }

  @Test
  public void testPostDeltaReport() throws ExecutionException, InterruptedException {
    mockResponse(mockResponseBody, mockResponse, DELTA_ID, HttpStatus.SC_OK);
    CompletableFuture<String> completableFuture = loadService.populateDeltaReport(TRANSACTION_ID, PREVIOUS_TRANSACTION_ID);

    assertTrue(isCompletedNormally(completableFuture));
    assertEquals(DELTA_ID, completableFuture.get());
    verify(mockClient).postAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID +
      "/reports/holdings/deltas");
  }

  @Test
  public void testGetDeltaReport() {
    mockResponse(mockResponseBody, mockResponse, "{}", HttpStatus.SC_OK);
    CompletableFuture<DeltaReport> completableFuture = loadService.loadDeltaReport(DELTA_ID, COUNT_FOR_PARAM, PAGE_FOR_PARAM);

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).getAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID +
      "/reports/holdings/deltas/"+DELTA_ID+"?format=kbart2&count=" + COUNT_FOR_PARAM + "&offset=" + PAGE_FOR_PARAM);
  }

  @Test
  public void testGetDeltaReportStatus() {
    mockResponse(mockResponseBody, mockResponse, "{}", HttpStatus.SC_OK);
    CompletableFuture<DeltaReportStatus> completableFuture = loadService.getDeltaReportStatus(DELTA_ID);

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).getAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID + "/reports/holdings/deltas/"+DELTA_ID+"/status");
  }
}
