/*
 * Copyright (C) 2011 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.common.cache;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Computes or retrieves values, based on a key, for use in populating a {@link LoadingCache}.
 * 缓存加载器，基于键计算或检索值，以用于填充{@link LoadingCache}。
 *
 * <p>Most implementations will only need to implement {@link #load}. Other methods may be
 * overridden as desired.
 * 大多数实现只需要实现{@link #load}。
 * 根据需要，可以覆盖其他方法。
 *
 * <p>Usage example:
 *
 * <pre>{@code
 * CacheLoader<Key, Graph> loader = new CacheLoader<Key, Graph>() {
 *   public Graph load(Key key) throws AnyException {
 *     return createExpensiveGraph(key);
 *   }
 * };
 * LoadingCache<Key, Graph> cache = CacheBuilder.newBuilder().build(loader);
 * }</pre>
 *
 * <p>Since this example doesn't support reloading or bulk loading, it can also be specified much
 * more simply:
 *
 * <pre>{@code
 * CacheLoader<Key, Graph> loader = CacheLoader.from(key -> createExpensiveGraph(key));
 * }</pre>
 *
 * @author Charles Fry
 * @since 10.0
 */
@GwtCompatible(emulated = true)
@ElementTypesAreNonnullByDefault
public abstract class CacheLoader<K, V> {
  /** Constructor for use by subclasses. */
  protected CacheLoader() {}

  // 检索值

  /**
   * Computes or retrieves the value corresponding to {@code key}.
   * 计算或检索与键对应的值。
   *
   * @param key the non-null key whose value should be loaded
   * @return the value associated with {@code key}; <b>must not be null</b>
   * @throws Exception if unable to load the result
   * @throws InterruptedException if this method is interrupted. {@code InterruptedException} is
   *     treated like any other {@code Exception} in all respects except that, when it is caught,
   *     the thread's interrupt status is set
   */
  public abstract V load(K key) throws Exception;

  /**
   * Computes or retrieves a replacement value corresponding to an already-cached {@code key}. This
   * method is called when an existing cache entry is refreshed by {@link
   * CacheBuilder#refreshAfterWrite}, or through a call to {@link LoadingCache#refresh}.
   * 计算或检索与已缓存的键相对应的替换值。
   * 当{@link CacheBuilder#refreshAfterWrite}或通过调用{@link LoadingCache#refresh}刷新现有缓存项时，会调用此方法。
   *
   * <p>This implementation synchronously delegates to {@link #load}. It is recommended that it be
   * overridden with an asynchronous implementation when using {@link
   * CacheBuilder#refreshAfterWrite}.
   * 此实现同步委托{@link #load}。
   * 建议在使用{@link CacheBuilder#refreshAfterWrite}时使用异步实现重写它。
   *
   * <p><b>Note:</b> <i>all exceptions thrown by this method will be logged and then swallowed</i>.
   * 注意：此方法引发的所有异常都将被记录，然后被吞噬。
   *
   * @param key the non-null key whose value should be loaded
   * @param oldValue the non-null old value corresponding to {@code key}
   * @return the future new value associated with {@code key}; <b>must not be null, must not return
   *     null</b>
   * @throws Exception if unable to reload the result
   * @throws InterruptedException if this method is interrupted. {@code InterruptedException} is
   *     treated like any other {@code Exception} in all respects except that, when it is caught,
   *     the thread's interrupt status is set
   * @since 11.0
   */
  @GwtIncompatible // Futures
  public ListenableFuture<V> reload(K key, V oldValue) throws Exception {
    checkNotNull(key);
    checkNotNull(oldValue);
    // 同步委托加载
    return Futures.immediateFuture(this.load(key));
  }

  /**
   * Computes or retrieves the values corresponding to {@code keys}. This method is called by {@link
   * LoadingCache#getAll}.
   * 计算或检索与键对应的值。
   * 此方法由{@link LoadingCache#getAll}调用。
   *
   * <p>If the returned map doesn't contain all requested {@code keys} then the entries it does
   * contain will be cached, but {@code getAll} will throw an exception. If the returned map
   * contains extra keys not present in {@code keys} then all returned entries will be cached, but
   * only the entries for {@code keys} will be returned from {@code getAll}.
   *
   * <p>This method should be overridden when bulk retrieval is significantly more efficient than
   * many individual lookups. Note that {@link LoadingCache#getAll} will defer to individual calls
   * to {@link LoadingCache#get} if this method is not overridden.
   *
   * @param keys the unique, non-null keys whose values should be loaded
   * @return a map from each key in {@code keys} to the value associated with that key; <b>may not
   *     contain null values</b>
   * @throws Exception if unable to load the result
   * @throws InterruptedException if this method is interrupted. {@code InterruptedException} is
   *     treated like any other {@code Exception} in all respects except that, when it is caught,
   *     the thread's interrupt status is set
   * @since 11.0
   */
  public Map<K, V> loadAll(Iterable<? extends K> keys) throws Exception {
    // This will be caught by getAll(), causing it to fall back to multiple calls to
    // LoadingCache.get
    throw new UnsupportedLoadingOperationException();
  }

