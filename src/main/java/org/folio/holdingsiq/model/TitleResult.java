package org.folio.holdingsiq.model;

public class TitleResult {
  private Title title;
  private boolean includeResource;

  public TitleResult(Title title, boolean includeResource) {
    this.title = title;
    this.includeResource = includeResource;
  }

  public Title getTitle() {
    return title;
  }

  public boolean isIncludeResource() {
    return includeResource;
  }

}
