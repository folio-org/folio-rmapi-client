package org.folio.holdingsiq.service.impl;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.ws.rs.core.HttpHeaders;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.impl.ClientPhase;
import io.vertx.ext.web.client.impl.HttpContext;
import io.vertx.ext.web.client.impl.HttpRequestImpl;
import io.vertx.ext.web.client.impl.WebClientInternal;
import io.vertx.ext.web.client.predicate.ErrorConverter;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import io.vertx.ext.web.client.predicate.ResponsePredicateResult;
import io.vertx.ext.web.codec.BodyCodec;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.entity.ContentType;

import org.folio.holdingsiq.model.Configuration;
import org.folio.holdingsiq.service.exception.ResourceNotFoundException;
import org.folio.holdingsiq.service.exception.ResultsProcessingException;
import org.folio.holdingsiq.service.exception.ServiceResponseException;
import org.folio.holdingsiq.service.exception.UnAuthorizedException;

@Log4j2
public class HoldingsRequestHelper {

  static final String VENDORS_PATH = "vendors";
  static final String PACKAGES_PATH = "packages";
  static final String TITLES_PATH = "titles";

  private static final String RMAPI_API_KEY_HEADER = "X-Api-Key";
  private static final String JSON_RESPONSE_ERROR = "Error processing RMAPI Response";
  private static final String INVALID_RMAPI_RESPONSE = "Invalid RMAPI response";
  private static final String VENDOR_LOWER_STRING = "vendor";
  private static final String PROVIDER_LOWER_STRING = "provider";
  private static final String VENDOR_UPPER_STRING = "Vendor";
  private static final String PROVIDER_UPPER_STRING = "Provider";

  private final String customerId;
  private final String apiKey;
  private final String baseURI;

  private final Vertx vertx;
  private final List<HoldingsResponseBodyListener> bodyListeners;

  HoldingsRequestHelper(Configuration config, Vertx vertx) {
    this.customerId = config.getCustomerId();
    this.apiKey = config.getApiKey();
    this.baseURI = config.getUrl();
    this.vertx = vertx;
    this.bodyListeners = new ArrayList<>();
  }

  public static HoldingsResponseBodyListener successBodyLogger() {
    return (body, ctx) -> {
      int sc = ctx.statusCode();

      if (sc == 200 || sc == 201 || sc == 202 || sc == 204) {
        log.info("[OK] RMAPI Service response: query = [{}], method = [{}], statusCode = [{}], body = [{}]",
          ctx.uri(), ctx.httpMethod(), sc, body);
      }
    };
  }

  public <T> CompletableFuture<T> getRequest(String query, Class<T> clazz) {
    CompletableFuture<T> future = new CompletableFuture<>();

    var client = createClient();

    var responseFuture = addHeaders(client.getAbs(query))
      .expect(ResponsePredicate.create(ResponsePredicate.SC_OK, errorConverter(query)))
      .as(getResponseCodec(clazz))
      .send();

    finishRequest(future, responseFuture, client);

    return future;
  }

  public <T> CompletableFuture<Void> putRequest(String query, T putData) {
    CompletableFuture<Void> future = new CompletableFuture<>();

    var client = createClient();

    var responseFuture = addHeaders(client.putAbs(query))
      .expect(ResponsePredicate.create(ResponsePredicate.SC_NO_CONTENT, errorConverter(query)))
      .as(BodyCodec.none())
      .sendJson(putData);

    finishRequest(future, responseFuture, client);

    return future;
  }

  public <T, P> CompletableFuture<T> postRequest(String query, P postData, Class<T> clazz) {
    CompletableFuture<T> future = new CompletableFuture<>();

    var client = createClient();

    var responseFuture = addHeaders(client.postAbs(query))
      .expect(ResponsePredicate.create(expectedStatusesPredicate(200, 202), errorConverter(query)))
      .as(getResponseCodec(clazz))
      .sendJson(postData);

    finishRequest(future, responseFuture, client);

    return future;
  }

  public <T> CompletableFuture<T> postRequest(String query, Class<T> clazz) {
    CompletableFuture<T> future = new CompletableFuture<>();

    var client = createClient();

    var responseFuture = addHeaders(client.postAbs(query))
      .expect(ResponsePredicate.create(expectedStatusesPredicate(202, 409), errorConverter(query)))
      .as(getResponseCodec(clazz))
      .send();

    finishRequest(future, responseFuture, client);

    return future;
  }

  public HoldingsRequestHelper addBodyListener(HoldingsResponseBodyListener listener) {
    if (listener != null) {
      bodyListeners.add(listener);
    }
    return this;
  }

