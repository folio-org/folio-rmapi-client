package org.folio.holdingsiq.service.exception;

/**
 * @author cgodfrey
 *
 */
public class ResultsProcessingException extends ServiceException {

  private static final long serialVersionUID = 1L;

  public ResultsProcessingException(String message, Exception e) {
    super(message, e);
  }
}
