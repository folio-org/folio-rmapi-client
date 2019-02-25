package org.folio.holdingsiq.service.impl;

import static java.lang.String.format;
import static java.util.concurrent.CompletableFuture.completedFuture;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import org.folio.holdingsiq.model.FilterQuery;
import org.folio.holdingsiq.model.PackageByIdData;
import org.folio.holdingsiq.model.PackageCreated;
import org.folio.holdingsiq.model.PackageId;
import org.folio.holdingsiq.model.PackagePost;
import org.folio.holdingsiq.model.PackagePut;
import org.folio.holdingsiq.model.PackageSelectedPayload;
import org.folio.holdingsiq.model.Packages;
import org.folio.holdingsiq.model.Proxies;
import org.folio.holdingsiq.model.ResourceDeletePayload;
import org.folio.holdingsiq.model.ResourceId;
import org.folio.holdingsiq.model.ResourcePut;
import org.folio.holdingsiq.model.ResourceSelectedPayload;
import org.folio.holdingsiq.model.RootProxyCustomLabels;
import org.folio.holdingsiq.model.Sort;
import org.folio.holdingsiq.model.Title;
import org.folio.holdingsiq.model.TitleCreated;
import org.folio.holdingsiq.model.TitlePost;
import org.folio.holdingsiq.model.Titles;
import org.folio.holdingsiq.model.VendorById;
import org.folio.holdingsiq.model.VendorPut;
import org.folio.holdingsiq.model.Vendors;
import org.folio.holdingsiq.service.HoldingsIQService;
import org.folio.holdingsiq.service.exception.ResourceNotFoundException;
import org.folio.holdingsiq.service.exception.ResultsProcessingException;
import org.folio.holdingsiq.service.exception.ServiceResponseException;
import org.folio.holdingsiq.service.exception.UnAuthorizedException;
import org.folio.holdingsiq.service.impl.urlbuilder.PackagesFilterableUrlBuilder;
import org.folio.holdingsiq.service.impl.urlbuilder.QueriableUrlBuilder;
import org.folio.holdingsiq.service.impl.urlbuilder.TitlesFilterableUrlBuilder;

// to split into resource oriented (Providers/Titles/...) services
public class HoldingsIQServiceImpl implements HoldingsIQService {

  private static final Logger LOG = LoggerFactory.getLogger(HoldingsIQServiceImpl.class);
  private static final String HTTP_HEADER_CONTENT_TYPE = "Content-type";
  private static final String APPLICATION_JSON = "application/json";
  private static final String HTTP_HEADER_ACCEPT = "Accept";
  private static final String RMAPI_API_KEY = "X-Api-Key";

  private static final String JSON_RESPONSE_ERROR = "Error processing RMAPI Response";
  private static final String INVALID_RMAPI_RESPONSE = "Invalid RMAPI response";

  private static final String VENDOR_NAME_PARAMETER = "vendorname";

  private static final String VENDORS_PATH = "vendors";
  private static final String PACKAGES_PATH = "packages";
  private static final String TITLES_PATH = "titles";

  private static final String VENDOR_LOWER_STRING = "vendor";
  private static final String PROVIDER_LOWER_STRING = "provider";
  private static final String VENDOR_UPPER_STRING = "Vendor";
  private static final String PROVIDER_UPPER_STRING = "Provider";

  private static final String RESOURCE_ENDPOINT_FORMAT = "vendors/%s/packages/%s/titles/%s";

  private String customerId;
  private String apiKey;
  private String baseURI;

  private Vertx vertx;


  public HoldingsIQServiceImpl(String customerId, String apiKey, String baseURI, Vertx vertx) {
    this.customerId = customerId;
    this.apiKey = apiKey;
    this.baseURI = baseURI;
    this.vertx = vertx;
  }

  private <T> void handleRMAPIError(HttpClientResponse response, String query, Buffer body,
      CompletableFuture<T> future) {

    LOG.error("{} status code = [{}] status message = [{}] query = [{}] body = [{}]",
      INVALID_RMAPI_RESPONSE, response.statusCode(), response.statusMessage(), query, body.toString());

    String msgBody = mapVendorToProvider(body.toString());

    if (response.statusCode() == 404) {
      future.completeExceptionally(new ResourceNotFoundException(
        format("Requested resource %s not found", query), response.statusCode(), response.statusMessage(), msgBody, query));
    } else if ((response.statusCode() == 401) || (response.statusCode() == 403)) {
      future.completeExceptionally(new UnAuthorizedException(
        format("Unauthorized Access to %s", query), response.statusCode(), response.statusMessage(), msgBody, query));
    } else {

      future.completeExceptionally(new ServiceResponseException(
        format("%s Code = %s Message = %s Body = %s", INVALID_RMAPI_RESPONSE, response.statusCode(),
          response.statusMessage(), body.toString()),
        response.statusCode(), response.statusMessage(), msgBody, query));
    }
  }

