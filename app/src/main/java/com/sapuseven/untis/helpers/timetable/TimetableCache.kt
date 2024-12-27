package com.sapuseven.untis.helpers.timetable

import android.content.Context
import com.sapuseven.untis.api.model.untis.timetable.Period
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import java.io.File
import java.lang.ref.WeakReference
import java.time.LocalDate


class TimetableCache(val context: WeakReference<Context>) {
	private var target: CacheTarget? = null

	fun setTarget(startDate: LocalDate, endDate: LocalDate, id: Int, type: String, userId: Long) {
		target = CacheTarget(startDate, endDate, id, type, userId)
	}

	fun exists(): Boolean {
		return targetCacheFile(target)?.exists() ?: false
	}

	@OptIn(ExperimentalSerializationApi::class)
	fun load(): CacheObject? {
		return try {
			Cbor.decodeFromByteArray<CacheObject>(targetCacheFile(target)?.readBytes() ?: ByteArray(0))
		} catch (e: Exception) {
			null
		}
	}

	@OptIn(ExperimentalSerializationApi::class)
	fun save(items: CacheObject) {
		targetCacheFile(target)?.writeBytes(Cbor.encodeToByteArray(items))
	}

	private fun targetCacheFile(target: CacheTarget?): File? {
		return File(context.get()?.cacheDir, target?.getName() ?: "default")
	}

	override fun toString(): String {
		return target?.getName() ?: "null"
	}

	fun delete() {
		targetCacheFile(target)?.delete()
	}

	@Serializable
	data class CacheObject(
			val timestamp: Long,
			val items: List<Period>
	)

	private inner class CacheTarget(
			val startDate: LocalDate,
			val endDate: LocalDate,
			val id: Int,
			val type: String,
			val userId: Long
	) {
		fun getName(): String {
			return String.format("%d-%s-%d-%s-%s", userId, type, id, startDate, endDate)
		}
	}
}
