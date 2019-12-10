package org.folio.holdingsiq.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(creatorVisibility = JsonAutoDetect.Visibility.ANY)
public class CustomerResources extends ResourceBase {

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

  @JsonCreator
  @Builder(builderMethodName = "customerResourcesBuilder")
  CustomerResources(
    @JsonProperty("coverageStatement") String coverageStatement,
    @JsonProperty("isSelected") Boolean isSelected,
    @JsonProperty("customEmbargoPeriod") EmbargoPeriod customEmbargoPeriod,
    @JsonProperty("proxy") Proxy proxy,
    @JsonProperty("url") String url,
    @JsonProperty("customCoverageList") List<CoverageDates> customCoverageList,
    @JsonProperty("userDefinedField1") String userDefinedField1,
    @JsonProperty("userDefinedField2") String userDefinedField2,
    @JsonProperty("userDefinedField3") String userDefinedField3,
    @JsonProperty("userDefinedField4") String userDefinedField4,
    @JsonProperty("userDefinedField5") String userDefinedField5,
    @JsonProperty("titleId") Integer titleId,
    @JsonProperty("packageId") Integer packageId,
    @JsonProperty("packageName") String packageName,
    @JsonProperty("isPackageCustom") Boolean isPackageCustom,
    @JsonProperty("vendorId") Integer vendorId,
    @JsonProperty("vendorName") String vendorName,
    @JsonProperty("locationId") Integer locationId,
    @JsonProperty("isTokenNeeded") Boolean isTokenNeeded,
    @JsonProperty("packageType") String packageType,
    @JsonProperty("visibilityData") VisibilityInfo visibilityData,
    @JsonProperty("managedCoverageList") List<CoverageDates> managedCoverageList,
    @JsonProperty("managedEmbargoPeriod") EmbargoPeriod managedEmbargoPeriod
  ) {
    super(
      coverageStatement,
      isSelected,
      customEmbargoPeriod,
      proxy,
      url,
      customCoverageList,
      userDefinedField1,
      userDefinedField2,
      userDefinedField3,
      userDefinedField4,
      userDefinedField5);
    this.titleId =  titleId;
    this.packageId =  packageId;
    this.packageName =  packageName;
    this.isPackageCustom =  isPackageCustom;
    this.vendorId =  vendorId;
    this.vendorName =  vendorName;
    this.locationId =  locationId;
    this.isTokenNeeded =  isTokenNeeded;
    this.packageType =  packageType;
    this.visibilityData =  visibilityData;
    this.managedCoverageList =  managedCoverageList;
    this.managedEmbargoPeriod =  managedEmbargoPeriod;
  }
}
