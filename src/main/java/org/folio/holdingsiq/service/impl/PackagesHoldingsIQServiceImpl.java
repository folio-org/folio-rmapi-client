package org.folio.holdingsiq.service.impl;

import static org.folio.holdingsiq.service.impl.HoldingsRequestHelper.PACKAGES_PATH;
import static org.folio.holdingsiq.service.impl.HoldingsRequestHelper.VENDORS_PATH;

import java.util.concurrent.CompletableFuture;

import io.vertx.core.Vertx;

import org.folio.holdingsiq.model.Configuration;
import org.folio.holdingsiq.model.PackageByIdData;
import org.folio.holdingsiq.model.PackageCreated;
import org.folio.holdingsiq.model.PackageId;
import org.folio.holdingsiq.model.PackagePost;
import org.folio.holdingsiq.model.PackagePut;
import org.folio.holdingsiq.model.PackageSelectedPayload;
import org.folio.holdingsiq.model.Packages;
import org.folio.holdingsiq.model.Sort;
import org.folio.holdingsiq.service.PackagesHoldingsIQService;
import org.folio.holdingsiq.service.impl.urlbuilder.PackagesFilterableUrlBuilder;

public class PackagesHoldingsIQServiceImpl implements PackagesHoldingsIQService {

  private HoldingsRequestHelper holdingsRequestHelper;

  public PackagesHoldingsIQServiceImpl(Configuration config, Vertx vertx) {
    holdingsRequestHelper = new HoldingsRequestHelper(config, vertx);
  }

  @Override
  public CompletableFuture<PackageByIdData> retrievePackage(PackageId packageId) {
    final String path = VENDORS_PATH + '/' + packageId.getProviderIdPart() + '/' + PACKAGES_PATH + '/' + packageId.getPackageIdPart();
    return holdingsRequestHelper.getRequest(holdingsRequestHelper.constructURL(path), PackageByIdData.class);
  }

  @Override
  public CompletableFuture<Packages> retrievePackages(Long providerId) {
    return retrievePackages(null, null, providerId, null, 1, 25, Sort.NAME);
  }

  @Override
  public CompletableFuture<Packages> retrievePackages(String filterSelected, String filterType, Long providerId,
                                                      String q, int page, int count, Sort sort) {
    String path = new PackagesFilterableUrlBuilder()
      .filterSelected(filterSelected)
      .filterType(filterType)
      .q(q)
      .page(page)
      .count(count)
      .sort(sort)
      .build();

    String packagesPath = providerId == null ? PACKAGES_PATH + "?" : VENDORS_PATH + '/' + providerId + '/' + PACKAGES_PATH + "?";

    return holdingsRequestHelper.getRequest(holdingsRequestHelper.constructURL(packagesPath + path), Packages.class);
  }

  @Override
  public CompletableFuture<PackageByIdData> postPackage(PackagePost entity, Long vendorId) {
    String path = VENDORS_PATH + '/' + vendorId + '/' + PACKAGES_PATH;
    return holdingsRequestHelper.postRequest(holdingsRequestHelper.constructURL(path), entity, PackageCreated.class)
      .thenCompose(packageCreated -> retrievePackage(
        PackageId.builder()
          .providerIdPart(vendorId)
          .packageIdPart(packageCreated.getPackageId()).build())
      );
  }

  @Override
  public CompletableFuture<Void> updatePackage(PackageId packageId, PackagePut packagePut) {
    final String path = VENDORS_PATH + '/' + packageId.getProviderIdPart() + '/' + PACKAGES_PATH + '/' + packageId.getPackageIdPart();
    return holdingsRequestHelper.putRequest(holdingsRequestHelper.constructURL(path), packagePut);
  }

  @Override
  public CompletableFuture<Void> deletePackage(PackageId packageId) {
    final String path = VENDORS_PATH + '/' + packageId.getProviderIdPart() + '/' + PACKAGES_PATH + '/' + packageId.getPackageIdPart();
    return holdingsRequestHelper.putRequest(holdingsRequestHelper.constructURL(path), new PackageSelectedPayload(false));
  }
}
