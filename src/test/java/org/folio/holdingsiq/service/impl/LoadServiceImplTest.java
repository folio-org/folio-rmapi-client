package org.folio.holdingsiq.service.impl;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutionException;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;

import org.folio.holdingsiq.model.TransactionId;
import org.folio.holdingsiq.service.LoadService;

public class LoadServiceImplTest extends HoldingsIQServiceTestConfig {

  private static final String PREVIOUS_TRANSACTION_ID = "abcd3ab0-da4b-4a1f-a004-a9d323e54cde";
  private static final String TRANSACTION_ID = "84113ab0-da4b-4a1f-a004-a9d686e54811";

  private static final String DELTA_ID = "7e3537a0-3f30-4ef8-9470-dd0a87ac1066";

  private LoadService service;

  @Before
  public void setUp() {
    service = new LoadServiceImpl(getConfiguration(), Vertx.vertx());
  }

  @Test
  public void testPostHoldings() {
    var urlPattern = new UrlPattern(equalTo("/rm/rmaccounts/" + STUB_CUSTOMER_ID + "/holdings"), false);
    wiremockServer.stubFor(
      post(urlPattern).willReturn(aResponse().withStatus(HttpStatus.SC_ACCEPTED).withBody("{}"))
    );
    var completableFuture = service.populateHoldings();

    assertTrue(isCompletedNormally(completableFuture));
    WireMock.verify(new RequestPatternBuilder(RequestMethod.POST, urlPattern));
  }

  @Test
  public void testPostHoldingsTransaction() throws ExecutionException, InterruptedException {
    TransactionId response = TransactionId.builder().transactionId(TRANSACTION_ID).build();
    var urlPattern =
      new UrlPattern(equalTo("/rm/rmaccounts/" + STUB_CUSTOMER_ID + "/reports/holdings?format=kbart2"), false);
    wiremockServer.stubFor(
      post(urlPattern).willReturn(aResponse().withStatus(HttpStatus.SC_ACCEPTED).withBody(Json.encode(response)))
    );
    var completableFuture = service.populateHoldingsTransaction();

    assertTrue(isCompletedNormally(completableFuture));
    assertEquals(TRANSACTION_ID, completableFuture.get().getTransactionId());
    WireMock.verify(new RequestPatternBuilder(RequestMethod.POST, urlPattern));
  }

  @Test
  public void testGetStatusHoldings() {
    var urlPattern = new UrlPattern(equalTo("/rm/rmaccounts/" + STUB_CUSTOMER_ID + "/holdings/status"), false);
    wiremockServer.stubFor(
      get(urlPattern).willReturn(aResponse().withStatus(HttpStatus.SC_OK).withBody("{}"))
    );
    var completableFuture = service.getLoadingStatus();

    assertTrue(isCompletedNormally(completableFuture));
    WireMock.verify(new RequestPatternBuilder(RequestMethod.GET, urlPattern));
  }

  @Test
  public void testGetTransactionStatus() {
    var urlPattern = new UrlPattern(
      equalTo("/rm/rmaccounts/" + STUB_CUSTOMER_ID + "/reports/holdings/transactions/" + TRANSACTION_ID + "/status"), false);
    wiremockServer.stubFor(
      get(urlPattern).willReturn(aResponse().withStatus(HttpStatus.SC_OK).withBody("{}"))
    );
    var completableFuture = service.getTransactionStatus(TRANSACTION_ID);

    assertTrue(isCompletedNormally(completableFuture));
    WireMock.verify(new RequestPatternBuilder(RequestMethod.GET, urlPattern));
  }

  @Test
  public void testGetTransactions() {
    var urlPattern = new UrlPattern(equalTo("/rm/rmaccounts/" + STUB_CUSTOMER_ID + "/reports/holdings/transactions"), false);
    wiremockServer.stubFor(
      get(urlPattern).willReturn(aResponse().withStatus(HttpStatus.SC_OK).withBody("{}"))
    );
    var completableFuture = service.getTransactions();

    assertTrue(isCompletedNormally(completableFuture));
    WireMock.verify(new RequestPatternBuilder(RequestMethod.GET, urlPattern));
  }

