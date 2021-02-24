package org.folio.holdingsiq.service.impl;

public interface HoldingsResponseBodyListener {

  void bodyReceived(Object body, HoldingsInteractionContext ctx);
}
