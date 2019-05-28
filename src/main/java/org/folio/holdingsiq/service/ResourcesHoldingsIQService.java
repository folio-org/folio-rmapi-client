package org.folio.holdingsiq.service;

import java.util.concurrent.CompletableFuture;

import org.folio.holdingsiq.model.ResourceId;
import org.folio.holdingsiq.model.ResourcePut;
import org.folio.holdingsiq.model.ResourceSelectedPayload;
import org.folio.holdingsiq.model.Title;

public interface ResourcesHoldingsIQService {

  CompletableFuture<Title> postResource(ResourceSelectedPayload resourcePost, ResourceId resourceId);
  CompletableFuture<Void> updateResource(ResourceId parsedResourceId, ResourcePut resourcePutBody);
  CompletableFuture<Void> deleteResource(ResourceId parsedResourceId);
  CompletableFuture<Title> retrieveResource(ResourceId resourceId);

}
