package org.folio.holdingsiq.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FacetsModel {

  @JsonProperty("id")
  private Integer packageId;

  @JsonProperty("name")
  private String packageName;

  @JsonProperty("count")
  private Integer totalCount;
}
