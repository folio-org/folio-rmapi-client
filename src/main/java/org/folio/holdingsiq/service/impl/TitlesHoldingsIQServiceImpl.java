package org.folio.holdingsiq.service.impl;

import static java.util.concurrent.CompletableFuture.completedFuture;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import io.vertx.core.Vertx;

import org.folio.holdingsiq.model.FilterQuery;
import org.folio.holdingsiq.model.PackageId;
import org.folio.holdingsiq.model.Sort;
import org.folio.holdingsiq.model.Title;
import org.folio.holdingsiq.model.TitleCreated;
import org.folio.holdingsiq.model.TitlePost;
import org.folio.holdingsiq.model.Titles;
import org.folio.holdingsiq.service.CommonHoldingsService;
import org.folio.holdingsiq.service.TitlesHoldingsIQService;
import org.folio.holdingsiq.service.impl.urlbuilder.TitlesFilterableUrlBuilder;

public class TitlesHoldingsIQServiceImpl extends CommonHoldingsService implements TitlesHoldingsIQService {

  public TitlesHoldingsIQServiceImpl(String customerId, String apiKey, String baseURI, Vertx vertx) {
    super(customerId, apiKey, baseURI, vertx);
  }

  @Override
  public CompletableFuture<Title> retrieveTitle(long id) {
    final String path = TITLES_PATH + '/' + id;
    return this.getRequest(constructURL(path), Title.class);
  }

  @Override
  public CompletableFuture<Titles> retrieveTitles(String rmapiQuery) {
    return getRequest(constructURL(String.format("titles?%s", rmapiQuery)), Titles.class);
  }

  @Override
  public CompletableFuture<Titles> retrieveTitles(FilterQuery filterQuery, Sort sort, int page, int count) {
    String path = new TitlesFilterableUrlBuilder().filter(filterQuery).sort(sort).page(page).count(count).build();

    return getRequest(constructURL(TITLES_PATH + "?" + path), Titles.class).thenCompose(titles -> {
      titles.getTitleList().removeIf(Objects::isNull);
      return completedFuture(titles);
    });
  }

  @Override
  public CompletableFuture<Titles> retrieveTitles(Long providerId, Long packageId, FilterQuery filterQuery, Sort sort,
      int page, int count) {
    String path = new TitlesFilterableUrlBuilder().filter(filterQuery).sort(sort).page(page).count(count).build();

    String titlesPath = VENDORS_PATH + '/' + providerId + '/' + PACKAGES_PATH + '/' + packageId + '/' + TITLES_PATH + "?";

    return this.getRequest(constructURL(titlesPath + path), Titles.class).thenCompose(titles -> {
      titles.getTitleList().removeIf(Objects::isNull);
      return completedFuture(titles);
    });
  }

  @Override
  public CompletableFuture<Title> postTitle(TitlePost titlePost, PackageId packageId) {
    return this.createTitle(titlePost, packageId).thenCompose(titleCreated -> retrieveTitle(titleCreated.getTitleId()));
  }

  private CompletableFuture<TitleCreated> createTitle(TitlePost entity, PackageId packageId) {
    final String path = VENDORS_PATH + '/' + packageId.getProviderIdPart() + '/' + PACKAGES_PATH + '/' + packageId
        .getPackageIdPart() + '/' + TITLES_PATH;
    return this.postRequest(constructURL(path), entity, TitleCreated.class);
  }
}