  public String constructURL(String path) {
    String fullPath = format("%s/rm/rmaccounts/%s/%s", baseURI, customerId, path);

    log.info("constructurl - path=" + fullPath);
    return fullPath;
  }

  private <T> BodyCodec<T> getResponseCodec(Class<T> clazz) {
    return BodyCodec.create(buffer -> {
      if (clazz == String.class) {
        @SuppressWarnings("unchecked")
        var t = (T) buffer.toString();
        return t;
      } else {
        try {
          return Json.decodeValue(buffer, clazz);
        } catch (Exception e) {
          log.error("{} - Response = [{}] Target Type = [{}] Cause: [{}]", JSON_RESPONSE_ERROR, buffer.toString(), clazz,
            e.getMessage());
          throw new ResultsProcessingException(JSON_RESPONSE_ERROR, e);
        }
      }
    });
  }

  private ErrorConverter errorConverter(String query) {
    return ErrorConverter.createFullBody(result -> {
      HttpResponse<Buffer> response = result.response();
      var body = response.body().toString();
      var statusCode = response.statusCode();
      var statusMessage = response.statusMessage();
      log.error("{} status code = [{}] status message = [{}] query = [{}] body = [{}]",
        INVALID_RMAPI_RESPONSE, statusCode, statusMessage, query, body);

      String msgBody = mapVendorToProvider(body);

      if (statusCode == 404) {
        var message = format("Requested resource %s not found", query);
        return new ResourceNotFoundException(message, statusCode, statusMessage, msgBody, query);
      } else if ((statusCode == 401) || (statusCode == 403)) {
        var message = format("Unauthorized Access to %s", query);
        return new UnAuthorizedException(message, statusCode, statusMessage, msgBody, query);
      } else {
        var message = format("%s Code = %s Message = %s Body = %s", INVALID_RMAPI_RESPONSE, statusCode, statusMessage, body);
        return new ServiceResponseException(message, statusCode, statusMessage, msgBody, query);
      }
    });
  }

  private String mapVendorToProvider(String msgBody) {
    return msgBody.replace(VENDOR_LOWER_STRING, PROVIDER_LOWER_STRING).replace(VENDOR_UPPER_STRING, PROVIDER_UPPER_STRING);
  }

  private Handler<HttpContext<?>> loggingInterceptor() {
    return httpContext -> {
      if (ClientPhase.SEND_REQUEST == httpContext.phase()) {
        HttpRequestImpl<?> request = (HttpRequestImpl<?>) httpContext.request();
        HttpMethod method = request.method();
        String uri = request.uri();
        Object requestBody = httpContext.body();
        if (requestBody != null) {
          log.info("RMAPI Service {} absolute URL is: {} ", method, uri);
          log.info("RMAPI Service {} body is: {}", method, requestBody);
        } else {
          log.info("RMAPI Service {} absolute URL is: {}", method, uri);
        }
      } else if (ClientPhase.RECEIVE_RESPONSE == httpContext.phase()) {
        httpContext.clientResponse()
          .bodyHandler(body -> fireBodyReceived(body,
            new HoldingsInteractionContext(httpContext.request(), httpContext.clientResponse())));
      }
      httpContext.next();
    };
  }

  private void fireBodyReceived(Object body, HoldingsInteractionContext context) {
    for (HoldingsResponseBodyListener listener : bodyListeners) {
      listener.bodyReceived(body, context);
    }
  }

  private WebClient createClient() {
    var client = WebClient.create(vertx);
    ((WebClientInternal) client).addInterceptor(loggingInterceptor());
    return client;
  }

  private HttpRequest<Buffer> addHeaders(HttpRequest<Buffer> request) {
    return request
      .putHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType())
      .putHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
      .putHeader(RMAPI_API_KEY_HEADER, apiKey);
  }

  private <T> void finishRequest(CompletableFuture<T> handler, Future<HttpResponse<T>> response, WebClient client) {
    response
      .onComplete(event -> client.close())
      .onSuccess(event -> handler.complete(event.body()))
      .onFailure(handler::completeExceptionally);
  }

  private ResponsePredicate expectedStatusesPredicate(int... expectedStatuses) {
    return response -> {
      var sc = response.statusCode();
      return ArrayUtils.contains(expectedStatuses, sc)
        ? ResponsePredicateResult.success()
        : ResponsePredicateResult.failure("Response status code " + sc + " is not in " + Arrays.toString(expectedStatuses));
    };
  }
}
