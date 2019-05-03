package org.folio.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.concurrent.CompletableFuture;

import org.glassfish.jersey.internal.util.Producer;
import org.junit.Test;

import io.vertx.core.Vertx;

public class VertxCacheTest {

  private static final String KEY = "key";
  private static final String VALUE = "value";
  private final VertxCache<String, String> testCache = new VertxCache<>(Vertx.vertx(), 60, "testCache");

  @Test
  public void shouldInitiallyReturnNull() {
    assertNull(testCache.getValue(KEY));
  }

  @Test
  public void shouldCacheValueAfterPut() {
    testCache.putValue(KEY, VALUE);
    assertEquals(VALUE, testCache.getValue(KEY));
  }

  @Test
  public void shouldLoadValueOnCacheMiss() {
    Producer<CompletableFuture<String>> loader = spy(new TestProducer());
    CompletableFuture<String> returnedValue = testCache.getValueOrLoad(KEY, loader);
    assertEquals(VALUE, testCache.getValue(KEY));
    assertEquals(VALUE, returnedValue.join());
    verify(loader).call();
  }

  @Test
  public void shouldNotLoadValueOnCacheHit() {
    Producer<CompletableFuture<String>> loader = spy(new TestProducer());
    testCache.putValue(KEY, VALUE);
    CompletableFuture<String> returnedValue = testCache.getValueOrLoad(KEY, loader);
    assertEquals(VALUE, testCache.getValue(KEY));
    assertEquals(VALUE, returnedValue.join());
    verifyZeroInteractions(loader);
  }

  @Test
  public void shouldInvalidateCache() {
    testCache.putValue(KEY, VALUE);
    testCache.invalidateAll();
    assertNull(testCache.getValue(KEY));
  }

  @Test
  public void shouldInvalidateCacheByKey() {
    testCache.putValue(KEY, VALUE);
    testCache.invalidate(KEY);
    assertNull(testCache.getValue(KEY));
  }

  private static class TestProducer implements Producer<CompletableFuture<String>>{
    @Override
    public CompletableFuture<String> call() {
      return CompletableFuture.completedFuture(VALUE);
    }
  }
}
