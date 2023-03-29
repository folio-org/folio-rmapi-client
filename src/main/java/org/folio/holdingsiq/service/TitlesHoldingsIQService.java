package org.folio.holdingsiq.service;

import java.util.concurrent.CompletableFuture;

import org.folio.holdingsiq.model.FilterQuery;
import org.folio.holdingsiq.model.PackageId;
import org.folio.holdingsiq.model.Sort;
import org.folio.holdingsiq.model.Title;
import org.folio.holdingsiq.model.TitlePost;
import org.folio.holdingsiq.model.Titles;

public interface TitlesHoldingsIQService {

  CompletableFuture<Title> retrieveTitle(long id);

  CompletableFuture<Titles> retrieveTitles(String rmapiQuery);

  CompletableFuture<Titles> retrieveTitles(FilterQuery filterQuery, String searchType, String packageIds,
                                           Sort sort, int page, int count);

  CompletableFuture<Titles> retrieveTitles(Long providerId, Long packageId, FilterQuery filterQuery, String searchType,
                                           Sort sort, int page, int count);

  CompletableFuture<Title> postTitle(TitlePost titlePost, PackageId packageId);
}
