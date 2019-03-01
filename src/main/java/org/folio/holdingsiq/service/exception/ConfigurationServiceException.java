package org.folio.holdingsiq.service.exception;

public class ConfigurationServiceException extends RuntimeException {
  private static final long serialVersionUID = 367568234830759007L;
  private final String responseBody;
  private final Integer statusCode;

  public ConfigurationServiceException(String responseBody, Integer statusCode) {
    this.responseBody = responseBody;
    this.statusCode = statusCode;
  }

  public String getResponseBody() {
    return responseBody;
  }

  public Integer getStatusCode() {
    return statusCode;
  }
}
