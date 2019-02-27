package org.folio.holdingsiq.service.impl;

import io.vertx.core.json.Json;
import org.apache.http.HttpStatus;
import org.folio.holdingsiq.model.Sort;
import org.folio.holdingsiq.model.VendorById;
import org.folio.holdingsiq.model.Vendors;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ProviderHoldingsIQServiceImplTest extends HoldingsIQServiceImplTest {

  private ProviderHoldingsIQServiceImpl providerHoldingsIQService =
    new ProviderHoldingsIQServiceImpl(HoldingsIQServiceImplTest.STUB_CUSTOMER_ID,
      HoldingsIQServiceImplTest.STUB_API_KEY, HoldingsIQServiceImplTest.STUB_BASE_URL, mockVertx, service);

  @Test
  public void testGetVendorId() throws IOException {
    mockResponse("{}", HttpStatus.SC_OK);
    when(Json.mapper.readValue(anyString(), any(Class.class))).thenReturn(rootProxyCustomLabels);
    CompletableFuture<Long> completableFuture = providerHoldingsIQService.getVendorId();

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).getAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID + "/");
  }

  @Test
  public void testRetrieveVendors() {
    mockResponse("{}", HttpStatus.SC_OK);
    CompletableFuture<Vendors> completableFuture = providerHoldingsIQService.retrieveProviders("Busket",
      PAGE_FOR_PARAM,
      COUNT_FOR_PARAM, Sort.NAME);

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).getAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID
      + "/vendors?search=Busket&offset=1&count=5&orderby=vendorname");
  }

  @Test
  public void testRetrieveVendorsCompleteExceptionallyWhenRequestWithError404() {
    mockResponse("{}", HttpStatus.SC_NOT_FOUND, "Error 404. Not faund");
    CompletableFuture<Vendors> future = providerHoldingsIQService.retrieveProviders("Busket",
      PAGE_FOR_PARAM, COUNT_FOR_PARAM, Sort.NAME);
    assertTrue(future.isCompletedExceptionally());
  }

  @Test
  public void testRetrieveVendorsCompleteExceptionallyWhenRequestWithError401() {
    mockResponse("{}", HttpStatus.SC_UNAUTHORIZED, "Error 401 unauthorized");
    CompletableFuture<Vendors> future = providerHoldingsIQService.retrieveProviders("Busket",
      PAGE_FOR_PARAM, COUNT_FOR_PARAM, Sort.NAME);
    assertTrue(future.isCompletedExceptionally());
  }

  @Test
  public void testRetrieveVendorsCompleteExceptionallyWhenThrowServiceException() throws IOException {
    mockResponse("{}", HttpStatus.SC_OK);
    when(Json.mapper.readValue(anyString(), any(Class.class))).thenThrow(IOException.class);

    CompletableFuture<Vendors> future = providerHoldingsIQService.retrieveProviders("Busket",
      PAGE_FOR_PARAM, COUNT_FOR_PARAM, Sort.NAME);

    assertTrue(future.isCompletedExceptionally());
  }

  @Test
  public void testUpdateVendor() {
    mockResponseForUpdateAndCreate("{}", HttpStatus.SC_NO_CONTENT, HttpStatus.SC_OK);
    CompletableFuture<VendorById> completableFuture = providerHoldingsIQService
      .updateProvider(VENDOR_ID, vendorPut);

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).putAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID
      + "/vendors/" + VENDOR_ID);
  }
}
