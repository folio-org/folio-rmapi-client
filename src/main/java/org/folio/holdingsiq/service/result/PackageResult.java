package org.folio.holdingsiq.service.result;

import org.folio.holdingsiq.model.PackageByIdData;
import org.folio.holdingsiq.model.Titles;
import org.folio.holdingsiq.model.VendorById;

public class PackageResult {
  private PackageByIdData packageData;
  private VendorById vendor;
  private Titles titles;

  public PackageResult(PackageByIdData packageData, VendorById vendor, Titles titles) {
    this.packageData = packageData;
    this.vendor = vendor;
    this.titles = titles;
  }

  public PackageByIdData getPackageData() {
    return packageData;
  }

  public VendorById getVendor() {
    return vendor;
  }

  public Titles getTitles() {
    return titles;
  }

}
