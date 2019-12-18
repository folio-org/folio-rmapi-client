package org.folio.holdingsiq.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class UserDefinedFields {
  @JsonProperty("userDefinedField1")
  private String userDefinedField1;
  @JsonProperty("userDefinedField2")
  private String userDefinedField2;
  @JsonProperty("userDefinedField3")
  private String userDefinedField3;
  @JsonProperty("userDefinedField4")
  private String userDefinedField4;
  @JsonProperty("userDefinedField5")
  private String userDefinedField5;
}
