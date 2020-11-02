package org.folio.holdingsiq.service.impl;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import org.folio.holdingsiq.model.Configuration;
import org.folio.holdingsiq.service.exception.ResourceNotFoundException;
import org.folio.holdingsiq.service.exception.ResultsProcessingException;
import org.folio.holdingsiq.service.exception.ServiceResponseException;
import org.folio.holdingsiq.service.exception.UnAuthorizedException;

class HoldingsRequestHelper {

  private static final Logger LOG = LoggerFactory.getLogger(HoldingsRequestHelper.class);

  private static final String HTTP_HEADER_CONTENT_TYPE = "Content-type";
  private static final String APPLICATION_JSON = "application/json";
  private static final String HTTP_HEADER_ACCEPT = "Accept";
  private static final String RMAPI_API_KEY = "X-Api-Key";

  private static final String JSON_RESPONSE_ERROR = "Error processing RMAPI Response";
  private static final String INVALID_RMAPI_RESPONSE = "Invalid RMAPI response";

  private static final String VENDOR_LOWER_STRING = "vendor";
  private static final String PROVIDER_LOWER_STRING = "provider";
  private static final String VENDOR_UPPER_STRING = "Vendor";
  private static final String PROVIDER_UPPER_STRING = "Provider";

  static final String VENDORS_PATH = "vendors";
  static final String PACKAGES_PATH = "packages";
  static final String TITLES_PATH = "titles";

  private String customerId;
  private String apiKey;
  private String baseURI;

  private Vertx vertx;
  private List<HoldingsResponseBodyListener> bodyListeners;

  
  HoldingsRequestHelper(Configuration config, Vertx vertx) {
    this.customerId = config.getCustomerId();
    this.apiKey = config.getApiKey();
    this.baseURI = config.getUrl();
    this.vertx = vertx;
    this.bodyListeners= new ArrayList<>();
  }

  HoldingsRequestHelper addBodyListener(HoldingsResponseBodyListener listener) {
    if (listener != null) {
      bodyListeners.add(listener);
    }
    
    return this;
  }

  <T> CompletableFuture<T> getRequest(String query, Class<T> clazz) {
    CompletableFuture<T> future = new CompletableFuture<>();

    HttpClient httpClient = vertx.createHttpClient();
    final HttpClientRequest request = httpClient.getAbs(query);

    addRequestHeaders(request);

    LOG.info("RMAPI Service GET absolute URL is: {}", request.absoluteURI());
    executeRequest(query, clazz, future, httpClient, request);

    request.end();

    return future;
  }

  <T> CompletableFuture<Void> putRequest(String query, T putData) {
    CompletableFuture<Void> future = new CompletableFuture<>();

    HttpClient httpClient = vertx.createHttpClient();
    final HttpClientRequest request = httpClient.putAbs(query);

    addRequestHeaders(request);

    LOG.info("RMAPI Service PUT absolute URL is: {}", request.absoluteURI());

    request.handler(response -> response.bodyHandler(body -> {
      httpClient.close();

      fireBodyReceived(body, new HoldingsInteractionContext(query, null, request, response));

      if (response.statusCode() == 204) {
        future.complete(null);
      } else {
        handleRMAPIError(response, query, body, future);
      }

    })).exceptionHandler(future::completeExceptionally);

    String encodedBody = Json.encodePrettily(putData);
    LOG.info("RMAPI Service PUT body is: {}", encodedBody);
    request.end(encodedBody);

    return future;
  }

  <T, P> CompletableFuture<T> postRequest(String query, P postData, Class<T> clazz){
    CompletableFuture<T> future = new CompletableFuture<>();

    HttpClient httpClient = vertx.createHttpClient();
    final HttpClientRequest request = httpClient.postAbs(query);

    addRequestHeaders(request);

    LOG.info("RMAPI Service POST absolute URL is: {}", request.absoluteURI());
    executeRequest(query, clazz, future, httpClient, request);

    String encodedBody = Json.encodePrettily(postData);
    LOG.info("RMAPI Service POST body is: {}", encodedBody);
    request.end(encodedBody);

    return future;
  }

