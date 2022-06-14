package org.folio.holdingsiq.service.impl;

import io.vertx.core.MultiMap;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.impl.HttpRequestImpl;
import lombok.Value;

@Value
public class HoldingsInteractionContext {

  HttpRequest<?> request;
  HttpResponse<?> response;

  public String uri() {
    return ((HttpRequestImpl<?>) request).uri();
  }

  public MultiMap requestHeaders() {
    return request.headers();
  }

  public MultiMap responseHeaders() {
    return response.headers();
  }

  public int statusCode() {
    return response.statusCode();
  }

  public String statusMessage() {
    return response.statusMessage();
  }
}
