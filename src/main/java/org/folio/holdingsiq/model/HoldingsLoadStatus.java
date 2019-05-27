package org.folio.holdingsiq.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class HoldingsLoadStatus {

  @JsonProperty("status")
  private String status;
  @JsonProperty("created")
  private String created;
  @JsonProperty("totalCount")
  private Integer totalCount;

}
