package org.folio.holdingsiq.service.impl;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import io.vertx.core.Context;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.commons.validator.routines.UrlValidator;

import org.folio.holdingsiq.model.Configuration;
import org.folio.holdingsiq.model.ConfigurationError;
import org.folio.holdingsiq.model.OkapiData;
import org.folio.holdingsiq.service.ConfigurationService;
import org.folio.holdingsiq.service.exception.ConfigurationServiceException;
import org.folio.rest.client.ConfigurationsClient;
import org.folio.rest.jaxrs.model.Config;
import org.folio.rest.tools.client.Response;
import org.folio.rest.tools.utils.TenantTool;

/**
 * Retrieves the RM API connection details from mod-configuration.
 */
public class ConfigurationServiceImpl implements ConfigurationService {

  private static final String EBSCO_URL_CODE = "kb.ebsco.url";
  private static final String EBSCO_API_KEY_CODE = "kb.ebsco.apiKey";
  private static final String EBSCO_CUSTOMER_ID_CODE = "kb.ebsco.customerId";
  private static final List<String> RM_API_CONFIG_KEYS = Arrays.asList(EBSCO_CUSTOMER_ID_CODE, EBSCO_API_KEY_CODE,
      EBSCO_URL_CODE);
  private static final int QUERY_LIMIT = 100;

  private ConfigurationClientProvider configurationClientProvider;

  /**
   * @param configurationClientProvider object used to get http client for sending request to okapi
   */
  public ConfigurationServiceImpl(ConfigurationClientProvider configurationClientProvider) {
    this.configurationClientProvider = configurationClientProvider;
  }

  @Override
  public CompletableFuture<Configuration> retrieveConfiguration(OkapiData okapiData) {
    return retrieveConfigurations(okapiData)
      .thenCompose(configurations ->
        CompletableFuture.completedFuture(mapResults(configurations.getJsonArray("configs"))));
  }

  /**
   * Removes old configuration and adds new configuration for RM API
   */
  @Override
  public CompletableFuture<Configuration> updateConfiguration(Configuration configuration, OkapiData okapiData) {
    return retrieveConfigurations(okapiData)
      .thenCompose(configurations -> {
        List<String> ids = mapToIds(configurations.getJsonArray("configs"));
        return deleteConfigurations(ids, okapiData);
      })
      .thenCompose(o -> {
        List<Config> configurations = Arrays.asList(
          createConfig(configuration.getUrl(), EBSCO_URL_CODE, "EBSCO RM-API URL"),
          createConfig(configuration.getApiKey(), EBSCO_API_KEY_CODE, "EBSCO RM-API API Key"),
          createConfig(configuration.getCustomerId(), EBSCO_CUSTOMER_ID_CODE, "EBSCO RM-API Customer ID")
        );
        return postConfigurations(configurations, okapiData);
      })
      .thenCompose(aVoid -> CompletableFuture.completedFuture(configuration));
  }

  @Override
  public CompletableFuture<List<ConfigurationError>> verifyCredentials(
    Configuration configuration, Context vertxContext, String tenant) {
    List<ConfigurationError> errors = new ArrayList<>();
    if (!isConfigurationParametersValid(configuration, errors)) {
      return CompletableFuture.completedFuture(errors);
    }
    return new HoldingsIQServiceImpl(configuration, vertxContext.owner())
      .verifyCredentials()
      .thenCompose(o -> CompletableFuture.completedFuture(Collections.<ConfigurationError>emptyList()))
      .exceptionally(e -> Collections.singletonList(new ConfigurationError("KB API Credentials are invalid")));
  }

  /**
   * Get configuration object from mod-configuration in the form of json
   *
   * @param okapiData data of OKAPI server from which configuration is retrieved
   * @return future that will complete when configuration is retrieved
   */
  private CompletableFuture<JsonObject> retrieveConfigurations(OkapiData okapiData) {
    final String tenantId = TenantTool.calculateTenantId(okapiData.getTenant());
    CompletableFuture<JsonObject> future = new CompletableFuture<>();
    try {
      ConfigurationsClient configurationsClient = configurationClientProvider
        .createClient(okapiData.getOkapiHost(), okapiData.getOkapiPort(), tenantId, okapiData.getApiToken());
      configurationsClient.getEntries("module=EKB", 0, QUERY_LIMIT, null, null, response ->
        response.bodyHandler(body -> {
          if (verifyResponse(response, body, future)) {
            future.complete(body.toJsonObject());
          }
        }));
    } catch (UnsupportedEncodingException e) {
      future.completeExceptionally(e);
    }
    return future;
  }

