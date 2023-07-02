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

/**
 * <a href="https://guava.dev/CacheBuilder">Discouraged</a> (in favor of <a
 * href="https://github.com/ben-manes/caffeine/wiki">Caffeine</a>) caching utilities.
 * 缓存实用程序
 *
 * <p>The core interface used to represent caches is {@link Cache}. In-memory caches can be
 * configured and created using {@link CacheBuilder}, with cache entries being loaded by {@link
 * CacheLoader}. Statistics about cache performance are exposed using {@link CacheStats}.
 * 用于表示缓存的核心接口是{@link com.google.common.cache.Cache Cache}。
 * 内存中的缓存可以使用{@link com.google.common.cache.CacheBuilder CacheBuilder}进行配置和创建，
 * 缓存条目由{@link com.google.common.cache.CacheLoader CacheLoader}加载。
 * 有关缓存性能的统计信息使用{@link com.google.common.cache.CacheStats CacheStats}公开。
 *
 * <p>See the Guava User Guide article on <a
 * href="https://github.com/google/guava/wiki/CachesExplained">caches</a>.
 * 请参阅关于缓存的Guava用户指南文章。
 *
 * <p>This package is a part of the open-source <a href="https://github.com/google/guava">Guava</a>
 * library.
 *
 * @author Charles Fry
 */
@CheckReturnValue
@ParametersAreNonnullByDefault
package com.google.common.cache;

import com.google.errorprone.annotations.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
