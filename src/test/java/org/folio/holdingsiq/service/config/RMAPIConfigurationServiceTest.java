package org.folio.holdingsiq.service.config;

import static io.vertx.core.Future.succeededFuture;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.util.concurrent.CompletableFuture;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import org.folio.holdingsiq.model.Configuration;
import org.folio.holdingsiq.model.OkapiData;
import org.folio.holdingsiq.service.impl.ConfigurationServiceImpl;
import org.folio.rest.jaxrs.model.Config;

@RunWith(PowerMockRunner.class)
@PrepareForTest(WebClient.class)
@PowerMockIgnore({"org.apache.logging.log4j.*"})
// the above added due to the issue:
// https://github.com/powermock/powermock/issues/861
public class RMAPIConfigurationServiceTest {

  @Mock
  private Vertx vertx;
  @Mock
  private WebClient webClient;
  @Mock
  private HttpResponse<Buffer> httpResponse;
  @Mock
  private HttpRequest<Buffer> httpRequest;
  private ConfigurationServiceImpl service;

  @Before
  public void setUp() {
    initMocks(this);

    mockStatic(WebClient.class);
    when(WebClient.create(vertx)).thenReturn(webClient);

    service = new ConfigurationServiceImpl(vertx);
  }

  @Test
  public void shouldFailIfResponseIsNotOk() {
    when(webClient.get(anyInt(), any(), any())).thenReturn(httpRequest);

    when(httpRequest.putHeader(anyString(), (String) any())).thenReturn(httpRequest);
    when(httpRequest.expect(any())).thenReturn(httpRequest);
    doAnswer(httpResponseAnswer(httpResponse)).when(httpRequest).send(any());

    when(httpResponse.statusCode()).thenReturn(500);
    when(httpResponse.toString()).thenReturn("failure");

    OkapiData okapiData = mock(OkapiData.class);

    CompletableFuture<Configuration> result = service.retrieveConfiguration(okapiData);

    assertTrue(result.isCompletedExceptionally());
  }

  /*@Test
  public void shouldCompleteExceptionallyWhenRequestFails() throws Exception {
    HttpClientResponse response = mock(HttpClientResponseImpl.class);
    when(response.statusCode()).thenReturn(400);
    when(response.bodyHandler(any())).thenAnswer(new HandleBodyAnswer(new BufferImpl()));
    doAnswer(new HandleResponseAnswer(response, 5))
      .when(mockConfigurationsClient).getEntries(anyString(), anyInt(), anyInt(), any(), any(), any());
    CompletableFuture<Configuration> future = configurationService.retrieveConfiguration(OKAPI_DATA);
    assertTrue(future.isCompletedExceptionally());
  }

  @Test
  public void shouldCompleteExceptionallyWhenHttpClientThrowsException() throws Exception {
    doThrow(new UnsupportedEncodingException()).when(mockConfigurationsClient)
      .getEntries(anyString(), anyInt(), anyInt(), any(), any(), any());
    CompletableFuture<Configuration> future = configurationService.retrieveConfiguration(OKAPI_DATA);
    assertTrue(future.isCompletedExceptionally());
  }

  @Test
  public void shouldCompleteExceptionallyWhenDeleteRequestFails() throws Exception {
    HttpClientResponse getResponse = mock(HttpClientResponseImpl.class);
    HttpClientResponse deleteResponse = mock(HttpClientResponseImpl.class);

    Configs configs = new Configs().withConfigs(Arrays.asList(
      createConfig("kb.ebsco.url"),
      createConfig("kb.ebsco.customerId"),
      createConfig("kb.ebsco.apiKey")
    ));

    ObjectMapper mapper = new ObjectMapper();
    BufferImpl buffer = new BufferImpl();
    buffer.appendString(mapper.writeValueAsString(configs));
    when(getResponse.statusCode()).thenReturn(200);
    when(deleteResponse.statusCode()).thenReturn(400);
    when(getResponse.bodyHandler(any())).thenAnswer(new HandleBodyAnswer(buffer));
    when(deleteResponse.bodyHandler(any())).thenAnswer(new HandleBodyAnswer(new BufferImpl()));

    doAnswer(new HandleResponseAnswer(getResponse, 5))
      .when(mockConfigurationsClient).getEntries(anyString(), anyInt(), anyInt(), any(), any(), any());
    doAnswer(new HandleResponseAnswer(deleteResponse, 2))
      .when(mockConfigurationsClient).deleteEntryId(any(), any(), any());

    CompletableFuture<Configuration> future = configurationService.updateConfiguration(Configuration.builder().build(), OKAPI_DATA);
    assertTrue(future.isCompletedExceptionally());
  }*/

  private static <T> GenericHandlerAnswer<AsyncResult<HttpResponse<T>>, Void> httpResponseAnswer(
      HttpResponse<T> httpResponse) {
    AsyncResult<HttpResponse<T>> res = succeededFuture(httpResponse);
    return new GenericHandlerAnswer<>(res, 0);
  }

  private Config createConfig(String code) {
    return new Config()
      .withModule("EKB")
      .withConfigName("api_access")
      .withCode(code)
      .withDescription("description")
      .withEnabled(true)
      .withValue("value");
  }

  private class HandleBodyAnswer implements Answer<Object> {
    private Buffer body;

    HandleBodyAnswer(Buffer body) {
      this.body = body;
    }

    @Override
    public Object answer(InvocationOnMock invocation) {
      ((Handler<Buffer>) invocation.getArgument(0)).handle(body);
      return invocation.getMock();
    }
  }

  private class HandleResponseAnswer implements Answer<Object> {
    private HttpClientResponse response;
    private int handlerArgumentIndex;

    HandleResponseAnswer(HttpClientResponse response, int handlerArgumentIndex) {
      this.response = response;
      this.handlerArgumentIndex = handlerArgumentIndex;
    }

    @Override
    public Object answer(InvocationOnMock invocation) {
      ((Handler<HttpClientResponse>) invocation.getArgument(handlerArgumentIndex)).handle(response);
      return null;
    }
  }

  static class GenericHandlerAnswer<H, R> implements Answer<R> {

    private final H handlerResult;
    private final int argumentIndex;
    private R returnResult;

    public GenericHandlerAnswer(H handlerResult, int handlerArgumentIndex) {
      this.handlerResult = handlerResult;
      this.argumentIndex = handlerArgumentIndex;
    }

    public GenericHandlerAnswer(H handlerResult, int handlerArgumentIndex, R returnResult) {
      this(handlerResult, handlerArgumentIndex);
      this.returnResult = returnResult;
    }

    @Override
    public R answer(InvocationOnMock invocation) {
      Handler<H> handler = invocation.getArgument(argumentIndex);
      handler.handle(handlerResult);
      return returnResult;
    }
  }
}
