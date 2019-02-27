package org.folio.holdingsiq.builder;

import org.folio.holdingsiq.model.Sort;
import org.folio.holdingsiq.service.impl.urlbuilder.QueryableUrlBuilder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class QueryableUrlBuilderTest {
  @Test
  public void shouldBuildUrlForNameSortWhenSortName() {
    String path = new QueryableUrlBuilder()
      .q("ebsco")
      .nameParameter("vendorname")
      .sort(Sort.NAME)
      .build();
    assertEquals("search=ebsco&offset=1&count=25&orderby=vendorname", path);
  }

  @Test
  public void shouldBuildUrlForRelevanceSortWhenSortRelevance() {
    String path = new QueryableUrlBuilder()
      .sort(Sort.RELEVANCE)
      .q("ebsco")
      .build();
    assertEquals("search=ebsco&offset=1&count=25&orderby=relevance", path);
  }

  @Test
  public void shouldBuildUrlForNameSortWhenQueryIsNotSet() {
    String path = new QueryableUrlBuilder()
      .nameParameter("vendorname")
      .build();
    assertEquals("search=&offset=1&count=25&orderby=vendorname", path);
  }
}
