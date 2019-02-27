package org.folio.holdingsiq.service.impl;

import org.apache.http.HttpStatus;
import org.folio.holdingsiq.model.ResourceSelectedPayload;
import org.folio.holdingsiq.model.Title;
import org.folio.holdingsiq.service.ResourcesHoldingsIQService;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

public class ResourcesHoldingsIQServiceImplTest extends HoldingsIQServiceImplTest {

  private ResourcesHoldingsIQService resourcesHoldingsIQService =
    new ResourcesHoldingsIQServiceImpl(HoldingsIQServiceImplTest.STUB_CUSTOMER_ID,
      HoldingsIQServiceImplTest.STUB_API_KEY, HoldingsIQServiceImplTest.STUB_BASE_URL, mockVertx);

  @Test
  public void testPostResources() {
    ResourceSelectedPayload resourceSelectedPayload = new ResourceSelectedPayload(false, "titleName",
      "pubType", STUB_BASE_URL);
    mockResponseForUpdateAndCreate("{}", HttpStatus.SC_NO_CONTENT, HttpStatus.SC_OK);

    CompletableFuture<Title> completableFuture = resourcesHoldingsIQService.postResource(resourceSelectedPayload, resourceId);

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).putAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID
      + "/vendors/" + VENDOR_ID + "/packages/" + PACKAGE_ID + "/titles/" + TITLE_ID);
    verify(mockClient).getAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID
      + "/vendors/" + VENDOR_ID + "/packages/" + PACKAGE_ID + "/titles/" + TITLE_ID);
  }

  @Test
  public void testUpdateResources() {
    mockResponseForUpdateAndCreate("{}", HttpStatus.SC_NO_CONTENT, HttpStatus.SC_OK);
    resourcesHoldingsIQService.updateResource(resourceId, resourcePut);

    verify(mockClient).putAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID
      + "/vendors/" + VENDOR_ID + "/packages/" + PACKAGE_ID + "/titles/" + TITLE_ID);
  }

  @Test
  public void testDeleteResource() {
    mockResponse("{}", HttpStatus.SC_NO_CONTENT);
    CompletableFuture<Void> completableFuture = resourcesHoldingsIQService.deleteResource(resourceId);

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).putAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID
      + "/vendors/" + VENDOR_ID + "/packages/" + PACKAGE_ID + "/titles/" + TITLE_ID);
  }
}
