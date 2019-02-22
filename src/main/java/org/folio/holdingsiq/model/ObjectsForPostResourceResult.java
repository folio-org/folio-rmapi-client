package org.folio.holdingsiq.model;

public class ObjectsForPostResourceResult {
  private Title title;
  private PackageByIdData packageData;
  private Titles titles;

  public ObjectsForPostResourceResult(Title title, PackageByIdData packageData, Titles titles) {
    this.title = title;
    this.packageData = packageData;
    this.titles = titles;
  }

  public Title getTitle() {
    return title;
  }

  public PackageByIdData getPackageData() {
    return packageData;
  }

  public Titles getTitles() {
    return titles;
  }
}
