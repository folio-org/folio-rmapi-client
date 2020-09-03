package org.folio.holdingsiq.service.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.folio.holdingsiq.service.config.ConfigTestData.OKAPI_DATA;
import static org.folio.holdingsiq.service.util.TestUtil.mockResponse;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import io.vertx.core.Context;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import org.folio.holdingsiq.model.Configuration;
import org.folio.holdingsiq.model.ConfigurationError;
import org.folio.holdingsiq.service.ConfigurationService;
import org.folio.holdingsiq.service.exception.ServiceResponseException;
import org.folio.holdingsiq.service.impl.ConfigurationServiceImpl;
import org.folio.holdingsiq.service.impl.HoldingsIQServiceTestConfig;

public class ConfigurationServiceImplTest extends HoldingsIQServiceTestConfig {

  private static final Configuration STUB_CONFIGURATION = Configuration.builder()
    .apiKey(STUB_API_KEY).customerId(STUB_CUSTOMER_ID).url(STUB_BASE_URL).build();

  @Mock
  private final Context context = mock(Context.class);
  private final ConfigurationService configService = new ConfigurationServiceImpl(mockVertx);

  @Before
  public void setUp() throws IOException {
    setUpStep();
    when(context.owner()).thenReturn(mockVertx);
  }

  @After
  public void tearDown() {
    tearDownStep();
  }

  @Test
  public void shouldNotReturnErrorsOnVerifyWhenCredentialsAreValid() throws ExecutionException, InterruptedException {
    mockResponse(mockResponseBody, mockResponse, "{}", HttpStatus.SC_OK);

    CompletableFuture<List<ConfigurationError>>
      completableFuture = configService.verifyCredentials(STUB_CONFIGURATION, context, OKAPI_DATA);

    assertTrue(isCompletedNormally(completableFuture));
    assertThat(completableFuture.get(), empty());
  }

  @Test
  public void shouldReturnConfigurationErrorOnVerifyWhenCredentialsAreInvalidWithCode401()
    throws ExecutionException, InterruptedException {
    mockResponse(mockResponseBody, mockResponse, "{}", HttpStatus.SC_UNAUTHORIZED);

    CompletableFuture<List<ConfigurationError>>
      completableFuture = configService.verifyCredentials(STUB_CONFIGURATION, context, OKAPI_DATA);

    assertTrue(isCompletedNormally(completableFuture));
    List<ConfigurationError> configurationErrors = completableFuture.get();
    assertThat(configurationErrors, hasSize(1));
    assertThat(configurationErrors.get(0), instanceOf(ConfigurationError.class));
  }

  @Test
  public void shouldReturnConfigurationErrorOnVerifyWhenCredentialsAreInvalidWithCode403()
    throws ExecutionException, InterruptedException {
    mockResponse(mockResponseBody, mockResponse, "{}", HttpStatus.SC_FORBIDDEN);

    CompletableFuture<List<ConfigurationError>>
      completableFuture = configService.verifyCredentials(STUB_CONFIGURATION, context, OKAPI_DATA);

    assertTrue(isCompletedNormally(completableFuture));
    List<ConfigurationError> configurationErrors = completableFuture.get();
    assertThat(configurationErrors, hasSize(1));
    assertThat(configurationErrors.get(0), instanceOf(ConfigurationError.class));
  }

  @Test
  public void shouldFailedOnVerifyWhenCredentialsAreValidWithCode429() {
    mockResponse(mockResponseBody, mockResponse, "{\n"
      + "  \"Errors\": [\n"
      + "    {\n"
      + "      \"Code\": 1010,\n"
      + "      \"Message\": \"Too Many Requests.\",\n"
      + "      \"SubCode\": 0\n"
      + "    }\n"
      + "  ]\n"
      + "}", 429);

    CompletableFuture<List<ConfigurationError>>
      completableFuture = configService.verifyCredentials(STUB_CONFIGURATION, context, OKAPI_DATA);

    assertFalse(isCompletedNormally(completableFuture));
    try {
      completableFuture.join();
    } catch (Exception throwable) {
      Throwable cause = throwable.getCause();
      assertThat(cause, instanceOf(ServiceResponseException.class));
      assertThat(((ServiceResponseException) cause).getCode(), equalTo(429));
      assertThat(((ServiceResponseException) cause).getResponseBody(), containsString("Too Many Requests."));
    }
  }
}
