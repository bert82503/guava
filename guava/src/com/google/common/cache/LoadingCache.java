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

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ExecutionError;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

/**
 * 加载中的缓存，从键到值的半持久映射。
 * 值由缓存自动加载，并存储在缓存中，直到被逐出或手动无效。
 * 生成实例的常见方法是使用{@link CacheBuilder}。
 * A semi-persistent mapping from keys to values. Values are automatically loaded by the cache, and
 * are stored in the cache until either evicted or manually invalidated. The common way to build
 * instances is using {@link CacheBuilder}.
 *
 * <p>Implementations of this interface are expected to be thread-safe, and can be safely accessed
 * by multiple concurrent threads.
 * 该接口的实现应该是线程安全的，并且可以由多个并发线程安全地访问。
 *
 * <p>When evaluated as a {@link Function}, a cache yields the same result as invoking {@link
 * #getUnchecked}.
 * 当作为函数求值时，缓存会产生与调用{@link #getUnchecked}相同的结果。
 *
 * @param <K> the type of the cache's keys, which are not permitted to be null
 * @param <V> the type of the cache's values, which are not permitted to be null
 * @author Charles Fry
 * @since 11.0
 */
@GwtCompatible
@ElementTypesAreNonnullByDefault
public interface LoadingCache<K, V> extends Cache<K, V>, Function<K, V> {

  // get，检索操作

  /**
   * Returns the value associated with {@code key} in this cache, first loading that value if
   * necessary. No observable state associated with this cache is modified until loading completes.
   * 返回与此缓存中的键关联的值，必要时首先加载该值。
   * 在加载完成之前，不会修改与此缓存相关联的可观察状态。
   *
   * <p>If another call to {@link #get} or {@link #getUnchecked} is currently loading the value for
   * {@code key}, simply waits for that thread to finish and returns its loaded value. Note that
   * multiple threads can concurrently load values for distinct keys.
   * 如果另一个对{@link #get}或{@link #getUnchecked}的调用当前正在加载键的值，只需等待该线程完成并返回其加载的值。
   * 请注意，多个线程可以同时加载不同键的值。
   *
   * <p>Caches loaded by a {@link CacheLoader} will call {@link CacheLoader#load} to load new values
   * into the cache. Newly loaded values are added to the cache using {@code
   * Cache.asMap().putIfAbsent} after loading has completed; if another value was associated with
   * {@code key} while the new value was loading then a removal notification will be sent for the
   * new value.
   * 由CacheLoader加载的缓存将调用{@link CacheLoader#load}将新值加载到缓存中。
   * 加载完成后，使用{@code Cache.asMap().putIfAbsent}将新加载的值添加到缓存中；
   * 如果在加载新值时另一个值与键相关联，则将为新值发送删除通知。
   *
   * <p>If the cache loader associated with this cache is known not to throw checked exceptions,
   * then prefer {@link #getUnchecked} over this method.
   * 如果已知与此缓存关联的缓存加载器不会抛出已检查的异常，则首选{@link #getUnchecked}而不是此方法。
   *
   * @throws ExecutionException if a checked exception was thrown while loading the value. ({@code
   *     ExecutionException} is thrown <a
   *     href="https://github.com/google/guava/wiki/CachesExplained#interruption">even if
   *     computation was interrupted by an {@code InterruptedException}</a>.)
   * @throws UncheckedExecutionException if an unchecked exception was thrown while loading the
   *     value
   * @throws ExecutionError if an error was thrown while loading the value
   */
  @CanIgnoreReturnValue // TODO(b/27479612): consider removing this?
  V get(K key) throws ExecutionException;

  /**
   * Returns the value associated with {@code key} in this cache, first loading that value if
   * necessary. No observable state associated with this cache is modified until loading completes.
   * Unlike {@link #get}, this method does not throw a checked exception, and thus should only be
   * used in situations where checked exceptions are not thrown by the cache loader.
   * 返回与此缓存中的键关联的值，必要时首先加载该值。
   * 在加载完成之前，不会修改与此缓存相关联的可观察状态。
   *
   * <p>If another call to {@link #get} or {@link #getUnchecked} is currently loading the value for
   * {@code key}, simply waits for that thread to finish and returns its loaded value. Note that
   * multiple threads can concurrently load values for distinct keys.
   *
   * <p>Caches loaded by a {@link CacheLoader} will call {@link CacheLoader#load} to load new values
   * into the cache. Newly loaded values are added to the cache using {@code
   * Cache.asMap().putIfAbsent} after loading has completed; if another value was associated with
   * {@code key} while the new value was loading then a removal notification will be sent for the
   * new value.
   *
   * <p><b>Warning:</b> this method silently converts checked exceptions to unchecked exceptions,
   * and should not be used with cache loaders which throw checked exceptions. In such cases use
   * {@link #get} instead.
   *
   * @throws UncheckedExecutionException if an exception was thrown while loading the value. (As
   *     explained in the last paragraph above, this should be an unchecked exception only.)
   * @throws ExecutionError if an error was thrown while loading the value
   */
  @CanIgnoreReturnValue // TODO(b/27479612): consider removing this?
  V getUnchecked(K key);