  /**
   * Sends a delete request for each configuration in the list
   *
   * @param okapiData        data of OKAPI server to which delete requests are sent
   * @param configurationIds ids of configurations to be deleted
   * @return future that will complete when all configurations are deleted
   */
  private CompletableFuture<Void> deleteConfigurations(List<String> configurationIds, OkapiData okapiData) {
    final String tenantId = TenantTool.calculateTenantId(okapiData.getTenant());
    List<CompletableFuture<Void>> futures = new ArrayList<>();
    ConfigurationsClient configurationsClient = configurationClientProvider
      .createClient(okapiData.getOkapiHost(), okapiData.getOkapiPort(), tenantId, okapiData.getApiToken());
    for (String id : configurationIds) {
      futures.add(deleteConfiguration(configurationsClient, id));
    }
    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
  }

  /**
   * Sends a delete request for configuration id
   *
   * @return future that will complete when configuration is deleted
   */
  private CompletableFuture<Void> deleteConfiguration(ConfigurationsClient configurationsClient, String configurationId) {
    CompletableFuture<Void> future = new CompletableFuture<>();
    try {
      configurationsClient.deleteEntryId(configurationId, null, response -> response.bodyHandler(body -> {
        if (verifyResponse(response, body, future)) {
          future.complete(null);
        }
      }));
    } catch (UnsupportedEncodingException e) {
      future.completeExceptionally(e);
    }
    return future;
  }

  /**
   * Sends a post request for each configuration in the list
   *
   * @param okapiData      data of OKAPI server to which configurations are posted
   * @param configurations configurations to post
   * @return future that will complete when all configurations are posted
   */
  private CompletableFuture<Void> postConfigurations(List<Config> configurations, OkapiData okapiData) {
    final String tenantId = TenantTool.calculateTenantId(okapiData.getTenant());
    List<CompletableFuture<Void>> futures = new ArrayList<>();
    ConfigurationsClient configurationsClient = configurationClientProvider
      .createClient(okapiData.getOkapiHost(), okapiData.getOkapiPort(), tenantId, okapiData.getApiToken());
    for (Config configuration : configurations) {
      futures.add(postConfiguration(configurationsClient, configuration));
    }
    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

  }

  /**
   * Sends a post request to create configuration
   *
   * @return future that will complete when configuration is created
   */
  private CompletableFuture<Void> postConfiguration(ConfigurationsClient configurationsClient, Config configuration) {
    CompletableFuture<Void> future = new CompletableFuture<>();
    try {
      configurationsClient.postEntries(null, configuration, response -> response.bodyHandler(body -> {
        if (verifyResponse(response, body, future)) {
          future.complete(null);
        }
      }));
    } catch (Exception e) {
      future.completeExceptionally(e);
    }
    return future;
  }

  /**
   * Completes future exceptionally if response is not successful
   *
   * @return true if response is successful
   */
  private boolean verifyResponse(HttpClientResponse response, Buffer responseBody, CompletableFuture<?> future) {
    if (!Response.isSuccess(response.statusCode())) {
      future.completeExceptionally(new ConfigurationServiceException(responseBody.toString(), response.statusCode()));
      return false;
    }
    return true;
  }

  /**
   * Simple mapper for the results of mod-configuration to Configuration.
   *
   * @param configs All the RM API related configurations returned by
   *                mod-configuration.
   */
  private Configuration mapResults(JsonArray configs) {
    Configuration.ConfigurationBuilder configBuilder = Configuration.builder();
    configs.stream()
      .filter(JsonObject.class::isInstance)
      .map(JsonObject.class::cast)
      .forEach(entry -> {
        String code = entry.getString("code");
        String value = entry.getString("value");
        if (EBSCO_CUSTOMER_ID_CODE.equalsIgnoreCase(code)) {
          configBuilder.customerId(value);
        } else if (EBSCO_API_KEY_CODE.equalsIgnoreCase(code)) {
          configBuilder.apiKey(value);
        } else if (EBSCO_URL_CODE.equalsIgnoreCase(code)) {
          configBuilder.url(value);
        }
      });
    return configBuilder.build();
  }

  private List<String> mapToIds(JsonArray configs) {
    return configs.stream()
      .filter(JsonObject.class::isInstance)
      .map(JsonObject.class::cast)
      .filter(config -> RM_API_CONFIG_KEYS.contains(config.getString("code")))
      .map(config -> config.getString("id"))
      .collect(Collectors.toList());
  }

  private Config createConfig(String apiKey, String code, String description) {
    return new Config()
      .withModule("EKB")
      .withConfigName("api_access")
      .withCode(code)
      .withDescription(description)
      .withEnabled(true)
      .withValue(apiKey);
  }

  private boolean isConfigurationParametersValid(Configuration configuration, List<ConfigurationError> errors) {
    if(isEmpty(configuration.getUrl())){
      errors.add(new ConfigurationError("Url is empty"));
    }
    if(isEmpty(configuration.getApiKey())){
      errors.add(new ConfigurationError("API key is empty"));
    }
    if(isEmpty(configuration.getCustomerId())){
      errors.add(new ConfigurationError("Customer ID is empty"));
    }

    UrlValidator urlValidator = new UrlValidator();
    if(!urlValidator.isValid(configuration.getUrl())){
      errors.add(new ConfigurationError("Url is invalid"));
    }

    return errors.isEmpty();
  }
}
