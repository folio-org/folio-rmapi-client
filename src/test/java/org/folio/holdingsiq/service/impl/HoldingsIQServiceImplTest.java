package org.folio.holdingsiq.service.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.Answer;

import com.google.common.io.Files;

import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;

public class HoldingsIQServiceImplTest {
  private static final String STUB_CUSTOMER_ID = "TEST_CUSTOMER_ID";
  private static final String STUB_API_KEY = "test_key";
  private static final String STUB_BASE_URL = "https://sandbox.ebsco.io";

  private Vertx mockVertx = mock(Vertx.class);
  private HttpClient mockClient = mock(HttpClient.class);
  private HttpClientRequest mockRequest = mock(HttpClientRequest.class);
  private HttpClientResponse mockResponse = mock(HttpClientResponse.class);
  private Buffer mockResponseBody = mock(Buffer.class);
  private MultiMap stubHeaderMap = new CaseInsensitiveHeaders();
  private HoldingsIQServiceImpl service = new HoldingsIQServiceImpl(STUB_CUSTOMER_ID, STUB_API_KEY, STUB_BASE_URL, mockVertx);
  private ArgumentCaptor<String> url = ArgumentCaptor.forClass(String.class);

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() {
    ArgumentCaptor<Handler<HttpClientResponse>> requestHandler = ArgumentCaptor.forClass(Handler.class);
    ArgumentCaptor<Handler<Throwable>> exceptionHandler = ArgumentCaptor.forClass(Handler.class);

    when(mockVertx.createHttpClient()).thenReturn(mockClient);
    when(mockClient.getAbs(url.capture())).thenReturn(mockRequest);
    when(mockRequest.headers()).thenReturn(stubHeaderMap);
    when(mockRequest.handler(requestHandler.capture())).thenReturn(mockRequest);
    when(mockRequest.exceptionHandler(exceptionHandler.capture())).thenReturn(mockRequest);
    when(mockResponse.bodyHandler(any())).thenAnswer(callHandlerWithBody());
    doAnswer(callHandlerWithResponse(requestHandler)).when(mockRequest).end();
    doAnswer(callHandlerWithResponse(requestHandler)).when(mockRequest).end(anyString());
  }

  @Test
  public void testGetVendorId() throws IOException, URISyntaxException {
    String stubResponse = "responses/proxiescustomlabels/get-root-proxy-custom-labels-success-response.json";
    mockResponse(readFile(stubResponse), HttpStatus.SC_OK);
    CompletableFuture<Long> vendorId = service.getVendorId();
    assertEquals(STUB_BASE_URL + "/rm/rmaccounts/" + STUB_CUSTOMER_ID + "/", url.getValue());
    assertEquals(111111L, (long) vendorId.join());
  }

  private void mockResponse(String responseBody, int status, String statusMessage) {
    mockResponse(responseBody, status);
    when(mockResponse.statusMessage()).thenReturn(statusMessage);
  }

  private void mockResponse(String responseBody, int status) {
    when(mockResponse.statusCode()).thenReturn(status);
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

  /**
   * Reads file from classpath as String
   */
  private static String readFile(String filename) throws IOException, URISyntaxException {
    return Files.asCharSource(getFile(filename), StandardCharsets.UTF_8).read();
  }

  /**
   * Returns File object corresponding to the file on classpath with specified filename
   */
  private static File getFile(String filename) throws URISyntaxException {
    return new File(HoldingsIQServiceImplTest.class.getClassLoader()
      .getResource(filename).toURI());
  }
}
