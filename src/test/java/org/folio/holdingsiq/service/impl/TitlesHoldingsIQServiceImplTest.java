package org.folio.holdingsiq.service.impl;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import static org.folio.holdingsiq.service.util.TestUtil.mockResponse;
import static org.folio.holdingsiq.service.util.TestUtil.mockResponseForUpdateAndCreate;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.core.JsonParser;
import io.vertx.core.json.Json;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.folio.holdingsiq.model.Sort;
import org.folio.holdingsiq.model.Title;
import org.folio.holdingsiq.model.Titles;
import org.folio.holdingsiq.service.TitlesHoldingsIQService;

public class TitlesHoldingsIQServiceImplTest extends HoldingsIQServiceTestConfig {

  private TitlesHoldingsIQService titlesHoldingsIQService =
    new TitlesHoldingsIQServiceImpl(HoldingsIQServiceImplTest.CONFIGURATION, mockVertx);

  @Before
  public void setUp() throws IOException {
    setUpStep();
  }

  @After
  public void tearDown() {
    tearDownStep();
  }

  @Test
  public void testRetrieveTitles() throws IOException {
    mockResponse(mockResponseBody, mockResponse, "{}", HttpStatus.SC_OK);
    doReturn(titles).when(Json.mapper).readValue(any(JsonParser.class), any(Class.class));

    CompletableFuture<Titles> completableFuture = titlesHoldingsIQService.retrieveTitles(filterQuery, Sort.NAME,
      PAGE_FOR_PARAM, COUNT_FOR_PARAM);

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).getAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID
      + "/titles?searchfield=titlename&selection=all&resourcetype=all&searchtype=" +
      "contains&search=&offset=1&count=5&orderby=titlename");
  }

  @Test
  public void testRetrieveTitlesWithVendorId() throws IOException {
    mockResponse(mockResponseBody, mockResponse, "{}", HttpStatus.SC_OK);
    doReturn(titles).when(Json.mapper).readValue(any(JsonParser.class), any(Class.class));

    CompletableFuture<Titles> completableFuture = titlesHoldingsIQService.retrieveTitles(VENDOR_ID, PACKAGE_ID, filterQuery,
      Sort.NAME, PAGE_FOR_PARAM, COUNT_FOR_PARAM);

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).getAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID
      + "/vendors/" + VENDOR_ID + "/packages/" + PACKAGE_ID + "/titles?searchfield=titlename&selection=all" +
      "&resourcetype=all&searchtype=contains&search=&offset=1&count=5&orderby=titlename");
  }

  @Test
  public void testRetrieveTitle() {
    mockResponse(mockResponseBody, mockResponse, "{}", HttpStatus.SC_OK);
    CompletableFuture<Title> completableFuture = titlesHoldingsIQService.retrieveTitle(TITLE_ID);

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).getAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID
      + "/titles/" + TITLE_ID);
  }

  @Test
  public void testPostTitle() throws IOException {
    mockResponseForUpdateAndCreate(mockResponseBody, mockResponse, "{}", HttpStatus.SC_OK, HttpStatus.SC_OK);

    doReturn(titleCreated).when(Json.mapper).readValue(any(JsonParser.class), any(Class.class));
    CompletableFuture<Title> completableFuture = titlesHoldingsIQService.postTitle(titlePost, packageId);

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).postAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID
      + "/vendors/" + VENDOR_ID + "/packages/" + PACKAGE_ID + "/titles");
    verify(mockClient).getAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID
      + "/titles/" + TITLE_ID);
  }
}
