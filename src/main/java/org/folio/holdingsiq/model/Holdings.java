package org.folio.holdingsiq.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Holdings {

  @JsonProperty("offset")
  private int offset;
  @JsonProperty("format")
  private String format;
  @JsonProperty("holdings")
  private List<Holding> holdingsList;

}
