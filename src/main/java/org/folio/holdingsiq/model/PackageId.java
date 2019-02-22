package org.folio.holdingsiq.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class PackageId {

  private long providerIdPart;
  private long packageIdPart;

}
