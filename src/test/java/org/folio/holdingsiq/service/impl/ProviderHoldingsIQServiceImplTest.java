package org.folio.holdingsiq.service.impl;

import io.vertx.core.json.Json;
import org.apache.http.HttpStatus;
import org.folio.holdingsiq.model.Sort;
import org.folio.holdingsiq.model.VendorById;
import org.folio.holdingsiq.model.Vendors;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static org.folio.holdingsiq.service.util.TestUtil.mockResponse;
import static org.folio.holdingsiq.service.util.TestUtil.mockResponseForUpdateAndCreate;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;

public class ProviderHoldingsIQServiceImplTest extends HoldingsIQServiceTestConfig {

  private ProviderHoldingsIQServiceImpl providerHoldingsIQService =
    new ProviderHoldingsIQServiceImpl(HoldingsIQServiceImplTest.CONFIGURATION, mockVertx, service);

  @Before
  public void setUp() throws IOException {
    setUpStep();
  }

  @After
  public void tearDown() {
    tearDownStep();
  }

  @Test
  public void testGetVendorId() throws IOException {
    mockResponse(mockResponseBody, mockResponse, "{}", HttpStatus.SC_OK);
    when(Json.mapper.readValue(anyString(), any(Class.class))).thenReturn(rootProxyCustomLabels);
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
  public void testRetrieveVendorsCompleteExceptionallyWhenThrowServiceException() throws JsonProcessingException {
    mockResponse(mockResponseBody, mockResponse, "{}", HttpStatus.SC_OK);
    when(Json.mapper.readValue(anyString(), any(Class.class))).thenThrow(JsonParseException.class);

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
