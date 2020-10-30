package org.folio.holdingsiq.service.impl;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import static org.folio.holdingsiq.service.util.TestUtil.mockResponse;
import static org.folio.holdingsiq.service.util.TestUtil.mockResponseForUpdateAndCreate;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import io.vertx.core.json.Json;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import org.folio.holdingsiq.model.Sort;
import org.folio.holdingsiq.model.VendorById;
import org.folio.holdingsiq.model.Vendors;

@RunWith(PowerMockRunner.class)
@PrepareForTest(HoldingsRequestHelper.class)
@PowerMockIgnore({"org.apache.logging.log4j.*"})
public class ProviderHoldingsIQServiceImplTest extends HoldingsIQServiceTestConfig {

  private ProviderHoldingsIQServiceImpl providerHoldingsIQService;


  @Before
  public void setUp() throws IOException {
    setUpStep();

    mockStatic(HoldingsRequestHelper.class);
    HoldingsResponseBodyListener listener = mock(HoldingsResponseBodyListener.class);
    // replace real logger with fake one to avoid calls to statusCode() method in the real logger
    // those calls are mocked inside tests, moreover their number is strictly defined. so any excessive calls
    // lead to test failures. this is a quick solution to the problem and should be revised later
    when(HoldingsRequestHelper.successBodyLogger()).thenReturn(listener);

    providerHoldingsIQService = new ProviderHoldingsIQServiceImpl(HoldingsIQServiceImplTest.CONFIGURATION, mockVertx,
        new HoldingsIQServiceImpl(CONFIGURATION, mockVertx));
  }

  @After
  public void tearDown() {
    tearDownStep();
  }

  @Test
  public void testGetVendorId() throws IOException {
    mockResponse(mockResponseBody, mockResponse, "{}", HttpStatus.SC_OK);
    doReturn(rootProxyCustomLabels).when(Json.mapper).readValue(any(JsonParser.class), any(Class.class));
    CompletableFuture<Long> completableFuture = providerHoldingsIQService.getVendorId();

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).getAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID + "/");
  }

  @Test
  public void testRetrieveVendors() {
    mockResponse(mockResponseBody, mockResponse, "{}", HttpStatus.SC_OK);
    CompletableFuture<Vendors> completableFuture = providerHoldingsIQService.retrieveProviders("Busket",
      PAGE_FOR_PARAM,
      COUNT_FOR_PARAM, Sort.NAME);

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).getAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID
      + "/vendors?search=Busket&offset=1&count=5&orderby=vendorname");
  }

  @Test
  public void testRetrieveVendorsCompleteExceptionallyWhenRequestWithError404() {
    mockResponse(mockResponseBody, mockResponse, "{}", HttpStatus.SC_NOT_FOUND, "Not Found");
    CompletableFuture<Vendors> future = providerHoldingsIQService.retrieveProviders("Busket",
      PAGE_FOR_PARAM, COUNT_FOR_PARAM, Sort.NAME);
    assertTrue(future.isCompletedExceptionally());
  }

  @Test
  public void testRetrieveVendorsCompleteExceptionallyWhenRequestWithError401() {
    mockResponse(mockResponseBody, mockResponse, "{}", HttpStatus.SC_UNAUTHORIZED, "Unauthorized");
    CompletableFuture<Vendors> future = providerHoldingsIQService.retrieveProviders("Busket",
      PAGE_FOR_PARAM, COUNT_FOR_PARAM, Sort.NAME);
    assertTrue(future.isCompletedExceptionally());
  }

  @Test
  public void testRetrieveVendorsCompleteExceptionallyWhenThrowServiceException() throws IOException {
    mockResponse(mockResponseBody, mockResponse, "{}", HttpStatus.SC_OK);
    doThrow(JsonParseException.class).when(Json.mapper).readValue(any(JsonParser.class), any(Class.class));

    CompletableFuture<Vendors> future = providerHoldingsIQService.retrieveProviders("Busket",
      PAGE_FOR_PARAM, COUNT_FOR_PARAM, Sort.NAME);

    assertTrue(future.isCompletedExceptionally());
  }

  @Test
  public void testUpdateVendor() {
    mockResponseForUpdateAndCreate(mockResponseBody, mockResponse, "{}", HttpStatus.SC_NO_CONTENT, HttpStatus.SC_OK);
    CompletableFuture<VendorById> completableFuture = providerHoldingsIQService
      .updateProvider(VENDOR_ID, vendorPut);

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).putAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID
      + "/vendors/" + VENDOR_ID);
  }
}
