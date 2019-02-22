package org.folio.holdingsiq.service.exception;

/**
 * @author cgodfrey
 *
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class UnAuthorizedException extends ServiceResponseException {

  private static final long serialVersionUID = 1L;

  public UnAuthorizedException(String message, Integer rmapiCode, String rmapiMessage, String responseBody,
                               String rmapiQuery) {
    super(message, rmapiCode, rmapiMessage, responseBody, rmapiQuery);
  }
}
