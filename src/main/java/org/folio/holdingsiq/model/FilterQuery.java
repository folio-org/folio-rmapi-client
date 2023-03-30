package org.folio.holdingsiq.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder(toBuilder = true)
public class FilterQuery {

  private String selected;
  private String type;
  private String name;
  private String isxn;
  private String subject;
  private String publisher;
  private List<Integer> packageIds;

}