  private String mapVendorToProvider(String msgBody) {
    return msgBody.replace(VENDOR_LOWER_STRING, PROVIDER_LOWER_STRING).replace(VENDOR_UPPER_STRING, PROVIDER_UPPER_STRING);
  }

  private <T> CompletableFuture<T> getRequest(String query, Class<T> clazz) {
    CompletableFuture<T> future = new CompletableFuture<>();

    HttpClient httpClient = vertx.createHttpClient();
    final HttpClientRequest request = httpClient.getAbs(query);

    addRequestHeaders(request);

    LOG.info("RMAPI Service GET absolute URL is: {}", request.absoluteURI());
    executeRequest(query, clazz, future, httpClient, request);

    request.end();

    return future;
  }

  private <T> CompletableFuture<Void> putRequest(String query, T putData) {
    CompletableFuture<Void> future = new CompletableFuture<>();

    HttpClient httpClient = vertx.createHttpClient();
    final HttpClientRequest request = httpClient.putAbs(query);

    addRequestHeaders(request);

    LOG.info("RMAPI Service PUT absolute URL is: {}", request.absoluteURI());

    request.handler(response -> response.bodyHandler(body -> {
      httpClient.close();
      if (response.statusCode() == 204) {
        future.complete(null);
      } else {
        handleRMAPIError(response, query, body, future);
      }

    })).exceptionHandler(future::completeExceptionally);

    String encodedBody = Json.encodePrettily(putData);
    LOG.info("RMAPI Service PUT body is: {}", encodedBody);
    request.end(encodedBody);

    return future;
  }

  private <T, P> CompletableFuture<T> postRequest(String query, P postData, Class<T> clazz){
    CompletableFuture<T> future = new CompletableFuture<>();

    HttpClient httpClient = vertx.createHttpClient();
    final HttpClientRequest request = httpClient.postAbs(query);

    addRequestHeaders(request);

    LOG.info("RMAPI Service POST absolute URL is: {}", request.absoluteURI());
    executeRequest(query, clazz, future, httpClient, request);

    String encodedBody = Json.encodePrettily(postData);
    LOG.info("RMAPI Service POST body is: {}", encodedBody);
    request.end(encodedBody);

    return future;
  }

  private <T> void executeRequest(String query, Class<T> clazz, CompletableFuture<T> future,
    HttpClient httpClient, HttpClientRequest request) {
    request.handler(response -> response.bodyHandler(body -> {
      httpClient.close();
      if (response.statusCode() == 200) {
        try {
          T results = Json.decodeValue(body.toString(), clazz);
          future.complete(results);
        } catch (Exception e) {
          LOG.error("{} - Response = [{}] Target Type = [{}] Cause: [{}]",
                JSON_RESPONSE_ERROR, body.toString(), clazz, e.getMessage());
          future.completeExceptionally(
              new ResultsProcessingException(format("%s for query = %s", JSON_RESPONSE_ERROR, query), e));
        }
      } else {

        handleRMAPIError(response, query, body, future);
      }
    })).exceptionHandler(future::completeExceptionally);
  }

  private void addRequestHeaders(HttpClientRequest request) {
    request.headers().add(HTTP_HEADER_ACCEPT, APPLICATION_JSON);
    request.headers().add(HTTP_HEADER_CONTENT_TYPE, APPLICATION_JSON);
    request.headers().add(RMAPI_API_KEY, apiKey);
  }

  @Override
  public CompletableFuture<Object> verifyCredentials() {
    return this.getRequest(constructURL(VENDORS_PATH + "?search=zz12&offset=1&orderby=vendorname&count=1"), Object.class);
  }

  @Override
  public CompletableFuture<Vendors> retrieveProviders(String q, int page, int count, Sort sort) {
    String query = new QueriableUrlBuilder()
        .q(q)
        .page(page)
        .count(count)
        .sort(sort)
        .nameParameter(VENDOR_NAME_PARAMETER)
        .build();
    return this.getRequest(constructURL(VENDORS_PATH + "?" + query), Vendors.class);
  }

