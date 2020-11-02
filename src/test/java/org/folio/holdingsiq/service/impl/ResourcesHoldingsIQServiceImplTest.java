package org.folio.holdingsiq.service.impl;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import static org.folio.holdingsiq.service.util.TestUtil.mockResponse;
import static org.folio.holdingsiq.service.util.TestUtil.mockResponseForUpdateAndCreate;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import org.folio.holdingsiq.model.ResourceSelectedPayload;
import org.folio.holdingsiq.model.Title;
import org.folio.holdingsiq.service.ResourcesHoldingsIQService;

@RunWith(PowerMockRunner.class)
@PrepareForTest(HoldingsRequestHelper.class)
@PowerMockIgnore({"org.apache.logging.log4j.*"})
public class ResourcesHoldingsIQServiceImplTest extends HoldingsIQServiceTestConfig {

  private ResourcesHoldingsIQService resourcesHoldingsIQService;


  @Before
  public void setUp() throws IOException {
    setUpStep();

    mockStatic(HoldingsRequestHelper.class);
    HoldingsResponseBodyListener listener = mock(HoldingsResponseBodyListener.class);
    // replace real logger with fake one to avoid calls to statusCode() method in the real logger
    // those calls are mocked inside tests, moreover their number is strictly defined. so any excessive calls
    // lead to test failures. this is a quick solution to the problem and should be revised later
    when(HoldingsRequestHelper.successBodyLogger()).thenReturn(listener);

    resourcesHoldingsIQService = new ResourcesHoldingsIQServiceImpl(HoldingsIQServiceImplTest.CONFIGURATION, mockVertx);
  }

  @After
  public void tearDown() {
    tearDownStep();
  }

  @Test
  public void testPostResources() {
    ResourceSelectedPayload resourceSelectedPayload = new ResourceSelectedPayload(false, "titleName",
      "pubType", STUB_BASE_URL);
    mockResponseForUpdateAndCreate(mockResponseBody, mockResponse, "{}", HttpStatus.SC_NO_CONTENT, HttpStatus.SC_OK);

    CompletableFuture<Title> completableFuture = resourcesHoldingsIQService.postResource(resourceSelectedPayload, resourceId);

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).putAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID
      + "/vendors/" + VENDOR_ID + "/packages/" + PACKAGE_ID + "/titles/" + TITLE_ID);
    verify(mockClient).getAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID
      + "/vendors/" + VENDOR_ID + "/packages/" + PACKAGE_ID + "/titles/" + TITLE_ID);
  }

  @Test
  public void testUpdateResources() {
    mockResponseForUpdateAndCreate(mockResponseBody, mockResponse, "{}", HttpStatus.SC_NO_CONTENT, HttpStatus.SC_OK);
    resourcesHoldingsIQService.updateResource(resourceId, resourcePut);

    verify(mockClient).putAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID
      + "/vendors/" + VENDOR_ID + "/packages/" + PACKAGE_ID + "/titles/" + TITLE_ID);
  }

  @Test
  public void testDeleteResource() {
    mockResponse(mockResponseBody, mockResponse, "{}", HttpStatus.SC_NO_CONTENT);
    CompletableFuture<Void> completableFuture = resourcesHoldingsIQService.deleteResource(resourceId);

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).putAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID
      + "/vendors/" + VENDOR_ID + "/packages/" + PACKAGE_ID + "/titles/" + TITLE_ID);
  }
}
