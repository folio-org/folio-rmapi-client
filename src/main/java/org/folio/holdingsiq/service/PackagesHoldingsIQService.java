package org.folio.holdingsiq.service;

import java.util.concurrent.CompletableFuture;

import org.folio.holdingsiq.model.PackageByIdData;
import org.folio.holdingsiq.model.PackageId;
import org.folio.holdingsiq.model.PackagePost;
import org.folio.holdingsiq.model.PackagePut;
import org.folio.holdingsiq.model.Packages;
import org.folio.holdingsiq.model.Sort;

public interface PackagesHoldingsIQService {

  CompletableFuture<PackageByIdData> retrievePackage(PackageId packageId);
  CompletableFuture<Packages> retrievePackages(Long providerId);
  CompletableFuture<Packages> retrievePackages(
    String filterSelected, String filterType, Long providerId, String q, int page, int count, Sort sort);
  CompletableFuture<PackageByIdData> postPackage(PackagePost entity, Long vendorId);
  CompletableFuture<Void> updatePackage(PackageId packageId, PackagePut packagePut);
  CompletableFuture<Void> deletePackage(PackageId packageId);
}
