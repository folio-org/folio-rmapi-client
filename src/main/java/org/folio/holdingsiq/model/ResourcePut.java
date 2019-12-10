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
public class ResourcePut extends ResourceBase{
  @JsonProperty("isHidden")
  private Boolean isHidden;
  @JsonProperty("titleName")
  private String titleName;
  @JsonProperty("pubType")
  private String pubType;
  @JsonProperty("publisherName")
  private String publisherName;
  @JsonProperty("isPeerReviewed")
  private Boolean isPeerReviewed;
  @JsonProperty("description")
  private String description;
  @JsonProperty("edition")
  private String edition;
  @JsonProperty("contributorsList")
  private List<Contributor> contributorsList;
  @JsonProperty("identifiersList")
  private List<Identifier> identifiersList;

  @JsonCreator
  @Builder(builderMethodName = "resourcePutBuilder")
  ResourcePut(
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
    @JsonProperty("isHidden") Boolean isHidden,
    @JsonProperty("titleName") String titleName,
    @JsonProperty("pubType") String pubType,
    @JsonProperty("publisherName") String publisherName,
    @JsonProperty("isPeerReviewed") Boolean isPeerReviewed,
    @JsonProperty("description") String description,
    @JsonProperty("edition") String edition,
    @JsonProperty("contributorsList") List<Contributor> contributorsList,
    @JsonProperty("identifiersList") List<Identifier> identifiersList
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
    this.isHidden = isHidden;
    this.titleName = titleName;
    this.pubType = pubType;
    this.publisherName = publisherName;
    this.isPeerReviewed = isPeerReviewed;
    this.description = description;
    this.edition = edition;
    this.contributorsList = contributorsList;
    this.identifiersList = identifiersList;
  }
}
