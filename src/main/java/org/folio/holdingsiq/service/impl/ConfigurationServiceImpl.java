package org.folio.holdingsiq.service.impl;

import static io.vertx.core.Future.failedFuture;
import static io.vertx.core.Future.succeededFuture;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.http.HttpHeaders.ACCEPT;

import static org.folio.rest.RestVerticle.OKAPI_HEADER_TENANT;
import static org.folio.rest.RestVerticle.OKAPI_HEADER_TOKEN;
import static org.folio.util.FutureUtils.mapVertxFuture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.http.HttpStatus;

import org.folio.holdingsiq.model.Configuration;
import org.folio.holdingsiq.model.ConfigurationError;
import org.folio.holdingsiq.model.OkapiData;
import org.folio.holdingsiq.service.ConfigurationService;
import org.folio.holdingsiq.service.exception.ConfigurationServiceException;
import org.folio.rest.tools.utils.TenantTool;

/**
 * Retrieves the RM API connection details from mod-configuration.
 */
public class ConfigurationServiceImpl implements ConfigurationService {

  private static final String JSON_API_TYPE = "application/vnd.api+json";
  private static final String USER_CREDS_URL = "/eholdings/user-kb-credential";

  private final WebClient client;


  public ConfigurationServiceImpl(Vertx vertx) {
    this.client = WebClient.create(vertx);
  }

  @Override
  public CompletableFuture<Configuration> retrieveConfiguration(OkapiData okapiData) {
    return getUserCredentials(okapiData).thenApply(this::credentialsToConfiguration);
  }

  @Override
  public CompletableFuture<List<ConfigurationError>> verifyCredentials(Configuration configuration,
                                                                       Context vertxContext, String tenant) {
    List<ConfigurationError> errors = new ArrayList<>();

    if (!isConfigurationParametersValid(configuration, errors)) {
      return completedFuture(errors);
    }

    return new HoldingsIQServiceImpl(configuration, vertxContext.owner())
      .verifyCredentials()
      .thenCompose(o -> completedFuture(Collections.<ConfigurationError>emptyList()))
      .exceptionally(e -> Collections.singletonList(new ConfigurationError("KB API Credentials are invalid")));
  }

  private CompletableFuture<JsonObject> getUserCredentials(OkapiData okapiData) {
    return mapVertxFuture(getJson(USER_CREDS_URL, okapiData));
  }

  private Future<JsonObject> getJson(String requestUrl, OkapiData okapiData) {
    Promise<HttpResponse<Buffer>> promise = Promise.promise();

    client.get(okapiData.getOkapiPort(), okapiData.getOkapiHost(), requestUrl)
      .putHeader(OKAPI_HEADER_TENANT, TenantTool.calculateTenantId(okapiData.getTenant()))
      .putHeader(OKAPI_HEADER_TOKEN, okapiData.getApiToken())
      .putHeader(ACCEPT, JSON_API_TYPE)
      .expect(ResponsePredicate.contentType(JSON_API_TYPE))
      .send(promise);

    return promise.future().compose(res ->
      res.statusCode() == HttpStatus.SC_OK
        ? succeededFuture(res.bodyAsJsonObject())
        : failedFuture(new ConfigurationServiceException(res.toString(), res.statusCode())));
  }

  private Configuration credentialsToConfiguration(JsonObject creds) {
    Configuration.ConfigurationBuilder builder = Configuration.builder();

    if (creds != null) {
      JsonObject attrs = creds.getJsonObject("attributes");

      if (attrs != null) {
        builder.customerId(attrs.getString("customerId"));
        builder.apiKey(attrs.getString("apiKey"));
        builder.url(attrs.getString("url"));
      }
    }

    return builder.build();
  }

  private boolean isConfigurationParametersValid(Configuration configuration, List<ConfigurationError> errors) {
    if (isEmpty(configuration.getUrl())) {
      errors.add(new ConfigurationError("Url is empty"));
    }
    if (isEmpty(configuration.getApiKey())) {
      errors.add(new ConfigurationError("API key is empty"));
    }
    if (isEmpty(configuration.getCustomerId())) {
      errors.add(new ConfigurationError("Customer ID is empty"));
    }

    UrlValidator urlValidator = new UrlValidator();
    if (!urlValidator.isValid(configuration.getUrl())) {
      errors.add(new ConfigurationError("Url is invalid"));
    }

    return errors.isEmpty();
  }
}
