package org.folio.holdingsiq.service.config;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertTrue;

import static org.folio.holdingsiq.service.config.ConfigTestData.OKAPI_DATA;

import java.util.List;
import java.util.concurrent.ExecutionException;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;

import org.folio.holdingsiq.model.ConfigurationError;
import org.folio.holdingsiq.service.ConfigurationService;
import org.folio.holdingsiq.service.exception.ServiceResponseException;
import org.folio.holdingsiq.service.impl.ConfigurationServiceImpl;
import org.folio.holdingsiq.service.impl.HoldingsIQServiceTestConfig;

public class ConfigurationServiceImplTest extends HoldingsIQServiceTestConfig {

  private ConfigurationService configService;
  private Context context;

  @Before
  public void setUp() {
    Vertx vertx = Vertx.vertx();
    context = vertx.getOrCreateContext();
    configService = new ConfigurationServiceImpl(vertx);
  }

  @Test
  public void shouldNotReturnErrorsOnVerifyWhenCredentialsAreValid() throws ExecutionException, InterruptedException {
    var urlPattern = new UrlPattern(WireMock.equalTo("/rm/rmaccounts/" + STUB_CUSTOMER_ID + "/"), false);
    wiremockServer.stubFor(
      get(urlPattern).willReturn(aResponse().withStatus(HttpStatus.SC_OK).withBody("{}"))
    );

    var completableFuture = configService.verifyCredentials(getConfiguration(), context, OKAPI_DATA);

    assertTrue(isCompletedNormally(completableFuture));
    assertThat(completableFuture.get(), empty());
  }

  @Test
  public void shouldReturnConfigurationErrorOnVerifyWhenCredentialsAreInvalidWithCode401()
    throws ExecutionException, InterruptedException {
    var urlPattern = new UrlPattern(WireMock.equalTo("/rm/rmaccounts/" + STUB_CUSTOMER_ID + "/"), false);
    wiremockServer.stubFor(
      get(urlPattern).willReturn(aResponse().withStatus(HttpStatus.SC_UNAUTHORIZED).withBody("{}"))
    );

    var completableFuture = configService.verifyCredentials(getConfiguration(), context, OKAPI_DATA);

    assertTrue(isCompletedNormally(completableFuture));
    List<ConfigurationError> configurationErrors = completableFuture.get();
    assertThat(configurationErrors, hasSize(1));
    assertThat(configurationErrors.get(0), instanceOf(ConfigurationError.class));
  }

  @Test
  public void shouldReturnConfigurationErrorOnVerifyWhenCredentialsAreInvalidWithCode403()
    throws ExecutionException, InterruptedException {
    var urlPattern = new UrlPattern(WireMock.equalTo("/rm/rmaccounts/" + STUB_CUSTOMER_ID + "/"), false);
    wiremockServer.stubFor(
      get(urlPattern).willReturn(aResponse().withStatus(HttpStatus.SC_FORBIDDEN).withBody("{}"))
    );

    var completableFuture = configService.verifyCredentials(getConfiguration(), context, OKAPI_DATA);

    assertTrue(isCompletedNormally(completableFuture));
    List<ConfigurationError> configurationErrors = completableFuture.get();
    assertThat(configurationErrors, hasSize(1));
    assertThat(configurationErrors.get(0), instanceOf(ConfigurationError.class));
  }

  @Test
  public void shouldFailedOnVerifyWhenCredentialsAreValidWithCode429() {
    var urlPattern = new UrlPattern(WireMock.equalTo("/rm/rmaccounts/" + STUB_CUSTOMER_ID + "/"), false);
    wiremockServer.stubFor(
      get(urlPattern).willReturn(aResponse().withStatus(429).withBody("{\n"
        + "  \"Errors\": [\n"
        + "    {\n"
        + "      \"Code\": 1010,\n"
        + "      \"Message\": \"Too Many Requests.\",\n"
        + "      \"SubCode\": 0\n"
        + "    }\n"
        + "  ]\n"
        + "}"))
    );

    var completableFuture = configService.verifyCredentials(getConfiguration(), context, OKAPI_DATA);


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
