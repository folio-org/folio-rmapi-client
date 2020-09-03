package org.folio.holdingsiq.service.config;

import static java.util.Collections.emptyList;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

import static org.folio.holdingsiq.service.config.ConfigTestData.OKAPI_DATA;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.verification.VerificationMode;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import org.folio.cache.VertxCache;
import org.folio.holdingsiq.model.Configuration;
import org.folio.holdingsiq.model.ConfigurationError;
import org.folio.holdingsiq.service.ConfigurationService;
import org.folio.holdingsiq.service.impl.ConfigurationServiceCache;
import org.folio.util.FutureUtils;
import org.folio.util.TokenUtils;
import org.folio.util.UserInfo;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TokenUtils.class)
@PowerMockIgnore({"org.apache.logging.log4j.*"})
public class ConfigurationServiceCacheTest {

  private static final Configuration STUB_CONFIGURATION = Configuration.builder().build();
  private static final UserInfo STUB_USER_INFO = new UserInfo("USER_ID", "USER_NAME");

  @Mock
  private Context context;
  @Mock
  private ConfigurationService configService;
  private VertxCache<String, Configuration> testCache;
  private ConfigurationServiceCache cacheService;


  @Before
  public void setUp() throws Exception {
    openMocks(this).close();
    mockStatic(TokenUtils.class);

    testCache = new VertxCache<>(Vertx.vertx(), 60, "testCache");
    cacheService = new ConfigurationServiceCache(configService, testCache);
  }

  @Test
  public void shouldDelegateToOtherServiceOnCacheMiss() throws ExecutionException, InterruptedException {
    when(TokenUtils.fetchUserInfo(OKAPI_DATA.getApiToken())).thenReturn(completedFuture(STUB_USER_INFO));
    when(configService.retrieveConfiguration(OKAPI_DATA)).thenReturn(completedFuture(STUB_CONFIGURATION));

    Configuration config = cacheService.retrieveConfiguration(OKAPI_DATA).get();

    assertThat(config, sameInstance(STUB_CONFIGURATION));
    verify(configService).retrieveConfiguration(OKAPI_DATA);
  }

  @Test
  public void shouldUseCachedValueOnCacheHit() throws ExecutionException, InterruptedException {
    when(TokenUtils.fetchUserInfo(OKAPI_DATA.getApiToken())).thenReturn(completedFuture(STUB_USER_INFO));
    testCache.putValue(STUB_USER_INFO.getUserId(), STUB_CONFIGURATION);

    Configuration config = cacheService.retrieveConfiguration(OKAPI_DATA).get();

    assertThat(config, sameInstance(STUB_CONFIGURATION));
    verifyNoInteractions(configService);
  }

  @Test
  public void shouldReturnValidConfigurationImmediately() throws ExecutionException, InterruptedException {
    List<ConfigurationError> errors = cacheService.verifyCredentials(Configuration.builder().configValid(true).build(),
        context, OKAPI_DATA).get();

    assertThat(errors, Matchers.empty());

    verifyNoInteractions(configService);
    verifyTokenUtils(never());
  }

  @Test
  public void shouldReturnVerificationErrorsFromService() throws ExecutionException, InterruptedException {
    ConfigurationError error = new ConfigurationError("ERROR");
    when(configService.verifyCredentials(STUB_CONFIGURATION, context, OKAPI_DATA))
      .thenReturn(completedFuture(Collections.singletonList(error)));

    List<ConfigurationError> errors = cacheService.verifyCredentials(STUB_CONFIGURATION, context, OKAPI_DATA).get();

    assertThat(errors, contains(error));

    verify(configService).verifyCredentials(STUB_CONFIGURATION, context, OKAPI_DATA);
    verifyTokenUtils(never());
  }

  @Test
  public void shouldStoreVerifiedConfigurationInCache() throws ExecutionException, InterruptedException {
    when(configService.verifyCredentials(STUB_CONFIGURATION, context, OKAPI_DATA)).thenReturn(completedFuture(emptyList()));
    when(TokenUtils.fetchUserInfo(OKAPI_DATA.getApiToken())).thenReturn(completedFuture(STUB_USER_INFO));

    List<ConfigurationError> errors = cacheService.verifyCredentials(STUB_CONFIGURATION, context, OKAPI_DATA).get();

    assertThat(errors, empty());
    assertThat(testCache.getValue(STUB_USER_INFO.getUserId()),
      equalTo(STUB_CONFIGURATION.toBuilder().configValid(Boolean.TRUE).build()));

    verify(configService).verifyCredentials(STUB_CONFIGURATION, context, OKAPI_DATA);
    verifyTokenUtils(times(1));
  }

  @Test
  public void shouldReturnTokenExceptionAsVerificationError() throws ExecutionException, InterruptedException {
    when(configService.verifyCredentials(STUB_CONFIGURATION, context, OKAPI_DATA)).thenReturn(completedFuture(emptyList()));
    when(TokenUtils.fetchUserInfo(OKAPI_DATA.getApiToken()))
      .thenReturn(FutureUtils.failedFuture(new RuntimeException("EXCEPTION")));

    List<ConfigurationError> errors = cacheService.verifyCredentials(STUB_CONFIGURATION, context, OKAPI_DATA).get();

    assertThat(errors, hasSize(1));
    assertThat(errors.get(0).getMessage(), containsString("EXCEPTION"));
    assertThat(testCache.getValue(STUB_USER_INFO.getUserId()), nullValue());

    verify(configService).verifyCredentials(STUB_CONFIGURATION, context, OKAPI_DATA);
    verifyTokenUtils(times(1));
  }

  private static void verifyTokenUtils(VerificationMode verificationMode) {
    verifyStatic(TokenUtils.class, verificationMode);
    TokenUtils.fetchUserInfo(OKAPI_DATA.getApiToken());
  }
}
