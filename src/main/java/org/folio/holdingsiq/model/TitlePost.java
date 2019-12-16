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
public class TitlePost {
  @JsonProperty("titleName")
  private String titleName;

  @JsonProperty("edition")
  private String edition;

  @JsonProperty("publisherName")
  private String publisherName;

  @JsonProperty("pubType")
  private String pubType;

  @JsonProperty("description")
  private String description;

  @JsonProperty("isPeerReviewed")
  private boolean isPeerReviewed;

  @JsonProperty("identifiersList")
  private List<Identifier> identifiersList;

  @JsonProperty("contributorsList")
  private List<Contributor> contributorsList;

  @JsonUnwrapped
  private UserDefinedFields userDefinedFields;
}
