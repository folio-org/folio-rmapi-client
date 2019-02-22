package org.folio.holdingsiq.service.exception;

public class ServiceResponseException extends ServiceException {

  private static final long serialVersionUID = 1L;

  private final Integer code;
  private final String query;
  private final String responseMessage;
  private final String responseBody;

  public ServiceResponseException(String message, Integer code, String rmapiMessage, String responseBody,
                                  String query) {
    super(message);
    this.code = code;
    this.responseMessage = rmapiMessage;
    this.responseBody = responseBody;
    this.query = query;
  }

  public Integer getCode() {
    return this.code;
  }

  public String getResponseMessage() {
    return this.responseMessage;
  }

  public String getResponseBody() {
    return this.responseBody;
  }

  public String getQuery() {
    return this.query;
  }
}
