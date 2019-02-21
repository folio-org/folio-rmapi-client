package org.folio.holdingsiq.service.result;

import org.folio.holdingsiq.model.PackageByIdData;
import org.folio.holdingsiq.model.Title;
import org.folio.holdingsiq.model.VendorById;

public class ResourceResult {
  private Title title;
  private VendorById vendor;
  private PackageByIdData packageData;
  private boolean includeTitle;

  public ResourceResult(Title title, VendorById vendor, PackageByIdData packageData, boolean includeTitle) {
    this.title = title;
    this.vendor = vendor;
    this.packageData = packageData;
    this.includeTitle = includeTitle;
  }

  public Title getTitle() {
    return title;
  }

  public VendorById getVendor() {
    return vendor;
  }

  public PackageByIdData getPackageData() {
    return packageData;
  }

  public boolean isIncludeTitle() {
    return includeTitle;
  }

}