  @Override
  public CompletableFuture<Titles> retrieveTitles(FilterQuery filterQuery, Sort sort, int page, int count) {
    String path = new TitlesFilterableUrlBuilder()
      .filter(filterQuery)
      .sort(sort)
      .page(page)
      .count(count)
      .build();

    return this.getRequest(constructURL(TITLES_PATH + "?" + path), Titles.class)
      .thenCompose(titles -> {
        titles.getTitleList().removeIf(Objects::isNull);
        return completedFuture(titles);
      });
  }

  @Override
  public CompletableFuture<Titles> retrieveTitles(Long providerId, Long packageId, FilterQuery filterQuery, Sort sort, int page, int count) {
    String path = new TitlesFilterableUrlBuilder()
      .filter(filterQuery)
      .sort(sort)
      .page(page)
      .count(count)
      .build();

    String titlesPath =  VENDORS_PATH + '/' + providerId + '/' + PACKAGES_PATH + '/' + packageId + '/' + TITLES_PATH + "?";

    return this.getRequest(constructURL(titlesPath + path), Titles.class)
          .thenCompose(titles -> {
            titles.getTitleList().removeIf(Objects::isNull);
            return completedFuture(titles);
          });
  }

  @Override
  public CompletableFuture<Packages> retrievePackages(Long providerId) {
    return retrievePackages(null, null, providerId, null, 1, 25, Sort.NAME);
  }

  @Override
  public CompletableFuture<Packages> retrievePackages(
    String filterSelected, String filterType, Long providerId, String q, int page, int count,
    Sort sort) {
    String path = new PackagesFilterableUrlBuilder()
      .filterSelected(filterSelected)
      .filterType(filterType)
      .q(q)
      .page(page)
      .count(count)
      .sort(sort)
      .build();

    String packagesPath = providerId == null ? PACKAGES_PATH + "?" : VENDORS_PATH + '/' + providerId + '/' + PACKAGES_PATH + "?";

    return this.getRequest(constructURL(packagesPath + path), Packages.class);
  }

  @Override
  public CompletableFuture<Long> getVendorId(){
    return retrieveRootProxyCustomLabels()
      .thenCompose(rootProxyCustomLabels ->
        CompletableFuture.completedFuture(Long.parseLong(rootProxyCustomLabels.getVendorId())));
  }

  @Override
  public CompletableFuture<VendorById> updateProvider(long id, VendorPut rmapiVendor) {
    final String path = VENDORS_PATH + '/' + id;

    return this.putRequest(constructURL(path), rmapiVendor)
      .thenCompose(vend -> this.retrieveProvider(id));
  }

  @Override
  public CompletableFuture<PackageByIdData> retrievePackage(PackageId packageId) {
    final String path = VENDORS_PATH + '/' + packageId.getProviderIdPart() + '/' + PACKAGES_PATH + '/' + packageId.getPackageIdPart();
    return this.getRequest(constructURL(path), PackageByIdData.class);
  }

  @Override
  public CompletionStage<Void> updatePackage(PackageId packageId, PackagePut packagePut) {
    final String path = VENDORS_PATH + '/' + packageId.getProviderIdPart() + '/' + PACKAGES_PATH + '/' + packageId.getPackageIdPart();

    return this.putRequest(constructURL(path), packagePut);
  }

  @Override
  public CompletableFuture<Void> deletePackage(PackageId packageId) {
    final String path = VENDORS_PATH + '/' + packageId.getProviderIdPart() + '/' + PACKAGES_PATH + '/' + packageId.getPackageIdPart();
    return this.putRequest(constructURL(path), new PackageSelectedPayload(false));
  }

  @Override
  public CompletableFuture<RootProxyCustomLabels> retrieveRootProxyCustomLabels() {
    return this.getRequest(constructURL(""), RootProxyCustomLabels.class);
  }

  @Override
  public CompletableFuture<Proxies> retrieveProxies() {
    return getRequest(constructURL("proxies"), Proxies.class);
  }

