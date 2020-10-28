package org.folio.holdingsiq.service.impl;

import static java.lang.String.format;

import static org.folio.holdingsiq.service.impl.HoldingsRequestHelper.PACKAGES_PATH;
import static org.folio.holdingsiq.service.impl.HoldingsRequestHelper.TITLES_PATH;
import static org.folio.holdingsiq.service.impl.HoldingsRequestHelper.VENDORS_PATH;
import static org.folio.holdingsiq.service.impl.HoldingsRequestHelper.successBodyLogger;

import java.util.concurrent.CompletableFuture;

import io.vertx.core.Vertx;

import org.folio.holdingsiq.model.Configuration;
import org.folio.holdingsiq.model.ResourceDeletePayload;
import org.folio.holdingsiq.model.ResourceId;
import org.folio.holdingsiq.model.ResourcePut;
import org.folio.holdingsiq.model.ResourceSelectedPayload;
import org.folio.holdingsiq.model.Title;
import org.folio.holdingsiq.service.ResourcesHoldingsIQService;

public class ResourcesHoldingsIQServiceImpl implements ResourcesHoldingsIQService {

  private static final String RESOURCE_ENDPOINT_FORMAT = "vendors/%s/packages/%s/titles/%s";
  private final HoldingsRequestHelper holdingsRequestHelper;

  public ResourcesHoldingsIQServiceImpl(Configuration config, Vertx vertx) {
    holdingsRequestHelper = new HoldingsRequestHelper(config, vertx);
    holdingsRequestHelper.addBodyListener(successBodyLogger());
  }

  @Override
  public CompletableFuture<Title> retrieveResource(ResourceId resourceId) {
    CompletableFuture<Title> titleFuture;
    final String path = format(RESOURCE_ENDPOINT_FORMAT, resourceId.getProviderIdPart(), resourceId.getPackageIdPart(), resourceId.getTitleIdPart());
    titleFuture = holdingsRequestHelper.getRequest(holdingsRequestHelper.constructURL(path), Title.class);
    return titleFuture;
  }

  @Override
  public CompletableFuture<Title> postResource(ResourceSelectedPayload resourcePost, ResourceId resourceId) {
    final String path = format(RESOURCE_ENDPOINT_FORMAT, resourceId.getProviderIdPart(), resourceId.getPackageIdPart(), resourceId.getTitleIdPart());
    return holdingsRequestHelper.putRequest(holdingsRequestHelper.constructURL(path), resourcePost)
      .thenCompose(o -> this.retrieveResource(resourceId));
  }


  public CompletableFuture<Void> updateResource(ResourceId parsedResourceId, ResourcePut resourcePutBody) {
    final String path = VENDORS_PATH + '/' + parsedResourceId.getProviderIdPart() + '/' + PACKAGES_PATH + '/' + parsedResourceId.getPackageIdPart() + '/' + TITLES_PATH + '/' + parsedResourceId.getTitleIdPart();
    return holdingsRequestHelper.putRequest(holdingsRequestHelper.constructURL(path), resourcePutBody);
  }

  @Override
  public CompletableFuture<Void> deleteResource(ResourceId parsedResourceId) {
    final String path = VENDORS_PATH + '/' + parsedResourceId.getProviderIdPart() + '/' + PACKAGES_PATH + '/' + parsedResourceId.getPackageIdPart() + '/' + TITLES_PATH + '/' + parsedResourceId.getTitleIdPart();
    return holdingsRequestHelper.putRequest(holdingsRequestHelper.constructURL(path), new ResourceDeletePayload(false));
  }
}