  private <T> void executeRequest(String query, Class<T> clazz, CompletableFuture<T> future,
                                  HttpClient httpClient, HttpClientRequest request) {
    request.handler(response -> response.bodyHandler(body -> {
      httpClient.close();

      fireBodyReceived(body, new HoldingsInteractionContext(query, clazz, request, response));

      if (response.statusCode() == 200 ||
        response.statusCode() == 202) {
        try {
          if(clazz == String.class){
            @SuppressWarnings("unchecked")
            T value = (T) body.toString();
            future.complete(value);
          }else{
            future.complete(Json.decodeValue(body.toString(), clazz));
          }
        } catch (Exception e) {
          LOG.error("{} - Response = [{}] Target Type = [{}] Cause: [{}]",
            JSON_RESPONSE_ERROR, body.toString(), clazz, e.getMessage());
          future.completeExceptionally(
            new ResultsProcessingException(format("%s for query = %s", JSON_RESPONSE_ERROR, query), e));
        }
      } else {

        handleRMAPIError(response, query, body, future);
      }
    })).exceptionHandler(future::completeExceptionally);
  }

  private void fireBodyReceived(Buffer body, HoldingsInteractionContext context) {
    for (HoldingsResponseBodyListener listener : bodyListeners) {
      listener.bodyReceived(body, context);
    }
  }

  void addRequestHeaders(HttpClientRequest request) {
    request.headers().add(HTTP_HEADER_ACCEPT, APPLICATION_JSON);
    request.headers().add(HTTP_HEADER_CONTENT_TYPE, APPLICATION_JSON);
    request.headers().add(RMAPI_API_KEY, apiKey);
  }

  <T> void handleRMAPIError(HttpClientResponse response, String query, Buffer body,
                                      CompletableFuture<T> future) {

    LOG.error("{} status code = [{}] status message = [{}] query = [{}] body = [{}]",
      INVALID_RMAPI_RESPONSE, response.statusCode(), response.statusMessage(), query, body.toString());

    String msgBody = mapVendorToProvider(body.toString());

    if (response.statusCode() == 404) {
      future.completeExceptionally(new ResourceNotFoundException(
        format("Requested resource %s not found", query), response.statusCode(), response.statusMessage(), msgBody, query));
    } else if ((response.statusCode() == 401) || (response.statusCode() == 403)) {
      future.completeExceptionally(new UnAuthorizedException(
        format("Unauthorized Access to %s", query), response.statusCode(), response.statusMessage(), msgBody, query));
    } else {

      future.completeExceptionally(new ServiceResponseException(
        format("%s Code = %s Message = %s Body = %s", INVALID_RMAPI_RESPONSE, response.statusCode(),
          response.statusMessage(), body.toString()),
        response.statusCode(), response.statusMessage(), msgBody, query));
    }
  }

  private String mapVendorToProvider(String msgBody) {
    return msgBody.replace(VENDOR_LOWER_STRING, PROVIDER_LOWER_STRING).replace(VENDOR_UPPER_STRING, PROVIDER_UPPER_STRING);
  }

  /**
   * Constructs full rmapi path
   *
   * @param path
   *          path appended to the end of url
   */
  String constructURL(String path) {
    String fullPath = format("%s/rm/rmaccounts/%s/%s", baseURI, customerId, path);

    LOG.info("constructurl - path=" + fullPath);
    return fullPath;
  }

  static HoldingsResponseBodyListener successBodyLogger() {
    return (body, ctx) -> {
      int sc = ctx.statusCode();

      if (sc == 200 || sc == 201 || sc == 202 || sc == 204) {
        LOG.info("[OK] RMAPI Service response: query = [{}], method = [{}], statusCode = [{}], body = [{}]",
            ctx.getRequestUrl(), ctx.httpMethod(), sc, body.toString());
      }
    };
  }

}
