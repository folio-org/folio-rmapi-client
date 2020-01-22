package org.folio.holdingsiq.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeltaReportParams {
  @JsonProperty("currentSnapshotId")
  private String currentSnapshotId;
  @JsonProperty("previousSnapshotId")
  private String previousSnapshotId;
}
