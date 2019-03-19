package org.folio.holdingsiq.service.validator;

import javax.validation.ValidationException;

import org.junit.Test;

public class PackageParametersValidatorTest {

  private final PackageParametersValidator validator = new PackageParametersValidator();

  @Test
  public void shouldNotThrowExceptionWhenParametersAreValid() {
    validator.validate("selected", null, "relevance", "query");
  }

  @Test(expected = ValidationException.class)
  public void shouldThrowExceptionWhenFilterSelectedIsNull() {
    validator.validate(null, null, "relevance", "query");
  }

  @Test(expected = ValidationException.class)
  public void shouldThrowExceptionWhenFilterSelectedIsInvalid() {
    validator.validate("notall", null, "relevance", "query");
  }

  @Test(expected = ValidationException.class)
  public void shouldThrowExceptionWhenFilterTypeIsInvalid() {
    validator.validate("selected", "notall", "relevance", "query");
  }

  @Test(expected = ValidationException.class)
  public void shouldThrowExceptionWhenSortIsInvalid() {
    validator.validate("selected", "all", "abc", "query");
  }

  @Test(expected = ValidationException.class)
  public void shouldThrowExceptionWhenSearchQueryIsEmpty() {
    validator.validate("selected", "all", "abc", "");
  }

}
