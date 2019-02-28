package org.folio.holdingsiq.service.impl;

import java.util.concurrent.CompletableFuture;

import io.vertx.core.Vertx;

import org.folio.holdingsiq.model.Sort;
import org.folio.holdingsiq.model.VendorById;
import org.folio.holdingsiq.model.VendorPut;
import org.folio.holdingsiq.model.Vendors;
import org.folio.holdingsiq.service.CommonHoldingsService;
import org.folio.holdingsiq.service.HoldingsIQService;
import org.folio.holdingsiq.service.ProviderHoldingsIQService;
import org.folio.holdingsiq.service.impl.urlbuilder.QueryableUrlBuilder;

public class ProviderHoldingsIQServiceImpl extends CommonHoldingsService implements ProviderHoldingsIQService {

  private static final String VENDOR_NAME_PARAMETER = "vendorname";
  private HoldingsIQService holdingsIQService;

  public ProviderHoldingsIQServiceImpl(String customerId, String apiKey, String baseURI, Vertx vertx, HoldingsIQService holdingsIQService) {
    super(customerId, apiKey, baseURI, vertx);
    this.holdingsIQService = holdingsIQService;
  }

  @Override
  public CompletableFuture<Long> getVendorId(){
    return holdingsIQService.retrieveRootProxyCustomLabels()
      .thenCompose(rootProxyCustomLabels ->
        CompletableFuture.completedFuture(Long.parseLong(rootProxyCustomLabels.getVendorId())));
  }

  @Override
  public CompletableFuture<VendorById> retrieveProvider(long id) {
    CompletableFuture<VendorById> vendorFuture;
    final String path = VENDORS_PATH + '/' + id;
    vendorFuture = this.getRequest(constructURL(path), VendorById.class);
    return vendorFuture;
  }

  @Override
  public CompletableFuture<VendorById> updateProvider(long id, VendorPut rmapiVendor) {
    final String path = VENDORS_PATH + '/' + id;

    return this.putRequest(constructURL(path), rmapiVendor)
      .thenCompose(vend -> this.retrieveProvider(id));
  }

  @Override
  public CompletableFuture<Vendors> retrieveProviders(String q, int page, int count, Sort sort) {
    String query = new QueryableUrlBuilder()
      .q(q)
      .page(page)
      .count(count)
      .sort(sort)
      .nameParameter(VENDOR_NAME_PARAMETER)
      .build();
    return getRequest(constructURL(VENDORS_PATH + "?" + query), Vendors.class);
  }

}
