package org.folio.holdingsiq.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class DeltaReport {
  @JsonProperty("count")
  private Integer count;
  @JsonProperty("format")
  private String format;
  @JsonProperty("holdings")
  private List<HoldingInReport> holdings;
  @JsonProperty("offset")
  private Integer offset;
  @JsonProperty("packageIds")
  private String packageIds;
  @JsonProperty("resourceTypeIds")
  private String resourceTypeIds;
  @JsonProperty("totalCount")
  private Integer totalCount;
}
