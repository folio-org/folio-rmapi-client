package org.folio.holdingsiq.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Proxy {

  @JsonProperty("id")
  private String id;
  @JsonProperty("inherited")
  private Boolean inherited;

}
