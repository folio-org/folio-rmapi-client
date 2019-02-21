package org.folio.holdingsiq.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class FilterQuery {

  private String selected;
  private String type;
  private String name;
  private String isxn;
  private String subject;
  private String publisher;

}
