package org.folio.holdingsiq.service.impl;

import static java.lang.String.format;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import io.vertx.core.Vertx;

import org.folio.holdingsiq.model.ResourceDeletePayload;
import org.folio.holdingsiq.model.ResourceId;
import org.folio.holdingsiq.model.ResourcePut;
import org.folio.holdingsiq.model.ResourceSelectedPayload;
import org.folio.holdingsiq.model.Title;
import org.folio.holdingsiq.service.CommonHoldingsService;
import org.folio.holdingsiq.service.ResourcesHoldingsIQService;

public class ResourcesHoldingsIQServiceImpl extends CommonHoldingsService implements ResourcesHoldingsIQService {

  private static final String RESOURCE_ENDPOINT_FORMAT = "vendors/%s/packages/%s/titles/%s";

  public ResourcesHoldingsIQServiceImpl(String customerId, String apiKey, String baseURI, Vertx vertx) {
    super(customerId, apiKey, baseURI, vertx);
  }

  @Override
  public CompletableFuture<Title> retrieveResource(ResourceId resourceId) {
    CompletableFuture<Title> titleFuture;
    final String path = format(RESOURCE_ENDPOINT_FORMAT, resourceId.getProviderIdPart(), resourceId.getPackageIdPart(), resourceId.getTitleIdPart());
    titleFuture = this.getRequest(constructURL(path), Title.class);
    return titleFuture;
  }

  @Override
  public CompletableFuture<Title> postResource(ResourceSelectedPayload resourcePost, ResourceId resourceId) {
    final String path = format(RESOURCE_ENDPOINT_FORMAT, resourceId.getProviderIdPart(), resourceId.getPackageIdPart(), resourceId.getTitleIdPart());
    return this.putRequest(constructURL(path), resourcePost)
      .thenCompose(o -> this.retrieveResource(resourceId));
  }


  public CompletionStage<Void> updateResource(ResourceId parsedResourceId, ResourcePut resourcePutBody) {
    final String path = VENDORS_PATH + '/' + parsedResourceId.getProviderIdPart() + '/' + PACKAGES_PATH + '/' + parsedResourceId.getPackageIdPart() + '/' + TITLES_PATH + '/' + parsedResourceId.getTitleIdPart();
    return this.putRequest(constructURL(path), resourcePutBody);
  }

  @Override
  public CompletableFuture<Void> deleteResource(ResourceId parsedResourceId) {
    final String path = VENDORS_PATH + '/' + parsedResourceId.getProviderIdPart() + '/' + PACKAGES_PATH + '/' + parsedResourceId.getPackageIdPart() + '/' + TITLES_PATH + '/' + parsedResourceId.getTitleIdPart();
    return this.putRequest(constructURL(path), new ResourceDeletePayload(false));
  }
}
