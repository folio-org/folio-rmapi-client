package org.folio.holdingsiq.service.exception;

import java.util.List;

import org.folio.holdingsiq.model.ConfigurationError;

public class ConfigurationInvalidException extends RuntimeException {

  private static final long serialVersionUID = 5325789760372474463L;

  private final List<ConfigurationError> errors;

  public ConfigurationInvalidException(List<ConfigurationError> errors) {
    this.errors = errors;
  }

  public List<ConfigurationError> getErrors() {
    return errors;
  }
}
