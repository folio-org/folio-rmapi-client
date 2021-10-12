package org.folio.holdingsiq.service.impl.urlbuilder;

import static org.apache.commons.lang3.StringUtils.defaultString;

import java.util.ArrayList;
import java.util.List;

import org.folio.holdingsiq.model.FilterQuery;
import org.folio.holdingsiq.model.Sort;

public class TitlesFilterableUrlBuilder {

  private static final String SEARCHFIELD_TITLENAME = "titlename";
  private static final String SEARCHFIELD_ISXN = "isxn";
  private static final String SEARCHFIELD_PUBLISHER = "publisher";
  private static final String SEARCHFIELD_SUBJECT = "subject";

  private static final String DEFAULT_SELECTION = "all";
  private static final String DEFAULT_RESOURCE_TYPE = "all";
  private static final String DEFAULT_SEARCH_TYPE = "advanced";

  private FilterQuery filterQuery;
  private int page = 1;
  private int count = 25;
  private Sort sort;
  private String searchType;

  public TitlesFilterableUrlBuilder filter(FilterQuery filterQuery) {
    this.filterQuery = filterQuery;
    return this;
  }

  public TitlesFilterableUrlBuilder searchType(String searchType) {
    this.searchType = searchType;
    return this;
  }

  public TitlesFilterableUrlBuilder page(int page) {
    this.page = page;
    return this;
  }

  public TitlesFilterableUrlBuilder count(int count) {
    this.count = count;
    return this;
  }

  public TitlesFilterableUrlBuilder sort(Sort sort) {
    this.sort = sort;
    return this;
  }

  public String build() {
    String search = null;
    String searchField;
    if (filterQuery.getName() != null) {
      search = filterQuery.getName();
      searchField = SEARCHFIELD_TITLENAME;
    } else if (filterQuery.getIsxn() != null) {
      search = filterQuery.getIsxn();
      searchField = SEARCHFIELD_ISXN;
    } else if (filterQuery.getSubject() != null) {
      search = filterQuery.getSubject();
      searchField = SEARCHFIELD_SUBJECT;
    } else if (filterQuery.getPublisher() != null) {
      search = filterQuery.getPublisher();
      searchField = SEARCHFIELD_PUBLISHER;
    } else {
      searchField = SEARCHFIELD_TITLENAME;
    }

    String query = new QueryableUrlBuilder()
      .q(search)
      .page(page)
      .count(count)
      .sort(sort)
      .nameParameter(SEARCHFIELD_TITLENAME)
      .build();

    List<String> parameters = new ArrayList<>();
    parameters.add("searchfield=" + searchField);
    parameters.add("selection=" + defaultString(filterQuery.getSelected(), DEFAULT_SELECTION));
    parameters.add("resourcetype=" + defaultString(filterQuery.getType(), DEFAULT_RESOURCE_TYPE));
    parameters.add("searchtype=" + defaultString(searchType, DEFAULT_SEARCH_TYPE));
    parameters.add(query);

    return String.join("&", parameters);
  }

}
