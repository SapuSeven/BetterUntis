package com.sapuseven.untis.helpers.timetable

import android.content.Context
import com.sapuseven.untis.models.untis.UntisDate
import com.sapuseven.untis.models.untis.timetable.Period
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import java.io.File
import java.lang.ref.WeakReference


class TimetableCache(val context: WeakReference<Context>) {
	private var target: CacheTarget? = null

	fun setTarget(startDate: UntisDate, endDate: UntisDate, id: Int, type: String) {
		target = CacheTarget(startDate, endDate, id, type)
	}

	fun exists(): Boolean {
		return targetCacheFile(target)?.exists() ?: false
	}

	fun load(): CacheObject? {
		return try {
			Cbor.decodeFromByteArray<CacheObject>(targetCacheFile(target)?.readBytes() ?: ByteArray(0))
		} catch (e: Exception) {
			null
		}
	}

	fun save(items: CacheObject) {
		targetCacheFile(target)?.writeBytes(Cbor.encodeToByteArray<CacheObject>(items))
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
			val startDate: UntisDate,
			val endDate: UntisDate,
			val id: Int,
			val type: String
	) {
		fun getName(): String {
			return String.format("%s-%d-%s-%s", type, id, startDate, endDate)
		}
	}
}
