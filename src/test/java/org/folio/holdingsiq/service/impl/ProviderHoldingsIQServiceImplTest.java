package org.folio.holdingsiq.service.impl;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;

import org.folio.holdingsiq.model.RootProxyCustomLabels;
import org.folio.holdingsiq.model.Sort;

public class ProviderHoldingsIQServiceImplTest extends HoldingsIQServiceTestConfig {

  private ProviderHoldingsIQServiceImpl service;

  @Before
  public void setUp() {
    var configuration = getConfiguration();
    var vertx = Vertx.vertx();
    service = new ProviderHoldingsIQServiceImpl(configuration, vertx);
  }

  @Test
  public void testGetVendorId() {
    var rootProxyCustomLabels = RootProxyCustomLabels.builder().vendorId(String.valueOf(VENDOR_ID)).build();
    var urlPattern = new UrlPattern(equalTo("/rm/rmaccounts/" + STUB_CUSTOMER_ID + "/"), false);
    wiremockServer.stubFor(
      get(urlPattern).willReturn(aResponse().withStatus(HttpStatus.SC_OK).withBody(Json.encode(rootProxyCustomLabels)))
    );
    var completableFuture = service.getVendorId();

    assertTrue(isCompletedNormally(completableFuture));
    verify(new RequestPatternBuilder(RequestMethod.GET, urlPattern));
  }

  @Test
  public void testRetrieveVendors() {
    var urlPattern = new UrlPattern(
      equalTo("/rm/rmaccounts/" + STUB_CUSTOMER_ID + "/vendors?search=Busket&offset=1&count=5&orderby=vendorname"), false);
    wiremockServer.stubFor(
      get(urlPattern).willReturn(aResponse().withStatus(HttpStatus.SC_OK).withBody("{}"))
    );
    var completableFuture = service.retrieveProviders("Busket", PAGE_FOR_PARAM, COUNT_FOR_PARAM, Sort.NAME);

    assertTrue(isCompletedNormally(completableFuture));
    verify(new RequestPatternBuilder(RequestMethod.GET, urlPattern));
  }

  @Test
  public void testRetrieveVendorsCompleteExceptionallyWhenRequestWithError404() {
    var urlPattern = new UrlPattern(
      equalTo("/rm/rmaccounts/" + STUB_CUSTOMER_ID + "/vendors?search=Busket&offset=1&count=5&orderby=vendorname"), false);
    wiremockServer.stubFor(
      get(urlPattern).willReturn(aResponse().withStatus(HttpStatus.SC_NOT_FOUND).withBody("{}"))
    );
    var completableFuture = service.retrieveProviders("Busket", PAGE_FOR_PARAM, COUNT_FOR_PARAM, Sort.NAME);

    assertFalse(isCompletedNormally(completableFuture));
    verify(new RequestPatternBuilder(RequestMethod.GET, urlPattern));
  }

  @Test
  public void testRetrieveVendorsCompleteExceptionallyWhenRequestWithError401() {
    var urlPattern = new UrlPattern(
      equalTo("/rm/rmaccounts/" + STUB_CUSTOMER_ID + "/vendors?search=Busket&offset=1&count=5&orderby=vendorname"), false);
    wiremockServer.stubFor(
      get(urlPattern).willReturn(aResponse().withStatus(HttpStatus.SC_UNAUTHORIZED).withBody("{}"))
    );
    var completableFuture = service.retrieveProviders("Busket", PAGE_FOR_PARAM, COUNT_FOR_PARAM, Sort.NAME);

    assertFalse(isCompletedNormally(completableFuture));
    verify(new RequestPatternBuilder(RequestMethod.GET, urlPattern));
  }

  @Test
  public void testRetrieveVendorsCompleteExceptionallyWhenThrowServiceException() {
    var urlPattern = new UrlPattern(
      equalTo("/rm/rmaccounts/" + STUB_CUSTOMER_ID + "/vendors?search=Busket&offset=1&count=5&orderby=vendorname"), false);
    wiremockServer.stubFor(
      get(urlPattern).willReturn(aResponse().withStatus(HttpStatus.SC_OK).withBody("invalid-json"))
    );
    var completableFuture = service.retrieveProviders("Busket", PAGE_FOR_PARAM, COUNT_FOR_PARAM, Sort.NAME);

    assertFalse(isCompletedNormally(completableFuture));
    verify(new RequestPatternBuilder(RequestMethod.GET, urlPattern));
  }

  @Test
  public void testUpdateVendor() {
    var urlPattern = new UrlPattern(equalTo("/rm/rmaccounts/" + STUB_CUSTOMER_ID + "/vendors/" + VENDOR_ID), false);
    wiremockServer.stubFor(
      put(urlPattern).willReturn(aResponse().withStatus(HttpStatus.SC_NO_CONTENT).withBody("{}"))
    );
    wiremockServer.stubFor(
      get(urlPattern).willReturn(aResponse().withStatus(HttpStatus.SC_OK).withBody("{}"))
    );
    var completableFuture = service.updateProvider(VENDOR_ID, vendorPut);

    assertTrue(isCompletedNormally(completableFuture));
    verify(new RequestPatternBuilder(RequestMethod.PUT, urlPattern));
    verify(new RequestPatternBuilder(RequestMethod.GET, urlPattern));
  }
}
