package org.folio.holdingsiq.service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.buffer.impl.BufferImpl;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.impl.HttpClientResponseImpl;
import org.folio.holdingsiq.model.Configuration;
import org.folio.holdingsiq.model.OkapiData;
import org.folio.holdingsiq.service.impl.ConfigurationClientProvider;
import org.folio.holdingsiq.service.impl.ConfigurationServiceImpl;
import org.folio.rest.client.ConfigurationsClient;
import org.folio.rest.jaxrs.model.Config;
import org.folio.rest.jaxrs.model.Configs;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class RMAPIConfigurationServiceTest {

  private static final String OKAPI_TOKEN_HEADER = "x-okapi-token";
  private static final String OKAPI_URL_HEADER = "x-okapi-url";
  private static final String OKAPI_TENANT_HEADER = "x-okapi-tenant";

  private static final OkapiData OKAPI_DATA = new OkapiData(ImmutableMap.of(
    OKAPI_TOKEN_HEADER, "token",
    OKAPI_TENANT_HEADER, "tenant",
    OKAPI_URL_HEADER, "https://localhost:8080"));
  private ConfigurationClientProvider configurationClientProvider = mock(ConfigurationClientProvider.class);
  private ConfigurationsClient mockConfigurationsClient = mock(ConfigurationsClient.class);
  private ConfigurationServiceImpl configurationService = new ConfigurationServiceImpl(configurationClientProvider);

  @Before
  public void setUp() {
    when(configurationClientProvider.createClient(anyString(), anyInt(), anyString(), anyString())).thenReturn(mockConfigurationsClient);
  }

  @Test
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
}
