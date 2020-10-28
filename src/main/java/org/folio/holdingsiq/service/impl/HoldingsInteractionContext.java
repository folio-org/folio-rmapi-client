package org.folio.holdingsiq.service.impl;

import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import lombok.Value;

@Value
public class HoldingsInteractionContext {

  String requestUrl;
  Class<?> expectedResultClass;

  HttpClientRequest request;
  HttpClientResponse response;


  public HttpMethod httpMethod() {
    return request.method();
  }

  public String absoluteURI() {
    return request.absoluteURI();
  }

  public String uri() {
    return request.uri();
  }

  public String path() {
    return request.path();
  }

  public String query() {
    return request.query();
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