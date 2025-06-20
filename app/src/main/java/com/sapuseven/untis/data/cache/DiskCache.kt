package com.sapuseven.untis.data.cache

import android.util.Log
import crocodile8.universal_cache.keep.Cache
import crocodile8.universal_cache.keep.CacheKey
import crocodile8.universal_cache.keep.CachedData
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.cbor.Cbor
import java.io.File
import java.security.MessageDigest

class DiskCache<KeyType : Any, ValueType : Any>(
	private val cacheDir: File, private val valueSerializer: KSerializer<ValueType>
) : Cache<KeyType, ValueType> {
	init {
		if (!cacheDir.exists()) {
			cacheDir.mkdirs()
		}

		if (!cacheDir.isDirectory) {
			throw IllegalArgumentException("cacheDir must be a directory")
		}
	}

	private val cacheLock = Mutex()

	@OptIn(ExperimentalSerializationApi::class)
	override suspend fun get(params: KeyType, additionalKey: Any?): CachedData<ValueType>? {
		val key = CacheKey(params, additionalKey)
		cacheLock.withLock {
			getCacheFile(key).let { cacheFile ->
				if (!cacheFile.exists()) return null

				val decoded = cacheFile.readBytes().let { Cbor.decodeFromByteArray(valueSerializer, it) }
				Log.d("DiskCache", "get: $key -> $decoded")
				return CachedData(decoded, cacheFile.lastModified())
			}
		}
	}

	@OptIn(ExperimentalSerializationApi::class)
	override suspend fun put(value: ValueType, params: KeyType, additionalKey: Any?, time: Long) {
		val key = CacheKey(params, additionalKey)
		cacheLock.withLock {
			val encoded = Cbor.encodeToByteArray(valueSerializer, value)
			Log.d("DiskCache", "put: $key -> $encoded")
			getCacheFile(key).writeBytes(encoded)
		}
	}

	override suspend fun clear() {
		cacheLock.withLock {
			cacheDir.listFiles()?.forEach { it.delete() }
		}
	}

	@OptIn(ExperimentalStdlibApi::class)
	private fun getCacheFile(key: CacheKey<KeyType>): File {
		val md = MessageDigest.getInstance("MD5")
		return File(cacheDir, md.digest(key.toString().toByteArray()).toHexString())
	}
}
