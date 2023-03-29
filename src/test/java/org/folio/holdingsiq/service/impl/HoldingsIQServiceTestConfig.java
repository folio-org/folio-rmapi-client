package org.folio.holdingsiq.service.impl;

import static org.awaitility.Awaitility.await;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.After;
import org.junit.Rule;

import org.folio.holdingsiq.model.Configuration;
import org.folio.holdingsiq.model.FilterQuery;
import org.folio.holdingsiq.model.PackageCreated;
import org.folio.holdingsiq.model.PackageId;
import org.folio.holdingsiq.model.PackagePost;
import org.folio.holdingsiq.model.ResourceId;
import org.folio.holdingsiq.model.ResourcePut;
import org.folio.holdingsiq.model.TitleCreated;
import org.folio.holdingsiq.model.TitlePost;
import org.folio.holdingsiq.model.Titles;
import org.folio.holdingsiq.model.VendorPut;

public class HoldingsIQServiceTestConfig {

  protected static final String STUB_CUSTOMER_ID = "TEST_CUSTOMER_ID";
  protected static final String STUB_API_KEY = "test_key";
  protected static final String STUB_BASE_URL = "https://sandbox.ebsco.io";
  protected static final String PACKAGE_IDS = "123,23";
  protected static final int PAGE_FOR_PARAM = 1;
  protected static final int COUNT_FOR_PARAM = 5;
  protected static final Long PACKAGE_ID = 2222L;
  protected static final Long TITLE_ID = 3333L;
  protected static final Long VENDOR_ID = 5555L;

  @Rule
  public WireMockRule wiremockServer = new WireMockRule(
    WireMockConfiguration.wireMockConfig()
      .dynamicPort()
      .notifier(new Slf4jNotifier(true)));

  protected FilterQuery filterQuery = FilterQuery.builder().build();
  protected VendorPut vendorPut = VendorPut.builder().build();
  protected ResourcePut resourcePut = ResourcePut.builder().build();
  protected PackagePost packagePost = PackagePost.builder().build();
  protected TitlePost titlePost = TitlePost.builder().build();
  protected TitleCreated titleCreated = TitleCreated.builder().titleId(TITLE_ID).build();
  protected PackageCreated packageCreated = PackageCreated.builder().packageId(PACKAGE_ID).build();
  protected Titles titles = Titles.builder().titleList(Collections.emptyList()).build();
  protected PackageId packageId = PackageId.builder().providerIdPart(VENDOR_ID).packageIdPart(PACKAGE_ID).build();
  protected ResourceId resourceId =
    ResourceId.builder().providerIdPart(VENDOR_ID).packageIdPart(PACKAGE_ID).titleIdPart(TITLE_ID).build();

  @After
  public void tearDown() {
    wiremockServer.resetAll();
  }

  public Configuration getConfiguration() {
    return Configuration.builder().customerId(STUB_CUSTOMER_ID).apiKey(STUB_API_KEY).url(wiremockServer.baseUrl()).build();
  }

  protected boolean isCompletedNormally(CompletableFuture<?> completableFuture) {
    await().atMost(5, TimeUnit.SECONDS).until(completableFuture::isDone);
    return completableFuture.isDone() && !completableFuture.isCompletedExceptionally() && !completableFuture.isCancelled();
  }
}
