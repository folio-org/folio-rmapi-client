package org.folio.holdingsiq.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class HoldingsDownloadTransaction {
  @JsonProperty("creationDate")
  private String creationDate;
  @JsonProperty("expirationDate")
  private String expirationDate;
  @JsonProperty("packageIds")
  private String packageIds;
  @JsonProperty("resourceTypeIds")
  private String resourceTypeIds;
  @JsonProperty("status")
  private String status;
  @JsonProperty("transactionId")
  private String transactionId;
}
