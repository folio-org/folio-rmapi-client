package org.folio.holdingsiq.service.impl;

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
import org.folio.holdingsiq.model.*;
import org.folio.holdingsiq.service.HoldingsIQService;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class HoldingsIQServiceTestConfig {
  protected static final String STUB_CUSTOMER_ID = "TEST_CUSTOMER_ID";
  protected static final String STUB_API_KEY = "test_key";
  protected static final String STUB_BASE_URL = "https://sandbox.ebsco.io";
  protected static final String DUMMY_RESPONSE_BODY = "{}";
  protected static final int PAGE_FOR_PARAM = 1;
  protected static final int COUNT_FOR_PARAM = 5;
  protected static final Long PACKAGE_ID = 2222L;
  protected static final Long TITLE_ID = 3333L;
  protected static final Long VENDOR_ID = 5555L;

  protected Vertx mockVertx = mock(Vertx.class);
  protected HttpClient mockClient = mock(HttpClient.class);
  protected HttpClientRequest mockRequest = mock(HttpClientRequest.class);
  protected HttpClientResponse mockResponse = mock(HttpClientResponse.class);
  protected Buffer mockResponseBody = mock(Buffer.class);
  protected MultiMap stubHeaderMap = new CaseInsensitiveHeaders();
  protected HoldingsIQService service = new HoldingsIQServiceImpl(STUB_CUSTOMER_ID, STUB_API_KEY, STUB_BASE_URL, mockVertx);
  protected ArgumentCaptor<String> url = ArgumentCaptor.forClass(String.class);

  protected ObjectMapper savedPrettyMapper;
  protected ObjectMapper savedMapper;
  protected FilterQuery filterQuery = FilterQuery.builder().build();
  protected VendorPut vendorPut = VendorPut.builder().build();
  protected ResourcePut resourcePut = ResourcePut.builder().build();
  protected PackagePost packagePost = PackagePost.builder().build();
  protected TitlePost titlePost = TitlePost.builder().build();

  protected TitleCreated titleCreated = TitleCreated.builder().titleId(TITLE_ID).build();
  protected PackageCreated packageCreated = PackageCreated.builder().packageId(PACKAGE_ID).build();
  protected Titles titles = Titles.builder().titleList(Collections.emptyList()).build();
  protected RootProxyCustomLabels rootProxyCustomLabels = RootProxyCustomLabels.builder().vendorId(String.valueOf(VENDOR_ID)).build();
  protected PackageId packageId = PackageId.builder().providerIdPart(VENDOR_ID).packageIdPart(PACKAGE_ID).build();
  protected ResourceId resourceId = ResourceId.builder().providerIdPart(VENDOR_ID)
    .packageIdPart(PACKAGE_ID).titleIdPart(TITLE_ID).build();

  protected void setUpStep() throws IOException {
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

  protected void tearDownStep() {
    Json.mapper = savedMapper;
    Json.prettyMapper = savedPrettyMapper;
  }

  protected boolean isCompletedNormally(CompletableFuture completableFuture) {
    return completableFuture.isDone() && !completableFuture.isCompletedExceptionally() && !completableFuture.isCancelled();
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
}
