package org.folio.holdingsiq.service.impl;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.Assert.assertTrue;

import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.folio.holdingsiq.model.RootProxyCustomLabels;
import org.folio.holdingsiq.service.HoldingsIQService;

public class HoldingsIQServiceImplTest extends HoldingsIQServiceTestConfig {

  private HoldingsIQService service;

  @Before
  public void setUp() {
    service = new HoldingsIQServiceImpl(getConfiguration(), Vertx.vertx());
  }

  @After
  public void tearDown() {
    wiremockServer.resetAll();
  }

  @Test
  public void testVerifyCredentials() {
    var urlPattern = new UrlPattern(equalTo("/rm/rmaccounts/" + STUB_CUSTOMER_ID + "/"), false);
    wiremockServer.stubFor(
      get(urlPattern).willReturn(aResponse().withStatus(HttpStatus.SC_OK).withBody("{}"))
    );
    var completableFuture = service.verifyCredentials();

    assertTrue(isCompletedNormally(completableFuture));
    verify(new RequestPatternBuilder(RequestMethod.GET, urlPattern));
  }

  @Test
  public void testRetrieveProxies() {
    var urlPattern = new UrlPattern(equalTo("/rm/rmaccounts/" + STUB_CUSTOMER_ID + "/proxies"), false);
    wiremockServer.stubFor(
      get(urlPattern).willReturn(aResponse().withStatus(HttpStatus.SC_OK).withBody("[]"))
    );
    var completableFuture = service.retrieveProxies();

    assertTrue(isCompletedNormally(completableFuture));
    verify(new RequestPatternBuilder(RequestMethod.GET, urlPattern));
  }

  @Test
  public void testUpdateRootProxyCustomLabels() {
    var rootProxyCustomLabels = RootProxyCustomLabels.builder().vendorId(String.valueOf(VENDOR_ID)).build();
    var urlPattern = new UrlPattern(equalTo("/rm/rmaccounts/" + STUB_CUSTOMER_ID + "/"), false);
    wiremockServer.stubFor(
      put(urlPattern).willReturn(aResponse().withStatus(HttpStatus.SC_NO_CONTENT))
    );
    wiremockServer.stubFor(
      get(urlPattern).willReturn(aResponse().withStatus(HttpStatus.SC_OK).withBody(Json.encode(rootProxyCustomLabels)))
    );
    var completableFuture = service.updateRootProxyCustomLabels(rootProxyCustomLabels);

    assertTrue(isCompletedNormally(completableFuture));

    verify(new RequestPatternBuilder(RequestMethod.GET, urlPattern));
    verify(new RequestPatternBuilder(RequestMethod.PUT, urlPattern));
  }
}
