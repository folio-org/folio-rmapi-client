package org.folio.holdingsiq.service.impl;

import org.apache.http.HttpStatus;
import org.folio.holdingsiq.model.Proxies;
import org.folio.holdingsiq.model.RootProxyCustomLabels;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static org.folio.holdingsiq.service.util.TestUtil.mockResponse;
import static org.folio.holdingsiq.service.util.TestUtil.mockResponseForUpdateAndCreate;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

public class HoldingsIQServiceImplTest extends HoldingsIQServiceTestConfig {

  @Before
  public void setUp() throws IOException {
    setUpStep();
  }

  @After
  public void tearDown() {
    tearDownStep();
  }

  @Test
  public void testVerifyCredentials() {
    mockResponse(mockResponseBody, mockResponse, "{}", HttpStatus.SC_OK);
    CompletableFuture<Object> completableFuture = service.verifyCredentials();

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).getAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID
      + "/vendors?search=zz12&offset=1&orderby=vendorname&count=1");
  }

  @Test
  public void testRetrieveProxies() {
    mockResponse(mockResponseBody, mockResponse, "{}", HttpStatus.SC_OK);
    CompletableFuture<Proxies> completableFuture = service.retrieveProxies();

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).getAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID
      + "/proxies");
  }

  @Test
  public void testUpdateRootProxyCustomLabels() {
    mockResponseForUpdateAndCreate(mockResponseBody, mockResponse, "{}", HttpStatus.SC_NO_CONTENT, HttpStatus.SC_OK);

    CompletableFuture<RootProxyCustomLabels> completableFuture =
      service.updateRootProxyCustomLabels(rootProxyCustomLabels);

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).putAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID + "/");
    verify(mockClient).getAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID + "/");
  }
}
