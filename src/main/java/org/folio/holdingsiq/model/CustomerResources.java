package org.folio.holdingsiq.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor(onConstructor = @__(@JsonIgnore))
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerResources {

  @JsonProperty("titleId")
  private Integer titleId;

  @JsonProperty("packageId")
  private Integer packageId;

  @JsonProperty("packageName")
  private String packageName;

  @JsonProperty("isPackageCustom")
  private Boolean isPackageCustom;

  @JsonProperty("vendorId")
  private Integer vendorId;

  @JsonProperty("vendorName")
  private String vendorName;

  @JsonProperty("locationId")
  private Integer locationId;

  @JsonProperty("isTokenNeeded")
  private Boolean isTokenNeeded;

  @JsonProperty("packageType")
  private String packageType;

  @JsonProperty("visibilityData")
  private VisibilityInfo visibilityData;

  @JsonProperty("managedCoverageList")
  private List<CoverageDates> managedCoverageList;

  @JsonProperty("managedEmbargoPeriod")
  private EmbargoPeriod managedEmbargoPeriod;

  @JsonProperty("coverageStatement")
  private String coverageStatement;

  @JsonProperty("isSelected")
  private Boolean isSelected;

  @JsonProperty("customEmbargoPeriod")
  private EmbargoPeriod customEmbargoPeriod;

  @JsonProperty("proxy")
  private Proxy proxy;

  @JsonProperty("url")
  private String url;

  @JsonProperty("customCoverageList")
  private List<CoverageDates> customCoverageList;

  @JsonUnwrapped
  private UserDefinedFields userDefinedFields;
}
