package org.folio.holdingsiq.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Holding {

  @JsonProperty("publication_title")
  private String publicationTitle;
  @JsonProperty("print_identifier")
  private String printIdentifier;
  @JsonProperty("online_identifier")
  private String onlineIdentifier;
  @JsonProperty("date_first_issue_online")
  private String dateFirstIssueOnline;
  @JsonProperty("num_first_vol_online")
  private String numFirstVolOnline;
  @JsonProperty("num_first_issue_online")
  private String numFirstIssueOnline;
  @JsonProperty("date_last_issue_online")
  private String dateLastIssueOnline;
  @JsonProperty("num_last_vol_online")
  private String numLastVolOnline;
  @JsonProperty("num_last_issue_online")
  private String numLastIssueOnline;
  @JsonProperty("title_url")
  private String titleUrl;
  @JsonProperty("title_id")
  private String titleId;
  @JsonProperty("embargo_info")
  private String embargoInfo;
  @JsonProperty("coverage_depth")
  private String coverageDepth;
  @JsonProperty("notes")
  private String notes;
  @JsonProperty("publisher_name")
  private String publisherName;
  @JsonProperty("publication_type")
  private String publicationType;
  @JsonProperty("date_monograph_published_print")
  private String dateMonographPublishedPrint;
  @JsonProperty("date_monograph_published_online")
  private String dateMonographPublishedOnline;
  @JsonProperty("monograph_volume")
  private String monographVolume;
  @JsonProperty("monograph_edition")
  private String monographEdition;
  @JsonProperty("first_editor")
  private String firstEditor;
  @JsonProperty("parent_publication_title_id")
  private String parentPublicationTitleId;
  @JsonProperty("preceding_publication_title_id")
  private String precedingPublicationTitleId;
  @JsonProperty("access_type")
  private String accessType;
  @JsonProperty("package_name")
  private String packageName;
  @JsonProperty("package_id")
  private String packageId;
  @JsonProperty("vendor_name")
  private String vendorName;
  @JsonProperty("vendor_id")
  private int vendorId;
  @JsonProperty("resource_type")
  private String resourceType;

}