  @Override
  public CompletableFuture<RootProxyCustomLabels> updateRootProxyCustomLabels(RootProxyCustomLabels rootProxyCustomLabels) {
    final String path = "";

    // below convertion from rootProxyPutRequest to rootProxyCustomLabels has to be inside a Converter implementation
    /*Proxy.ProxyBuilder pb = Proxy.builder();
    pb.id(rootProxyPutRequest.getData().getAttributes().getProxyTypeId());

    RootProxyCustomLabels.RootProxyCustomLabelsBuilder clb = rootProxyCustomLabels.toBuilder().proxy(pb.build());*/
    /* In RM API - custom labels and root proxy are updated using the same PUT endpoint.
     * We are GETting the object containing both, updating the root proxy with the new one and making a PUT request to RM API.
     * Custom Labels contain only values that have display labels up to a maximum of 5 with fewer possible
     */
    /*clb.labelList(rootProxyCustomLabels.getLabelList());*/

    return this.putRequest(constructURL(path), rootProxyCustomLabels)
      .thenCompose(updatedRootProxy -> this.retrieveRootProxyCustomLabels());
  }

  @Override
  public CompletableFuture<Title> retrieveTitle(long id) {
    final String path = TITLES_PATH + '/' + id;
    return this.getRequest(constructURL(path), Title.class);
  }


  @Override
  public CompletableFuture<Title> postResource(ResourceSelectedPayload resourcePost, ResourceId resourceId) {
    final String path = format(RESOURCE_ENDPOINT_FORMAT, resourceId.getProviderIdPart(), resourceId.getPackageIdPart(), resourceId.getTitleIdPart());
    return this.putRequest(constructURL(path), resourcePost)
      .thenCompose(o -> this.retrieveResource(resourceId));
  }

  @Override
  public CompletableFuture<PackageByIdData> postPackage(PackagePost entity, Long vendorId) {
    String path = VENDORS_PATH + '/' + vendorId + '/' + PACKAGES_PATH;
    return this.postRequest(constructURL(path), entity, PackageCreated.class)
      .thenCompose(packageCreated -> retrievePackage(
          PackageId.builder()
            .providerIdPart(vendorId)
            .packageIdPart(packageCreated.getPackageId()).build())
      );
  }

  private CompletableFuture<TitleCreated> createTitle(TitlePost entity, PackageId packageId) {
    final String path = VENDORS_PATH + '/' + packageId.getProviderIdPart() + '/' + PACKAGES_PATH + '/' + packageId.getPackageIdPart() + '/' + TITLES_PATH;
    return this.postRequest(constructURL(path), entity, TitleCreated.class);
  }

  @Override
  public CompletableFuture<Title> postTitle(TitlePost titlePost, PackageId packageId) {
    return  this.createTitle(titlePost, packageId).thenCompose(titleCreated -> retrieveTitle(titleCreated.getTitleId()));
  }

  /**
   * Constructs full rmapi path
   *
   * @param path
   *          path appended to the end of url
   */
  private String constructURL(String path) {
    String fullPath = format("%s/rm/rmaccounts/%s/%s", baseURI, customerId, path);

    LOG.info("constructurl - path=" + fullPath);
    return fullPath;
  }

  @Override
  public CompletionStage<Void> updateResource(ResourceId parsedResourceId, ResourcePut resourcePutBody) {
    final String path = VENDORS_PATH + '/' + parsedResourceId.getProviderIdPart() + '/' + PACKAGES_PATH + '/' + parsedResourceId.getPackageIdPart() + '/' + TITLES_PATH + '/' + parsedResourceId.getTitleIdPart();
    return this.putRequest(constructURL(path), resourcePutBody);
  }

  @Override
  public CompletableFuture<Void> deleteResource(ResourceId parsedResourceId) {
    final String path = VENDORS_PATH + '/' + parsedResourceId.getProviderIdPart() + '/' + PACKAGES_PATH + '/' + parsedResourceId.getPackageIdPart() + '/' + TITLES_PATH + '/' + parsedResourceId.getTitleIdPart();
    return this.putRequest(constructURL(path), new ResourceDeletePayload(false));
  }

  private CompletableFuture<VendorById> retrieveProvider(long id) {
    CompletableFuture<VendorById> vendorFuture;
    final String path = VENDORS_PATH + '/' + id;
    vendorFuture = this.getRequest(constructURL(path), VendorById.class);
    return vendorFuture;
  }

  private CompletableFuture<Title> retrieveResource(ResourceId resourceId) {
    CompletableFuture<Title> titleFuture;
    final String path = format(RESOURCE_ENDPOINT_FORMAT, resourceId.getProviderIdPart(), resourceId.getPackageIdPart(), resourceId.getTitleIdPart());
    titleFuture = this.getRequest(constructURL(path), Title.class);
    return titleFuture;
  }
}
