package org.folio.holdingsiq.service.result;

import org.folio.holdingsiq.model.Packages;
import org.folio.holdingsiq.model.VendorById;

public class VendorResult {
  private VendorById vendor;
  private Packages packages;

  public VendorResult(VendorById vendor, Packages packages) {
    this.vendor = vendor;
    this.packages = packages;
  }

  public VendorById getVendor() {
    return vendor;
  }

  public Packages getPackages() {
    return packages;
  }

}