  /**
   * Returns a cache loader that uses {@code function} to load keys, without supporting either
   * reloading or bulk loading. This allows creating a cache loader using a lambda expression.
   *
   * <p>The returned object is serializable if {@code function} is serializable.
   *
   * @param function the function to be used for loading values; must never return {@code null}
   * @return a cache loader that loads values by passing each key to {@code function}
   */
  public static <K, V> CacheLoader<K, V> from(Function<K, V> function) {
    return new FunctionToCacheLoader<>(function);
  }

  /**
   * Returns a cache loader based on an <i>existing</i> supplier instance. Note that there's no need
   * to create a <i>new</i> supplier just to pass it in here; just subclass {@code CacheLoader} and
   * implement {@link #load load} instead.
   *
   * <p>The returned object is serializable if {@code supplier} is serializable.
   *
   * @param supplier the supplier to be used for loading values; must never return {@code null}
   * @return a cache loader that loads values by calling {@link Supplier#get}, irrespective of the
   *     key
   */
  public static <V> CacheLoader<Object, V> from(Supplier<V> supplier) {
    return new SupplierToCacheLoader<>(supplier);
  }

  private static final class FunctionToCacheLoader<K, V> extends CacheLoader<K, V>
      implements Serializable {
    private final Function<K, V> computingFunction;

    public FunctionToCacheLoader(Function<K, V> computingFunction) {
      this.computingFunction = checkNotNull(computingFunction);
    }

    @Override
    public V load(K key) {
      return computingFunction.apply(checkNotNull(key));
    }

    private static final long serialVersionUID = 0;
  }

  // 异步重新加载

  /**
   * Returns a {@code CacheLoader} which wraps {@code loader}, executing calls to {@link
   * CacheLoader#reload} using {@code executor}.
   * 返回一个封装加载器的CacheLoader，使用executor执行{@link CacheLoader#reload}调用。
   *
   * <p>This method is useful only when {@code loader.reload} has a synchronous implementation, such
   * as {@linkplain #reload the default implementation}.
   * 只有当loader.reload具有同步实现（例如默认实现）时，此方法才有用。
   *
   * @since 17.0
   */
  @GwtIncompatible // Executor + Futures，执行器和异步计算任务
  public static <K, V> CacheLoader<K, V> asyncReloading(
      final CacheLoader<K, V> loader, final Executor executor) {
    checkNotNull(loader);
    checkNotNull(executor);
    return new CacheLoader<K, V>() {
      @Override
      public V load(K key) throws Exception {
        // 同步加载
        return loader.load(key);
      }

      @Override
      public ListenableFuture<V> reload(final K key, final V oldValue) {
        // 异步重新加载任务
        ListenableFutureTask<V> task =
            ListenableFutureTask.create(() -> loader.reload(key, oldValue).get());
        executor.execute(task);
        return task;
      }

      @Override
      public Map<K, V> loadAll(Iterable<? extends K> keys) throws Exception {
        // 同步加载
        return loader.loadAll(keys);
      }
    };
  }

  private static final class SupplierToCacheLoader<V> extends CacheLoader<Object, V>
      implements Serializable {
    private final Supplier<V> computingSupplier;

    public SupplierToCacheLoader(Supplier<V> computingSupplier) {
      this.computingSupplier = checkNotNull(computingSupplier);
    }

    @Override
    public V load(Object key) {
      checkNotNull(key);
      return computingSupplier.get();
    }

    private static final long serialVersionUID = 0;
  }

  /**
   * Exception thrown by {@code loadAll()} to indicate that it is not supported.
   *
   * @since 19.0
   */
  public static final class UnsupportedLoadingOperationException
      extends UnsupportedOperationException {
    // Package-private because this should only be thrown by loadAll() when it is not overridden.
    // Cache implementors may want to catch it but should not need to be able to throw it.
    UnsupportedLoadingOperationException() {}
  }

  /**
   * Thrown to indicate that an invalid response was returned from a call to {@link CacheLoader}.
   *
   * @since 11.0
   */
  public static final class InvalidCacheLoadException extends RuntimeException {
    public InvalidCacheLoadException(String message) {
      super(message);
    }
  }
}