  @Test
  public void testGetHoldings() {
    var urlPattern = new UrlPattern(equalTo(
      "/rm/rmaccounts/" + STUB_CUSTOMER_ID + "/holdings?format=kbart2&count=" + COUNT_FOR_PARAM + "&offset="
        + PAGE_FOR_PARAM), false);
    wiremockServer.stubFor(
      get(urlPattern).willReturn(aResponse().withStatus(HttpStatus.SC_OK).withBody("{}"))
    );
    var completableFuture = service.loadHoldings(COUNT_FOR_PARAM, PAGE_FOR_PARAM);

    assertTrue(isCompletedNormally(completableFuture));
    WireMock.verify(new RequestPatternBuilder(RequestMethod.GET, urlPattern));
  }

  @Test
  public void testGetHoldingsTransaction() {
    var urlPattern = new UrlPattern(equalTo(
      "/rm/rmaccounts/" + STUB_CUSTOMER_ID + "/reports/holdings/transactions/" + TRANSACTION_ID + "?format=kbart2&count="
        + COUNT_FOR_PARAM + "&offset=" + PAGE_FOR_PARAM), false);
    wiremockServer.stubFor(
      get(urlPattern).willReturn(aResponse().withStatus(HttpStatus.SC_OK).withBody("{}"))
    );
    var completableFuture = service.loadHoldingsTransaction(TRANSACTION_ID, COUNT_FOR_PARAM, PAGE_FOR_PARAM);

    assertTrue(isCompletedNormally(completableFuture));
    WireMock.verify(new RequestPatternBuilder(RequestMethod.GET, urlPattern));
  }

  @Test
  public void testPostDeltaReport() throws ExecutionException, InterruptedException {
    var urlPattern = new UrlPattern(equalTo("/rm/rmaccounts/" + STUB_CUSTOMER_ID + "/reports/holdings/deltas"), false);
    wiremockServer.stubFor(
      post(urlPattern).willReturn(aResponse().withStatus(HttpStatus.SC_ACCEPTED).withBody(DELTA_ID))
    );
    var completableFuture = service.populateDeltaReport(TRANSACTION_ID, PREVIOUS_TRANSACTION_ID);

    assertTrue(isCompletedNormally(completableFuture));
    assertEquals(DELTA_ID, completableFuture.get());
    WireMock.verify(new RequestPatternBuilder(RequestMethod.POST, urlPattern));
  }

  @Test
  public void testGetDeltaReport() {
    var urlPattern = new UrlPattern(equalTo(
      "/rm/rmaccounts/" + STUB_CUSTOMER_ID + "/reports/holdings/deltas/" + DELTA_ID + "?format=kbart2&count="
        + COUNT_FOR_PARAM + "&offset=" + PAGE_FOR_PARAM), false);
    wiremockServer.stubFor(
      get(urlPattern).willReturn(aResponse().withStatus(HttpStatus.SC_OK).withBody("{}"))
    );
    var completableFuture = service.loadDeltaReport(DELTA_ID, COUNT_FOR_PARAM, PAGE_FOR_PARAM);

    assertTrue(isCompletedNormally(completableFuture));
    WireMock.verify(new RequestPatternBuilder(RequestMethod.GET, urlPattern));
  }

  @Test
  public void testGetDeltaReportStatus() {
    var urlPattern =
      new UrlPattern(equalTo("/rm/rmaccounts/" + STUB_CUSTOMER_ID + "/reports/holdings/deltas/" + DELTA_ID + "/status"),
        false);
    wiremockServer.stubFor(
      get(urlPattern).willReturn(aResponse().withStatus(HttpStatus.SC_OK).withBody("{}"))
    );
    var completableFuture = service.getDeltaReportStatus(DELTA_ID);

    assertTrue(isCompletedNormally(completableFuture));
    WireMock.verify(new RequestPatternBuilder(RequestMethod.GET, urlPattern));
  }
}
