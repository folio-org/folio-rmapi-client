package org.folio.holdingsiq.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class VendorPutToken {

   @JsonProperty("value")
  private Object value;

}
