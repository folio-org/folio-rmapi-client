package org.folio.holdingsiq.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeltaReportStatus {
  @JsonProperty("currentSnapshotId")
  private String currentSnapshotId;
  @JsonProperty("packageIds")
  private String packageIds;
  @JsonProperty("previousSnapshotId")
  private String previousSnapshotId;
  @JsonProperty("resourceTypeIds")
  private String resourceTypeIds;
  @JsonProperty("status")
  private String status;
  @JsonProperty("totalCount")
  private String totalCount;
}
