package org.folio.holdingsiq.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceBase {
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
