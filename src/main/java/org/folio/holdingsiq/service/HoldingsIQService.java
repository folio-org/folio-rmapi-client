package org.folio.holdingsiq.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.folio.holdingsiq.model.FilterQuery;
import org.folio.holdingsiq.model.PackageByIdData;
import org.folio.holdingsiq.model.PackageId;
import org.folio.holdingsiq.model.PackagePost;
import org.folio.holdingsiq.model.PackagePut;
import org.folio.holdingsiq.model.Packages;
import org.folio.holdingsiq.model.Proxies;
import org.folio.holdingsiq.model.ResourceId;
import org.folio.holdingsiq.model.ResourcePut;
import org.folio.holdingsiq.model.ResourceSelectedPayload;
import org.folio.holdingsiq.model.RootProxyCustomLabels;
import org.folio.holdingsiq.model.Sort;
import org.folio.holdingsiq.model.Title;
import org.folio.holdingsiq.model.TitlePost;
import org.folio.holdingsiq.model.Titles;
import org.folio.holdingsiq.model.VendorById;
import org.folio.holdingsiq.model.VendorPut;
import org.folio.holdingsiq.model.Vendors;

public interface HoldingsIQService {

  CompletableFuture<Object> verifyCredentials();

  CompletableFuture<Vendors> retrieveProviders(String q, int page, int count, Sort sort);

  CompletableFuture<Titles> retrieveTitles(FilterQuery filterQuery, Sort sort, int page, int count);

  CompletableFuture<Titles> retrieveTitles(Long providerId, Long packageId, FilterQuery filterQuery, Sort sort, int page, int count);

  CompletableFuture<Packages> retrievePackages(Long providerId);

  CompletableFuture<Packages> retrievePackages(
    String filterSelected, String filterType, Long providerId, String q, int page, int count,
    Sort sort);

  CompletableFuture<Long> getVendorId();

  CompletableFuture<VendorById> updateProvider(long id, VendorPut rmapiVendor);

  CompletableFuture<PackageByIdData> retrievePackage(PackageId packageId);

  CompletionStage<Void> updatePackage(PackageId packageId, PackagePut packagePut);

  CompletableFuture<Void> deletePackage(PackageId packageId);

  CompletableFuture<RootProxyCustomLabels> retrieveRootProxyCustomLabels();

  CompletableFuture<Proxies> retrieveProxies();

  CompletableFuture<RootProxyCustomLabels> updateRootProxyCustomLabels(RootProxyCustomLabels rootProxyCustomLabels);

  CompletableFuture<Title> retrieveTitle(long id);

  CompletableFuture<Title> postResource(ResourceSelectedPayload resourcePost, ResourceId resourceId);

  CompletableFuture<PackageByIdData> postPackage(PackagePost entity, Long vendorId);

  CompletableFuture<Title> postTitle(TitlePost titlePost, PackageId packageId);

  CompletionStage<Void> updateResource(ResourceId parsedResourceId, ResourcePut resourcePutBody);

  CompletableFuture<Void> deleteResource(ResourceId parsedResourceId);

  CompletableFuture<VendorById> retrieveProvider(long id);

  CompletableFuture<Title> retrieveResource(ResourceId resourceId);
}
