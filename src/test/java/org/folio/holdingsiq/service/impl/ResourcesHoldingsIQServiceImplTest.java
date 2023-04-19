package org.folio.holdingsiq.service.impl;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static org.folio.holdingsiq.service.util.DataUtils.getResourceBody;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import io.vertx.core.Vertx;
import lombok.SneakyThrows;
import org.apache.http.HttpStatus;
import org.folio.holdingsiq.model.ResourceSelectedPayload;
import org.folio.holdingsiq.service.ResourcesHoldingsIQService;
import org.junit.Before;
import org.junit.Test;

public class ResourcesHoldingsIQServiceImplTest extends HoldingsIQServiceTestConfig {

  private final static String URL = "/rm/rmaccounts/" + STUB_CUSTOMER_ID + "/vendors/" +
    VENDOR_ID + "/packages/" + PACKAGE_ID + "/titles/" + TITLE_ID;
  private ResourcesHoldingsIQService service;


  @Before
  public void setUp() {
    service = new ResourcesHoldingsIQServiceImpl(getConfiguration(), Vertx.vertx());
  }

  @Test
  public void testPostResources() {
    ResourceSelectedPayload resourceSelectedPayload = new ResourceSelectedPayload(false, "titleName",
      "pubType", STUB_BASE_URL);
    var urlPattern = new UrlPattern(equalTo(URL), false);
    wiremockServer.stubFor(
      put(urlPattern).willReturn(aResponse().withStatus(HttpStatus.SC_NO_CONTENT))
    );
    wiremockServer.stubFor(
      get(urlPattern).willReturn(aResponse().withStatus(HttpStatus.SC_OK).withBody("{}"))
    );
    var completableFuture = service.postResource(resourceSelectedPayload, resourceId);

    assertTrue(isCompletedNormally(completableFuture));
    WireMock.verify(new RequestPatternBuilder(RequestMethod.PUT, urlPattern));
    WireMock.verify(new RequestPatternBuilder(RequestMethod.GET, urlPattern));
  }

  @Test
  @SneakyThrows
  public void testRetrieveResources() {
    var urlPattern = new UrlPattern(equalTo(URL), false);

    wiremockServer.stubFor(get(urlPattern)
      .willReturn(aResponse().withStatus(HttpStatus.SC_OK)
        .withBody(getResourceBody())));

    var completableFuture = service.retrieveResource(resourceId);
    assertTrue(isCompletedNormally(completableFuture));
    assertNotNull(completableFuture.get().getCustomerResourcesList().get(0).getProxy().getProxiedUrl());
    WireMock.verify(new RequestPatternBuilder(RequestMethod.GET, urlPattern));
  }

  @Test
  public void testUpdateResources() {
    var urlPattern = new UrlPattern(equalTo(URL), false);
    wiremockServer.stubFor(
      put(urlPattern).willReturn(aResponse().withStatus(HttpStatus.SC_NO_CONTENT))
    );
    var completableFuture = service.updateResource(resourceId, resourcePut);

    assertTrue(isCompletedNormally(completableFuture));
    WireMock.verify(new RequestPatternBuilder(RequestMethod.PUT, urlPattern));
  }

  @Test
  public void testDeleteResource() {
    var urlPattern = new UrlPattern(equalTo(URL), false);
    wiremockServer.stubFor(
      put(urlPattern).willReturn(aResponse().withStatus(HttpStatus.SC_NO_CONTENT))
    );
    var completableFuture = service.deleteResource(resourceId);

    assertTrue(isCompletedNormally(completableFuture));
    WireMock.verify(new RequestPatternBuilder(RequestMethod.PUT, urlPattern));
  }
}
