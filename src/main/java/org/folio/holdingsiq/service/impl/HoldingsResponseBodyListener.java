package org.folio.holdingsiq.service.impl;

import io.vertx.core.buffer.Buffer;

public interface HoldingsResponseBodyListener {

  void bodyReceived(Buffer body, HoldingsInteractionContext ctx);
}
