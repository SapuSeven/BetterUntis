package com.sapuseven.untis.helpers.timetable

import android.content.Context
import com.sapuseven.untis.models.untis.UntisDate
import com.sapuseven.untis.models.untis.timetable.Period
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.CBOR
import kotlinx.serialization.list
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

	fun load(): CacheObject {
		return CBOR.load(CacheObject.serializer(), targetCacheFile(target)?.readBytes() ?: ByteArray(0))
	}

	fun save(items: CacheObject) {
		targetCacheFile(target)?.writeBytes(CBOR.dump(CacheObject.serializer(), items))
	}

	private fun targetCacheFile(target: CacheTarget?): File? {
		return File(context.get()?.cacheDir, target?.getName())
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