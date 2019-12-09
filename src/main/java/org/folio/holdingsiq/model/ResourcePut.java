package org.folio.holdingsiq.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourcePut {
  @JsonProperty("isSelected")
  private Boolean isSelected;
  @JsonProperty("isHidden")
  private Boolean isHidden;
  @JsonProperty("coverageStatement")
  private String coverageStatement;
  @JsonProperty("customEmbargoPeriod")
  private EmbargoPeriod customEmbargoPeriod;
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
  @JsonProperty("proxy")
  private Proxy proxy;
  @JsonProperty("url")
  private String url;
  @JsonProperty("contributorsList")
  private List<Contributor> contributorsList;
  @JsonProperty("identifiersList")
  private List<Identifier> identifiersList;
  @JsonProperty("customCoverageList")
  private List<CoverageDates> customCoverageList;
  @JsonProperty("userDefinedField1")
  private String userDefinedField1;
  @JsonProperty("userDefinedField2")
  private String userDefinedField2;
  @JsonProperty("userDefinedField3")
  private String userDefinedField3;
  @JsonProperty("userDefinedField4")
  private String userDefinedField4;
  @JsonProperty("userDefinedField5")
  private String userDefinedField5;
}
