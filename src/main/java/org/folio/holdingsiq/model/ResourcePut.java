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
public class ResourcePut {

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
