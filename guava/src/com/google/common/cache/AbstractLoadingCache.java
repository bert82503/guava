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

import com.google.common.annotations.GwtIncompatible;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * This class provides a skeletal implementation of the {@code Cache} interface to minimize the
 * effort required to implement this interface.
 * 此类提供了Cache接口的骨架实现，以最大限度地减少实现该接口所需的工作量。
 *
 * <p>To implement a cache, the programmer needs only to extend this class and provide an
 * implementation for the {@link #get(Object)} and {@link #getIfPresent} methods. {@link
 * #getUnchecked}, {@link #get(Object, Callable)}, and {@link #getAll} are implemented in terms of
 * {@code get}; {@link #getAllPresent} is implemented in terms of {@code getIfPresent}; {@link
 * #putAll} is implemented in terms of {@link #put}, {@link #invalidateAll(Iterable)} is implemented
 * in terms of {@link #invalidate}. The method {@link #cleanUp} is a no-op. All other methods throw
 * an {@link UnsupportedOperationException}.
 *
 * @author Charles Fry
 * @since 11.0
 */
@GwtIncompatible
@ElementTypesAreNonnullByDefault
public abstract class AbstractLoadingCache<K, V> extends AbstractCache<K, V>
    implements LoadingCache<K, V> {

  /** Constructor for use by subclasses. */
  protected AbstractLoadingCache() {}

  // get，检索操作

  @CanIgnoreReturnValue // TODO(b/27479612): consider removing this?
  @Override
  public V getUnchecked(K key) {
    try {
      // 检索键
      return get(key);
    } catch (ExecutionException e) {
      // 捕获异常，转换为不受检的运行时异常
      throw new UncheckedExecutionException(e.getCause());
    }
  }

  @Override
  public ImmutableMap<K, V> getAll(Iterable<? extends K> keys) throws ExecutionException {
    Map<K, V> result = Maps.newLinkedHashMap();
    for (K key : keys) {
      if (!result.containsKey(key)) {
        // 检索键
        result.put(key, get(key));
      }
    }
    return ImmutableMap.copyOf(result);
  }

  @Override
  public final V apply(K key) {
    return getUnchecked(key);
  }

  // 刷新操作

  @Override
  public void refresh(K key) {
    throw new UnsupportedOperationException();
  }
}
