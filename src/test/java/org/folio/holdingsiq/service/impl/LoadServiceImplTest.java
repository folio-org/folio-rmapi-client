package org.folio.holdingsiq.service.impl;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

import static org.folio.holdingsiq.service.util.TestUtil.mockResponse;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.folio.holdingsiq.model.Holdings;
import org.folio.holdingsiq.model.HoldingsLoadStatus;
import org.folio.holdingsiq.service.LoadService;

public class LoadServiceImplTest extends HoldingsIQServiceTestConfig {

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
  public void testGetStatusHoldings() {
    mockResponse(mockResponseBody, mockResponse, "{}", HttpStatus.SC_OK);
    CompletableFuture<HoldingsLoadStatus> completableFuture = loadService.getLoadingStatus();

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).getAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID + "/holdings/status");
  }

  @Test
  public void testGetHoldings() {
    mockResponse(mockResponseBody, mockResponse, "{}", HttpStatus.SC_OK);
    CompletableFuture<Holdings> completableFuture = loadService.loadHoldings(COUNT_FOR_PARAM, PAGE_FOR_PARAM);

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).getAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID +
      "/holdings?format=kbart2&count=" + COUNT_FOR_PARAM + "&offset=" + PAGE_FOR_PARAM);
  }
}
