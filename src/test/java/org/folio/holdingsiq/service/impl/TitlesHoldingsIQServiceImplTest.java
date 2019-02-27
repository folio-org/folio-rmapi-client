package org.folio.holdingsiq.service.impl;

import io.vertx.core.json.Json;
import org.apache.http.HttpStatus;
import org.folio.holdingsiq.model.Sort;
import org.folio.holdingsiq.model.Title;
import org.folio.holdingsiq.model.Titles;
import org.folio.holdingsiq.service.TitlesHoldingsIQService;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TitlesHoldingsIQServiceImplTest extends HoldingsIQServiceImplTest {

  private TitlesHoldingsIQService titlesHoldingsIQService =
    new TitlesHoldingsIQServiceImpl(HoldingsIQServiceImplTest.STUB_CUSTOMER_ID,
      HoldingsIQServiceImplTest.STUB_API_KEY, HoldingsIQServiceImplTest.STUB_BASE_URL, mockVertx);

  @Test
  public void testRetrieveTitles() throws IOException {
    mockResponse("{}", HttpStatus.SC_OK);
    when(Json.mapper.readValue(anyString(), any(Class.class))).thenReturn(titles);

    CompletableFuture<Titles> completableFuture = titlesHoldingsIQService.retrieveTitles(fqb.build(), Sort.NAME,
      PAGE_FOR_PARAM, COUNT_FOR_PARAM);

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).getAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID
      + "/titles?searchfield=titlename&selection=all&resourcetype=all&searchtype=" +
      "advanced&search=&offset=1&count=5&orderby=titlename");
  }

  @Test
  public void testRetrieveTitlesWithVendorId() throws IOException {
    mockResponse("{}", HttpStatus.SC_OK);
    when(Json.mapper.readValue(anyString(), any(Class.class))).thenReturn(titles);

    CompletableFuture<Titles> completableFuture = titlesHoldingsIQService.retrieveTitles(VENDOR_ID, PACKAGE_ID, fqb.build(),
      Sort.NAME, PAGE_FOR_PARAM, COUNT_FOR_PARAM);

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).getAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID
      + "/vendors/" + VENDOR_ID + "/packages/" + PACKAGE_ID + "/titles?searchfield=titlename&selection=all" +
      "&resourcetype=all&searchtype=advanced&search=&offset=1&count=5&orderby=titlename");
  }

  @Test
  public void testRetrieveTitle() {
    mockResponse("{}", HttpStatus.SC_OK);
    CompletableFuture<Title> completableFuture = titlesHoldingsIQService.retrieveTitle(TITLE_ID);

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).getAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID
      + "/titles/" + TITLE_ID);
  }

  @Test
  public void testPostTitle() throws IOException {
    mockResponseForUpdateAndCreate("{}", HttpStatus.SC_OK, HttpStatus.SC_OK);

    when(Json.mapper.readValue(anyString(), any(Class.class))).thenReturn(titleCreated);
    CompletableFuture<Title> completableFuture = titlesHoldingsIQService.postTitle(titlePost, packageId);

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).postAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID
      + "/vendors/" + VENDOR_ID + "/packages/" + PACKAGE_ID + "/titles");
    verify(mockClient).getAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID
      + "/titles/" + TITLE_ID);
  }
}
