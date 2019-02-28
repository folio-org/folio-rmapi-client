package org.folio.holdingsiq.service.impl;

import io.vertx.core.json.Json;
import org.apache.http.HttpStatus;
import org.folio.holdingsiq.model.PackageByIdData;
import org.folio.holdingsiq.model.PackagePut;
import org.folio.holdingsiq.model.Packages;
import org.folio.holdingsiq.model.Sort;
import org.folio.holdingsiq.service.PackagesHoldingsIQService;
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

public class PackagesHoldingsIQServiceImplTest extends HoldingsIQServiceTestConfig {

  private PackagesHoldingsIQService packagesHoldingsIQService =
    new PackagesHoldingsIQServiceImpl(HoldingsIQServiceImplTest.STUB_CUSTOMER_ID,
      HoldingsIQServiceImplTest.STUB_API_KEY, HoldingsIQServiceImplTest.STUB_BASE_URL, mockVertx);

  @Before
  public void setUp() throws IOException {
    setUpStep();
  }

  @After
  public void tearDown() {
    tearDownStep();
  }

  @Test
  public void testRetrievePackages() {
    mockResponse(mockResponseBody, mockResponse, "{}", HttpStatus.SC_OK);
    CompletableFuture<Packages> completableFuture = packagesHoldingsIQService.retrievePackages("ebsco",
      "filterType", VENDOR_ID, "Query", PAGE_FOR_PARAM, COUNT_FOR_PARAM, Sort.NAME);

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).getAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID
      + "/vendors/" + VENDOR_ID + "/packages?selection=orderedthroughebsco&contenttype=filterType&search=" +
      "Query&offset=1&count=5&orderby=packagename");
  }

  @Test
  public void testRetrievePackagesWithVendorId() {
    mockResponse(mockResponseBody, mockResponse, "{}", HttpStatus.SC_OK);
    CompletableFuture<Packages> completableFuture = packagesHoldingsIQService.retrievePackages(VENDOR_ID);

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).getAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID
      + "/vendors/" + VENDOR_ID + "/packages?selection=all&contenttype=all&search=&offset=1"
      + "&count=25&orderby=packagename");
  }

  @Test
  public void testRetrievePackage() {
    mockResponse(mockResponseBody, mockResponse, "{}", HttpStatus.SC_OK);
    CompletableFuture<PackageByIdData> completableFuture = packagesHoldingsIQService.retrievePackage(packageId);

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).getAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID
      + "/vendors/" + VENDOR_ID + "/packages/" + PACKAGE_ID);
  }

  @Test
  public void testUpdatePackage() {
    mockResponseForUpdateAndCreate(mockResponseBody, mockResponse, "{}", HttpStatus.SC_NO_CONTENT, HttpStatus.SC_OK);
    packagesHoldingsIQService.updatePackage(packageId, PackagePut.builder().build());

    verify(mockClient).putAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID
      + "/vendors/" + VENDOR_ID + "/packages/" + PACKAGE_ID);
  }

  @Test
  public void testDeletePackage() {
    mockResponse(mockResponseBody, mockResponse, "{}", HttpStatus.SC_NO_CONTENT);
    CompletableFuture<Void> completableFuture = packagesHoldingsIQService.deletePackage(packageId);

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).putAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID
      + "/vendors/" + VENDOR_ID + "/packages/" + PACKAGE_ID);
  }

  @Test
  public void testPostPackage() throws IOException {
    mockResponse(mockResponseBody, mockResponse, "{}", HttpStatus.SC_OK);

    when(Json.mapper.readValue(anyString(), any(Class.class))).thenReturn(packageCreated);
    CompletableFuture<PackageByIdData> completableFuture = packagesHoldingsIQService.postPackage(packagePost, VENDOR_ID);

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).postAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID
      + "/vendors/" + VENDOR_ID + "/packages");
    verify(mockClient).getAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID
      + "/vendors/" + VENDOR_ID + "/packages/" + PACKAGE_ID);
  }
}
