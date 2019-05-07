package org.folio.holdingsiq.service.config;

import static org.folio.holdingsiq.service.config.ConfigTestData.OKAPI_DATA;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;

import org.folio.cache.VertxCache;
import org.folio.holdingsiq.model.Configuration;
import org.folio.holdingsiq.service.ConfigurationService;
import org.folio.holdingsiq.service.impl.ConfigurationServiceCache;
import org.junit.Test;

import io.vertx.core.Vertx;

public class ConfigurationServiceCacheTest {

  public static final Configuration STUB_CONFIGURATION = Configuration.builder().build();
  private final VertxCache<String, Configuration> testCache = new VertxCache<>(Vertx.vertx(), 60, "testCache");
  private final ConfigurationService mockService = mock(ConfigurationService.class);

  @Test
  public void shouldDelegateToOtherServiceOnCacheMiss() {
    when(mockService.retrieveConfiguration(any())).thenReturn(CompletableFuture.completedFuture(STUB_CONFIGURATION));
    ConfigurationServiceCache cacheService = new ConfigurationServiceCache(mockService, testCache);
    cacheService.retrieveConfiguration(OKAPI_DATA);
    verify(mockService).retrieveConfiguration(any());
  }

  @Test
  public void shouldUseCachedValueOnCacheHit() {
    testCache.putValue(OKAPI_DATA.getTenant(), STUB_CONFIGURATION);
    ConfigurationServiceCache cacheService = new ConfigurationServiceCache(mockService, testCache);
    cacheService.retrieveConfiguration(OKAPI_DATA);
    verifyZeroInteractions(mockService);
  }

}
