/*
 * Copyright 2023 Andrew0000
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This file has been modified by SapuSeven on 15 Jan 2025.
 *
 * List of modifications:
 * 	- Remove `Logger` references (is internal and not used)
 * 	- Remove `getOngoingSize()` (is internal and not used)
 * 	- Remove `suspend` modifier from `get()` and `getRaw()`
 *  - Refactor `isOkByMaxAge` (nullability check)
 *
 * Copyright 2025 SapuSeven
 */

// TODO: Delete this if https://github.com/Andrew0000/Universal-Cache/issues/6 is resolved

package com.sapuseven.untis.data.cache

import crocodile8.universal_cache.CachedSourceResult
import crocodile8.universal_cache.FromCache
import crocodile8.universal_cache.keep.Cache
import crocodile8.universal_cache.keep.CachedData
import crocodile8.universal_cache.keep.MemoryCache
import crocodile8.universal_cache.request.Requester
import crocodile8.universal_cache.time.SystemTimeProvider
import crocodile8.universal_cache.time.TimeProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Caching layer that loads a result from the given source or uses cache.
 */
class CachedSource<P : Any, T : Any>(
	source: suspend (params: P) -> T,
	private val cache: Cache<P, T> = MemoryCache(1),
	private val timeProvider: TimeProvider = SystemTimeProvider,
	private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

	private val _updates = MutableSharedFlow<Pair<P, CachedSourceResult<T>>>()
	/**
	 * Flow of every update from the source.
	 * Can be used to observe all future updates.
	 * Note: Order of emission is not guaranteed in high concurrency.
	 */
	val updates: SharedFlow<Pair<P, CachedSourceResult<T>>> = _updates

	private val _errors = MutableSharedFlow<Pair<P, Throwable>>()
	/**
	 * Flow of every exception from the source.
	 * Can be used to observe all future exceptions.
	 * Note: Order of emission is not guaranteed in high concurrency.
	 */
	val errors: SharedFlow<Pair<P, Throwable>> = _errors

	private val requester = Requester(source)
	private val cacheLock = Mutex()

	/**
	 * Clears underlying cache.
	 */
	suspend fun clearCache() {
		cacheLock.withLock {
			cache.clear()
		}
	}

	/**
	 * Get or load a result based on given parameters.
	 *
	 * @param params request parameters, also may be used as key for cache and ongoing sharing.
	 * Must be 1) a data-class or 2) primitive or 3) has equals/hash code implemented for proper distinction.
	 * Use [Unit] ot [Int] or [CachedSourceNoParams] if there are no parameters for request.
	 *
	 * @param fromCache preferred mode of getting from cache.
	 *
	 * @param shareOngoingRequest allows to share ongoing source request without running in parallel.
	 *
	 * @param maxAge maximum age of cached value to be used as result.
	 *
	 * @param additionalKey extra key for cache distinction.
	 * Must be 1) a data-class or 2) primitive or 3) has equals/hash code implemented for proper distinction.
	 *
	 * @return Flow that emits 1 (or 2 in case of [FromCache.CACHED_THEN_LOAD]) elements or exception.
	 */
	fun get(
		params: P,
		fromCache: FromCache,
		shareOngoingRequest: Boolean = true,
		maxAge: Long? = null,
		additionalKey: Any? = null,
	): Flow<T> =
		getRaw(params, fromCache, shareOngoingRequest, maxAge, additionalKey)
			.map { it.value }

	/**
	 * See [get]
	 */
	fun getRaw(
		params: P,
		fromCache: FromCache,
		shareOngoingRequest: Boolean = true,
		maxAge: Long? = null,
		additionalKey: Any? = null,
	): Flow<CachedSourceResult<T>> {
		val lazyFlow = suspend {
			when (fromCache) {

				FromCache.NEVER -> {
					getFromSource(params, additionalKey, shareOngoing = shareOngoingRequest)
				}

				FromCache.IF_FAILED -> {
					getFromSource(params, additionalKey, shareOngoing = shareOngoingRequest)
						.catch {
							val cached = getFromCache(params, additionalKey, maxAge)
							if (cached != null) {
								emit(CachedSourceResult(cached.value, fromCache = true, originTimeStamp = cached.time))
							} else {
								throw it
							}
						}
				}

				FromCache.IF_HAVE -> {
					val cached = getFromCache(params, additionalKey, maxAge)
					if (cached != null) {
						flow { emit(CachedSourceResult(cached.value, fromCache = true, originTimeStamp = cached.time)) }
					} else {
						getFromSource(params, additionalKey, shareOngoing = shareOngoingRequest)
					}
				}

				FromCache.ONLY -> {
					val cached = getFromCache(params, additionalKey, maxAge)
					if (cached != null) {
						flow { emit(CachedSourceResult(cached.value, fromCache = true, originTimeStamp = cached.time)) }
					} else {
						flow { throw NullPointerException("Cache is empty") }
					}
				}

				FromCache.CACHED_THEN_LOAD -> {
					val cached = getFromCache(params, additionalKey, maxAge)
					flow {
						if (cached != null) {
							emitAll(flowOf(CachedSourceResult(cached.value, fromCache = true, originTimeStamp = cached.time)))
						}
						emitAll(
							getFromSource(params, additionalKey, shareOngoing = shareOngoingRequest)
						)
					}
				}
			}
		}
		return flow {
			emitAll(lazyFlow())
		}
	}

	//internal suspend fun getOngoingSize() = requester.getOngoingSize()

	private suspend fun getFromSource(
		params: P,
		additionalKey: Any?,
		shareOngoing: Boolean
	): Flow<CachedSourceResult<T>> =
		when {
			shareOngoing -> requester.requestShared(params, dispatcher)
			else -> requester.request(params, dispatcher)
		}
			.map { CachedSourceResult(it, fromCache = false, originTimeStamp = timeProvider.get()) }
			.onEach {
				putToCache(it.value, params, additionalKey, time = it.originTimeStamp ?: timeProvider.get())
				_updates.emit(params to it)
			}
			.catch {
				_errors.emit(params to it)
				throw it
			}

	private suspend fun getFromCache(params: P, additionalKey: Any?, maxAge: Long?): CachedData<T>? {
		cacheLock.withLock {
			val cachedData = cache.get(params, additionalKey)
			if (cachedData != null && cachedData.isOkByMaxAge(maxAge)) {
				return cachedData
			}
			return null
		}
	}

	private fun <T : Any> CachedData<T>.isOkByMaxAge(maxAge: Long?): Boolean {
		if (maxAge == null) {
			return true
		}

		return time?.let {
			val age = timeProvider.get() - it
			age < maxAge
		} ?: false
	}

	private suspend fun putToCache(value: T, params: P, additionalKey: Any?, time: Long) {
		cacheLock.withLock {
			cache.put(value, params, additionalKey, time)
		}
	}
}
