package org.folio.holdingsiq.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.json.Json;
import org.apache.http.HttpStatus;
import org.folio.holdingsiq.model.*;
import org.folio.holdingsiq.service.exception.ServiceException;
import org.folio.holdingsiq.service.impl.HoldingsIQServiceImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class HoldingsIQServiceImplTest {
  private static final String STUB_CUSTOMER_ID = "TEST_CUSTOMER_ID";
  private static final String STUB_API_KEY = "test_key";
  private static final String STUB_BASE_URL = "https://sandbox.ebsco.io";
  private static final String DUMMY_RESPONSE_BODY = "{}";
  private static final int PAGE_FOR_PARAM = 1;
  private static final int COUNT_FOR_PARAM = 5;
  private static final Long PACKAGE_ID = 2222L;
  private static final Long TITLE_ID = 3333L;
  private static final Long VENDOR_ID = 5555L;

  private Vertx mockVertx = mock(Vertx.class);
  private HttpClient mockClient = mock(HttpClient.class);
  private HttpClientRequest mockRequest = mock(HttpClientRequest.class);
  private HttpClientResponse mockResponse = mock(HttpClientResponse.class);
  private Buffer mockResponseBody = mock(Buffer.class);
  private MultiMap stubHeaderMap = new CaseInsensitiveHeaders();
  private HoldingsIQServiceImpl service = new HoldingsIQServiceImpl(STUB_CUSTOMER_ID, STUB_API_KEY, STUB_BASE_URL, mockVertx);
  private ArgumentCaptor<String> url = ArgumentCaptor.forClass(String.class);

  private ObjectMapper savedPrettyMapper;
  private ObjectMapper savedMapper;
  private FilterQuery.FilterQueryBuilder fqb = FilterQuery.builder();
  private VendorPut vendorPut = VendorPut.builder().build();
  private ResourcePut resourcePut = ResourcePut.builder().build();
  private PackagePost packagePost = PackagePost.builder().build();
  private TitlePost titlePost = TitlePost.builder().build();

  private TitleCreated titleCreated = TitleCreated.builder().titleId(TITLE_ID).build();
  private PackageCreated packageCreated = PackageCreated.builder().packageId(PACKAGE_ID).build();
  private Titles titles = Titles.builder().titleList(Collections.emptyList()).build();
  private RootProxyCustomLabels rootProxyCustomLabels = RootProxyCustomLabels.builder().vendorId(String.valueOf(VENDOR_ID)).build();
  private PackageId packageId = PackageId.builder().providerIdPart(VENDOR_ID).packageIdPart(PACKAGE_ID).build();
  private ResourceId resourceId = ResourceId.builder().providerIdPart(VENDOR_ID)
    .packageIdPart(PACKAGE_ID).titleIdPart(TITLE_ID).build();

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() throws IOException {
    ArgumentCaptor<Handler<HttpClientResponse>> requestHandler = ArgumentCaptor.forClass(Handler.class);
    ArgumentCaptor<Handler<Throwable>> exceptionHandler = ArgumentCaptor.forClass(Handler.class);

    when(mockVertx.createHttpClient()).thenReturn(mockClient);
    when(mockClient.getAbs(url.capture())).thenReturn(mockRequest);
    when(mockClient.putAbs(url.capture())).thenReturn(mockRequest);
    when(mockClient.postAbs(url.capture())).thenReturn(mockRequest);
    when(mockRequest.headers()).thenReturn(stubHeaderMap);
    when(mockRequest.handler(requestHandler.capture())).thenReturn(mockRequest);
    when(mockRequest.exceptionHandler(exceptionHandler.capture())).thenReturn(mockRequest);
    when(mockResponse.bodyHandler(any())).thenAnswer(callHandlerWithBody());
    doAnswer(callHandlerWithResponse(requestHandler)).when(mockRequest).end();
    doAnswer(callHandlerWithResponse(requestHandler)).when(mockRequest).end(anyString());

    //Save mappers to restore them later
    savedMapper = Json.mapper;
    savedPrettyMapper = Json.prettyMapper;
    Json.prettyMapper = mock(ObjectMapper.class);
    Json.mapper = mock(ObjectMapper.class);
    when(Json.prettyMapper.writeValueAsString(any())).thenReturn(DUMMY_RESPONSE_BODY);
  }

  @After
  public void tearDown() {
    Json.mapper = savedMapper;
    Json.prettyMapper = savedPrettyMapper;
  }

  @Test
  public void testVerifyCredentials() {
    mockResponse("{}", HttpStatus.SC_OK);
    CompletableFuture<Object> completableFuture = service.verifyCredentials();

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).getAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID
      + "/vendors?search=zz12&offset=1&orderby=vendorname&count=1");
  }

  @Test
  public void testGetVendorId() throws IOException {
    mockResponse("{}", HttpStatus.SC_OK);
    when(Json.mapper.readValue(anyString(), any(Class.class))).thenReturn(rootProxyCustomLabels);

    CompletableFuture<Long> completableFuture = service.getVendorId();

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).getAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID + "/");
  }

  @Test
  public void testRetrieveVendors() {
    mockResponse("{}", HttpStatus.SC_OK);
    CompletableFuture<Vendors> completableFuture = service.retrieveProviders("Busket", PAGE_FOR_PARAM,
      COUNT_FOR_PARAM, Sort.NAME);

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).getAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID
      + "/vendors?search=Busket&offset=1&count=5&orderby=vendorname");
  }

  @Test
  public void testRetrieveVendorsCompleteExceptionallyWhenRequestWithError404() {
    mockResponse("{}", HttpStatus.SC_NOT_FOUND, "Error 404. Not faund");
    CompletableFuture<Vendors> future = service.retrieveProviders("Busket", PAGE_FOR_PARAM, COUNT_FOR_PARAM, Sort.NAME);
    assertTrue(future.isCompletedExceptionally());
  }

  @Test
  public void testRetrieveVendorsCompleteExceptionallyWhenRequestWithError401() {
    mockResponse("{}", HttpStatus.SC_UNAUTHORIZED, "Error 401 unauthorized");
    CompletableFuture<Vendors> future = service.retrieveProviders("Busket", PAGE_FOR_PARAM, COUNT_FOR_PARAM, Sort.NAME);
    assertTrue(future.isCompletedExceptionally());
  }

  @Test
  public void testRetrieveVendorsCompleteExceptionallyWhenThrowServiceException() {
    mockResponse("{}", HttpStatus.SC_OK);
    when(Json.decodeValue(anyString(), any(Class.class))).thenThrow(ServiceException.class);

    CompletableFuture<Vendors> future = service.retrieveProviders("Busket", PAGE_FOR_PARAM, COUNT_FOR_PARAM, Sort.NAME);

    assertTrue(future.isCompletedExceptionally());
  }

  @Test
  public void testRetrieveTitles() throws IOException {
    mockResponse("{}", HttpStatus.SC_OK);
    when(Json.mapper.readValue(anyString(), any(Class.class))).thenReturn(titles);

    CompletableFuture<Titles> completableFuture = service.retrieveTitles(fqb.build(), Sort.NAME,
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

    CompletableFuture<Titles> completableFuture = service.retrieveTitles(VENDOR_ID, PACKAGE_ID, fqb.build(),
      Sort.NAME, PAGE_FOR_PARAM, COUNT_FOR_PARAM);

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).getAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID
      + "/vendors/" + VENDOR_ID + "/packages/" + PACKAGE_ID + "/titles?searchfield=titlename&selection=all" +
      "&resourcetype=all&searchtype=advanced&search=&offset=1&count=5&orderby=titlename");
  }

  @Test
  public void testRetrievePackages() {
    mockResponse("{}", HttpStatus.SC_OK);
    CompletableFuture<Packages> completableFuture = service.retrievePackages("ebsco",
      "filterType", VENDOR_ID, "Query", PAGE_FOR_PARAM, COUNT_FOR_PARAM, Sort.NAME);

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).getAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID
      + "/vendors/" + VENDOR_ID + "/packages?selection=orderedthroughebsco&contenttype=filterType&search=" +
      "Query&offset=1&count=5&orderby=packagename");
  }

  @Test
  public void testRetrievePackagesWithVendorId() {
    mockResponse("{}", HttpStatus.SC_OK);
    CompletableFuture<Packages> completableFuture = service.retrievePackages(VENDOR_ID);

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).getAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID
      + "/vendors/" + VENDOR_ID + "/packages?selection=all&contenttype=all&search=&offset=1"
      + "&count=25&orderby=packagename");
  }

  @Test
  public void testUpdateVendor() {
    mockResponseForUpdateAndCreate("{}", HttpStatus.SC_NO_CONTENT, HttpStatus.SC_OK);
    CompletableFuture<VendorById> completableFuture = service.updateProvider(VENDOR_ID, vendorPut);

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).putAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID
      + "/vendors/" + VENDOR_ID);
  }

  @Test
  public void testRetrievePackage() {
    mockResponse("{}", HttpStatus.SC_OK);
    CompletableFuture<PackageByIdData> completableFuture = service.retrievePackage(packageId);

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).getAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID
      + "/vendors/" + VENDOR_ID + "/packages/" + PACKAGE_ID);
  }

  @Test
  public void testUpdatePackage() {
    mockResponseForUpdateAndCreate("{}", HttpStatus.SC_NO_CONTENT, HttpStatus.SC_OK);
    service.updatePackage(packageId, PackagePut.builder().build());

    verify(mockClient).putAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID
      + "/vendors/" + VENDOR_ID + "/packages/" + PACKAGE_ID);
  }

  @Test
  public void testDeletePackage() {
    mockResponse("{}", HttpStatus.SC_NO_CONTENT);
    CompletableFuture<Void> completableFuture = service.deletePackage(packageId);

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).putAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID
      + "/vendors/" + VENDOR_ID + "/packages/" + PACKAGE_ID);
  }

  @Test
  public void testRetrieveProxies() {
    mockResponse("{}", HttpStatus.SC_OK);
    CompletableFuture<Proxies> completableFuture = service.retrieveProxies();

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).getAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID
      + "/proxies");
  }

  @Test
  public void testUpdateRootProxyCustomLabels() {
    mockResponseForUpdateAndCreate("{}", HttpStatus.SC_NO_CONTENT, HttpStatus.SC_OK);

    CompletableFuture<RootProxyCustomLabels> completableFuture =
      service.updateRootProxyCustomLabels(rootProxyCustomLabels);

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).putAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID + "/");
    verify(mockClient).getAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID + "/");
  }

  @Test
  public void testRetrieveTitle() {
    mockResponse("{}", HttpStatus.SC_OK);
    CompletableFuture<Title> completableFuture = service.retrieveTitle(TITLE_ID);

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).getAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID
      + "/titles/" + TITLE_ID);
  }

  @Test
  public void testPostResources() {
    ResourceSelectedPayload resourceSelectedPayload = new ResourceSelectedPayload(false, "titleName",
      "pubType", STUB_BASE_URL);
    mockResponseForUpdateAndCreate("{}", HttpStatus.SC_NO_CONTENT, HttpStatus.SC_OK);

    CompletableFuture<Title> completableFuture = service.postResource(resourceSelectedPayload, resourceId);

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).putAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID
      + "/vendors/" + VENDOR_ID + "/packages/" + PACKAGE_ID + "/titles/" + TITLE_ID);
    verify(mockClient).getAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID
      + "/vendors/" + VENDOR_ID + "/packages/" + PACKAGE_ID + "/titles/" + TITLE_ID);
  }

  @Test
  public void testPostPackage() throws IOException {
    mockResponse("{}", HttpStatus.SC_OK);

    when(Json.mapper.readValue(anyString(), any(Class.class))).thenReturn(packageCreated);
    CompletableFuture<PackageByIdData> completableFuture = service.postPackage(packagePost, VENDOR_ID);

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).postAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID
      + "/vendors/" + VENDOR_ID + "/packages");
    verify(mockClient).getAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID
      + "/vendors/" + VENDOR_ID + "/packages/" + PACKAGE_ID);
  }

  @Test
  public void testPostTitle() throws IOException {
    mockResponseForUpdateAndCreate("{}", HttpStatus.SC_OK, HttpStatus.SC_OK);

    when(Json.mapper.readValue(anyString(), any(Class.class))).thenReturn(titleCreated);
    CompletableFuture<Title> completableFuture = service.postTitle(titlePost, packageId);

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).postAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID
      + "/vendors/" + VENDOR_ID + "/packages/" + PACKAGE_ID + "/titles");
    verify(mockClient).getAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID
      + "/titles/" + TITLE_ID);
  }

  @Test
  public void testUpdateResources() {
    mockResponseForUpdateAndCreate("{}", HttpStatus.SC_NO_CONTENT, HttpStatus.SC_OK);
    service.updateResource(resourceId, resourcePut);

    verify(mockClient).putAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID
      + "/vendors/" + VENDOR_ID + "/packages/" + PACKAGE_ID + "/titles/" + TITLE_ID);
  }

  @Test
  public void testDeleteResource() {
    mockResponse("{}", HttpStatus.SC_NO_CONTENT);
    CompletableFuture<Void> completableFuture = service.deleteResource(resourceId);

    assertTrue(isCompletedNormally(completableFuture));
    verify(mockClient).putAbs(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID
      + "/vendors/" + VENDOR_ID + "/packages/" + PACKAGE_ID + "/titles/" + TITLE_ID);
  }

  private void mockResponse(String responseBody, int status, String statusMessage) {
    mockResponse(responseBody, status);
    when(mockResponse.statusMessage()).thenReturn(statusMessage);
  }

  private void mockResponse(String responseBody, int status) {
    when(mockResponse.statusCode()).thenReturn(status);
    when(mockResponseBody.toString()).thenReturn(responseBody);
  }

  private void mockResponseForUpdateAndCreate(String responseBody, int firstStatus, int secondStatus) {
    when(mockResponse.statusCode()).thenReturn(firstStatus).thenReturn(secondStatus);
    when(mockResponseBody.toString()).thenReturn(responseBody);
  }

  private Answer<Object> callHandlerWithBody() {
    return invocation -> {
      Handler<Buffer> handler = invocation.getArgument(0);
      handler.handle(mockResponseBody);
      return mockResponse;
    };
  }

  private Answer callHandlerWithResponse(ArgumentCaptor<Handler<HttpClientResponse>> requestHandler) {
    return invocation -> {
      requestHandler.getValue().handle(mockResponse);
      return null;
    };
  }

  private boolean isCompletedNormally(CompletableFuture completableFuture) {
    return completableFuture.isDone() && !completableFuture.isCompletedExceptionally() && !completableFuture.isCancelled();
  }
}
