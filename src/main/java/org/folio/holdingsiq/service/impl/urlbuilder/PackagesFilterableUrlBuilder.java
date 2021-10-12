package org.folio.holdingsiq.service.impl.urlbuilder;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;

import java.util.ArrayList;
import java.util.List;

import org.folio.holdingsiq.model.Sort;

public class PackagesFilterableUrlBuilder {

  private static final String DEFAULT_SELECTION = "all";
  private static final String DEFAULT_CONTENT_TYPE = "all";
  private static final String DEFAULT_SEARCH_TYPE = "advanced";

  private String filterSelected;
  private String filterType;
  private String searchType;
  private int page = 1;
  private int count = 25;
  private Sort sort;
  private String q;

  public PackagesFilterableUrlBuilder filterSelected(String filterSelected) {
    this.filterSelected = filterSelected;
    return this;
  }

  public PackagesFilterableUrlBuilder filterType(String filterType) {
    this.filterType = filterType;
    return this;
  }

  public PackagesFilterableUrlBuilder searchType(String searchType) {
    this.searchType = searchType;
    return this;
  }

  public PackagesFilterableUrlBuilder page(int page) {
    this.page = page;
    return this;
  }

  public PackagesFilterableUrlBuilder count(int count) {
    this.count = count;
    return this;
  }

  public PackagesFilterableUrlBuilder sort(Sort sort) {
    this.sort = sort;
    return this;
  }

  public PackagesFilterableUrlBuilder q(String q) {
    this.q = q;
    return this;
  }

  public String build() {
    String query = new QueryableUrlBuilder()
      .q(q)
      .page(page)
      .count(count)
      .sort(sort)
      .nameParameter("packagename")
      .build();

    List<String> parameters = new ArrayList<>();
    parameters.add("selection=" + defaultIfEmpty(filterSelected, DEFAULT_SELECTION));
    parameters.add("contenttype=" + defaultIfEmpty(filterType, DEFAULT_CONTENT_TYPE));
    parameters.add("searchtype=" + defaultIfEmpty(searchType, DEFAULT_SEARCH_TYPE));
    parameters.add(query);

    return String.join("&", parameters);
  }
}
