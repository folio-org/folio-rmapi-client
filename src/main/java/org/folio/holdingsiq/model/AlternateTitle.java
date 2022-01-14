package org.folio.holdingsiq.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlternateTitle {

  @JsonProperty("alternateTitle")
  private String alternateTitle;

  @JsonProperty("titleType")
  private String titleType;

}
