package org.folio.holdingsiq.service.impl;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.Assert.assertTrue;

import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;

import org.folio.holdingsiq.model.PackagePut;
import org.folio.holdingsiq.model.Sort;
import org.folio.holdingsiq.service.PackagesHoldingsIQService;

public class PackagesHoldingsIQServiceImplTest extends HoldingsIQServiceTestConfig {

  private PackagesHoldingsIQService service;

  @Before
  public void setUp() {
    service = new PackagesHoldingsIQServiceImpl(getConfiguration(), Vertx.vertx());
  }

  @Test
  public void testRetrievePackages() {
    var urlPattern = new UrlPattern(equalTo("/rm/rmaccounts/" + STUB_CUSTOMER_ID + "/vendors/" + VENDOR_ID
      + "/packages?selection=orderedthroughebsco&contenttype=filterType&search=" +
      "Query&offset=1&count=5&orderby=packagename"), false);
    wiremockServer.stubFor(
      get(urlPattern).willReturn(aResponse().withStatus(HttpStatus.SC_OK).withBody("{}"))
    );
    var completableFuture = service.retrievePackages("orderedthroughebsco",
      "filterType", VENDOR_ID, "Query", PAGE_FOR_PARAM, COUNT_FOR_PARAM, Sort.NAME);

    assertTrue(isCompletedNormally(completableFuture));
    verify(new RequestPatternBuilder(RequestMethod.GET, urlPattern));
  }

  @Test
  public void testRetrievePackagesWithVendorId() {
    var urlPattern = new UrlPattern(equalTo("/rm/rmaccounts/" + STUB_CUSTOMER_ID + "/vendors/" + VENDOR_ID
      + "/packages?selection=all&contenttype=all&search=&offset=1"
      + "&count=25&orderby=packagename"), false);
    wiremockServer.stubFor(
      get(urlPattern).willReturn(aResponse().withStatus(HttpStatus.SC_OK).withBody("{}"))
    );
    var completableFuture = service.retrievePackages(VENDOR_ID);

    assertTrue(isCompletedNormally(completableFuture));
    verify(new RequestPatternBuilder(RequestMethod.GET, urlPattern));
  }

  @Test
  public void testRetrievePackage() {
    var urlPattern =
      new UrlPattern(equalTo("/rm/rmaccounts/" + STUB_CUSTOMER_ID + "/vendors/" + VENDOR_ID + "/packages/" + PACKAGE_ID),
        false);
    wiremockServer.stubFor(
      get(urlPattern).willReturn(aResponse().withStatus(HttpStatus.SC_OK).withBody(Json.encode(packageCreated)))
    );
    var completableFuture = service.retrievePackage(packageId);

    assertTrue(isCompletedNormally(completableFuture));
    verify(new RequestPatternBuilder(RequestMethod.GET, urlPattern));
  }

  @Test
  public void testUpdatePackage() {
    var urlPattern =
      new UrlPattern(equalTo("/rm/rmaccounts/" + STUB_CUSTOMER_ID + "/vendors/" + VENDOR_ID + "/packages/" + PACKAGE_ID),
        false);
    wiremockServer.stubFor(
      put(urlPattern).willReturn(aResponse().withStatus(HttpStatus.SC_NO_CONTENT))
    );
    var completableFuture = service.updatePackage(packageId, PackagePut.builder().build());

    assertTrue(isCompletedNormally(completableFuture));
    verify(new RequestPatternBuilder(RequestMethod.PUT, urlPattern));
  }

  @Test
  public void testDeletePackage() {
    var urlPattern =
      new UrlPattern(equalTo("/rm/rmaccounts/" + STUB_CUSTOMER_ID + "/vendors/" + VENDOR_ID + "/packages/" + PACKAGE_ID),
        false);
    wiremockServer.stubFor(
      put(urlPattern).willReturn(aResponse().withStatus(HttpStatus.SC_NO_CONTENT))
    );
    var completableFuture = service.deletePackage(packageId);

    assertTrue(isCompletedNormally(completableFuture));
    verify(new RequestPatternBuilder(RequestMethod.PUT, urlPattern));
  }

  @Test
  public void testPostPackage() {
    var urlPatternGet =
      new UrlPattern(equalTo("/rm/rmaccounts/" + STUB_CUSTOMER_ID + "/vendors/" + VENDOR_ID + "/packages/" + PACKAGE_ID),
        false);
    var urlPatternPost =
      new UrlPattern(equalTo("/rm/rmaccounts/" + STUB_CUSTOMER_ID + "/vendors/" + VENDOR_ID + "/packages"), false);
    wiremockServer.stubFor(
      post(urlPatternPost).willReturn(aResponse().withStatus(HttpStatus.SC_OK).withBody(Json.encode(packageCreated)))
    );
    wiremockServer.stubFor(
      get(urlPatternGet).willReturn(aResponse().withStatus(HttpStatus.SC_OK).withBody(Json.encode(packageCreated)))
    );
    var completableFuture = service.postPackage(packagePost, VENDOR_ID);

    assertTrue(isCompletedNormally(completableFuture));
    verify(new RequestPatternBuilder(RequestMethod.POST, urlPatternPost));
    verify(new RequestPatternBuilder(RequestMethod.GET, urlPatternGet));
  }
}