  /**
   * Returns a map of the values associated with {@code keys}, creating or retrieving those values
   * if necessary. The returned map contains entries that were already cached, combined with newly
   * loaded entries; it will never contain null keys or values.
   * 返回与键关联的值的映射，如有必要，可创建或检索这些值。
   * 返回的映射包含已缓存的条目以及新加载的条目；它永远不会包含空键或值。
   *
   * <p>Caches loaded by a {@link CacheLoader} will issue a single request to {@link
   * CacheLoader#loadAll} for all keys which are not already present in the cache. All entries
   * returned by {@link CacheLoader#loadAll} will be stored in the cache, over-writing any
   * previously cached values. This method will throw an exception if {@link CacheLoader#loadAll}
   * returns {@code null}, returns a map containing null keys or values, or fails to return an entry
   * for each requested key.
   * 由CacheLoader加载的缓存将向{@link CacheLoader#loadAll}发出一个请求，请求缓存中尚未存在的所有键。
   * {@link CacheLoader#loadAll}返回的所有条目都将存储在缓存中，覆盖之前缓存的任何值。
   * 如果{@link CacheLoader#loadAll}返回null、返回包含null键或值的映射或未能为每个请求的键返回条目，则此方法将引发异常。
   *
   * <p>Note that duplicate elements in {@code keys}, as determined by {@link Object#equals}, will
   * be ignored.
   * 请注意，键中由{@link Object#equals}确定的重复元素将被忽略。
   *
   * @throws ExecutionException if a checked exception was thrown while loading the value. ({@code
   *     ExecutionException} is thrown <a
   *     href="https://github.com/google/guava/wiki/CachesExplained#interruption">even if
   *     computation was interrupted by an {@code InterruptedException}</a>.)
   * @throws UncheckedExecutionException if an unchecked exception was thrown while loading the
   *     values
   * @throws ExecutionError if an error was thrown while loading the values
   * @since 11.0
   */
  @CanIgnoreReturnValue // TODO(b/27479612): consider removing this
  ImmutableMap<K, V> getAll(Iterable<? extends K> keys) throws ExecutionException;

  // 类型转换函数

  /**
   * @deprecated Provided to satisfy the {@code Function} interface; use {@link #get} or {@link
   *     #getUnchecked} instead.
   * @throws UncheckedExecutionException if an exception was thrown while loading the value. (As
   *     described in the documentation for {@link #getUnchecked}, {@code LoadingCache} should be
   *     used as a {@code Function} only with cache loaders that throw only unchecked exceptions.)
   */
  @Deprecated
  @Override
  V apply(K key);

  // 刷新操作

  /**
   * Loads a new value for {@code key}, possibly asynchronously. While the new value is loading the
   * previous value (if any) will continue to be returned by {@code get(key)} unless it is evicted.
   * If the new value is loaded successfully it will replace the previous value in the cache; if an
   * exception is thrown while refreshing the previous value will remain, <i>and the exception will
   * be logged (using {@link java.util.logging.Logger}) and swallowed</i>.
   * 为键加载一个新值，可能是异步的。
   * 当新值正在加载时，{@code get(key)}将继续返回以前的值（如果有），除非它被收回。
   * 如果成功加载新值，它将替换缓存中的先前值；如果在刷新时抛出异常，则会保留以前的值，并且会记录（使用java.util.logging.Logger）并吞下该异常。
   *
   * <p>Caches loaded by a {@link CacheLoader} will call {@link CacheLoader#reload} if the cache
   * currently contains a value for {@code key}, and {@link CacheLoader#load} otherwise. Loading is
   * asynchronous only if {@link CacheLoader#reload} was overridden with an asynchronous
   * implementation.
   * 如果缓存当前包含键的值，则由CacheLoader加载的缓存将调用{@link CacheLoader#reload}，否则将调用{@link CacheLoader#load}。
   * 只有当{@link CacheLoader#reload}被异步实现重写时，加载才是异步的。
   *
   * <p>Returns without doing anything if another thread is currently loading the value for {@code
   * key}. If the cache loader associated with this cache performs refresh asynchronously then this
   * method may return before refresh completes.
   * 如果另一个线程当前正在加载键的值，则返回而不执行任何操作。
   * 如果与此缓存相关联的缓存加载器异步执行刷新，则此方法可能会在刷新完成之前返回。
   *
   * @since 11.0
   */
  void refresh(K key);

  // 并发映射表，视图转换

  /**
   * {@inheritDoc}
   *
   * <p><b>Note that although the view <i>is</i> modifiable, no method on the returned map will ever
   * cause entries to be automatically loaded.</b>
   */
  @Override
  ConcurrentMap<K, V> asMap();
}
