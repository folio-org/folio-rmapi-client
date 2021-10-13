package org.folio.holdingsiq.service.builder;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.folio.holdingsiq.model.Sort;
import org.folio.holdingsiq.service.impl.urlbuilder.PackagesFilterableUrlBuilder;

public class PackagesFilterableUrlBuilderTest {

  @Test
  public void shouldBuildUrlWithFilterSelectedTrue() {
    String url = new PackagesFilterableUrlBuilder()
      .filterSelected("selected")
      .sort(Sort.NAME)
      .build();
    assertEquals("selection=selected&contenttype=all&searchtype=advanced&search=&offset=1&count=25&orderby=packagename", url);
  }

  @Test
  public void shouldBuildUrlWithFilterSelectedDefault() {
    String url = new PackagesFilterableUrlBuilder()
      .sort(Sort.NAME)
      .build();
    assertEquals("selection=all&contenttype=all&searchtype=advanced&search=&offset=1&count=25&orderby=packagename", url);
  }

  @Test
  public void shouldBuildUrlWithFilterSelectedEBSCO() {
    String url = new PackagesFilterableUrlBuilder()
      .filterSelected("orderedthroughebsco")
      .sort(Sort.NAME)
      .build();
    assertEquals("selection=orderedthroughebsco&contenttype=all&searchtype=advanced&search=&offset=1&count=25&orderby=packagename", url);
  }

  @Test
  public void shouldBuildUrlWithFilterType() {
    String url = new PackagesFilterableUrlBuilder()
      .filterType("abstractandindex")
      .sort(Sort.NAME)
      .build();
    assertEquals("selection=all&contenttype=abstractandindex&searchtype=advanced&search=&offset=1&count=25&orderby=packagename", url);
  }

  @Test
  public void shouldBuildUrlWithFilterTypeDefault() {
    String url = new PackagesFilterableUrlBuilder()
      .sort(Sort.NAME)
      .build();
    assertEquals("selection=all&contenttype=all&searchtype=advanced&search=&offset=1&count=25&orderby=packagename", url);
  }

  @Test
  public void shouldBuildUrlWithCount() {
    String url = new PackagesFilterableUrlBuilder()
      .count(5)
      .sort(Sort.NAME)
      .build();
    assertEquals("selection=all&contenttype=all&searchtype=advanced&search=&offset=1&count=5&orderby=packagename", url);
  }

  @Test
  public void shouldBuildUrlWithSort() {
    String url = new PackagesFilterableUrlBuilder()
      .q("Academic")
      .sort(Sort.RELEVANCE)
      .build();
    assertEquals("selection=all&contenttype=all&searchtype=advanced&search=Academic&offset=1&count=25&orderby=relevance", url);
  }

  @Test
  public void shouldBuildUrlWithPage() {
    String url = new PackagesFilterableUrlBuilder()
      .page(2)
      .sort(Sort.NAME)
      .build();
    assertEquals("selection=all&contenttype=all&searchtype=advanced&search=&offset=2&count=25&orderby=packagename", url);
  }

  @Test
  public void shouldBuildUrlWithQABCCLIO() {
    String url = new PackagesFilterableUrlBuilder()
      .q("ABC-CLIO")
      .sort(Sort.RELEVANCE)
      .build();
    assertEquals("selection=all&contenttype=all&searchtype=advanced&search=ABC-CLIO&offset=1&count=25&orderby=relevance", url);
  }
}
