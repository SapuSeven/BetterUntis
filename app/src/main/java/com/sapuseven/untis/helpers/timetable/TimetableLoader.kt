package com.sapuseven.untis.helpers.timetable

import android.content.Context
import android.util.Log
import com.sapuseven.untis.data.connectivity.UntisApiConstants
import com.sapuseven.untis.data.connectivity.UntisAuthentication
import com.sapuseven.untis.data.connectivity.UntisRequest
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.data.timetable.PeriodData
import com.sapuseven.untis.data.timetable.TimegridItem
import com.sapuseven.untis.helpers.SerializationUtils.getJSON
import com.sapuseven.untis.interfaces.TimetableDisplay
import com.sapuseven.untis.models.untis.UntisDate
import com.sapuseven.untis.models.untis.params.TimetableParams
import com.sapuseven.untis.models.untis.response.TimetableResponse
import com.sapuseven.untis.models.untis.timetable.Period
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import org.joda.time.Instant
import java.lang.ref.WeakReference

class TimetableLoader(
		private val context: WeakReference<Context>,
		private val timetableDisplay: TimetableDisplay,
		private val user: UserDatabase.User,
		private val timetableDatabaseInterface: TimetableDatabaseInterface
) {
	companion object {
		const val FLAG_LOAD_CACHE = 0b00000001
		const val FLAG_LOAD_SERVER = 0b00000010

		const val CODE_CACHE_MISSING = 1
		const val CODE_REQUEST_FAILED = 2
		const val CODE_REQUEST_PARSING_EXCEPTION = 3
	}

	private val requestList = ArrayList<TimetableLoaderTarget>()

	private var api: UntisRequest = UntisRequest()
	private var query: UntisRequest.UntisRequestQuery = UntisRequest.UntisRequestQuery(user)

	fun load(target: TimetableLoaderTarget, flags: Int = 0, proxyHost: String? = null) = GlobalScope.launch(Dispatchers.Main) {
		requestList.add(target)

		if (flags and FLAG_LOAD_CACHE > 0)
			loadFromCache(target, requestList.size - 1)
		if (flags and FLAG_LOAD_SERVER > 0)
			loadFromServer(target, requestList.size - 1, proxyHost)
	}

	private fun loadFromCache(target: TimetableLoaderTarget, requestId: Int) {
		val cache = TimetableCache(context)
		cache.setTarget(target.startDate, target.endDate, target.id, target.type)

		if (cache.exists()) {
			Log.d("TimetableLoaderDebug", "target $target (requestId $requestId): cached file found")
			cache.load()?.let { cacheObject ->
				timetableDisplay.addTimetableItems(cacheObject.items.map { periodToTimegridItem(it, target.type) }, target.startDate, target.endDate, cacheObject.timestamp)
			} ?: run {
				cache.delete()
				Log.d("TimetableLoaderDebug", "target $target (requestId $requestId): cached file corrupted")
				timetableDisplay.onTimetableLoadingError(requestId, CODE_CACHE_MISSING, "cached timetable corrupted")
			}
		} else {
			Log.d("TimetableLoaderDebug", "target $target (requestId $requestId): cached file missing")
			timetableDisplay.onTimetableLoadingError(requestId, CODE_CACHE_MISSING, "no cached timetable found")
		}
	}

	private suspend fun loadFromServer(target: TimetableLoaderTarget, requestId: Int, proxyHost: String? = null) {
		val cache = TimetableCache(context)
		cache.setTarget(target.startDate, target.endDate, target.id, target.type)

		query.proxyHost = proxyHost

		val params = TimetableParams(
				target.id,
				target.type,
				target.startDate,
				target.endDate,
				user.masterDataTimestamp,
				0, // TODO: Figure out how timetableTimestamp works
				emptyList(),
				if (user.anonymous) UntisAuthentication.createAuthObject() else UntisAuthentication.createAuthObject(user.user, user.key)
		)

		query.data.id = requestId.toString()
		query.data.method = UntisApiConstants.METHOD_GET_TIMETABLE
		query.data.params = listOf(params)

		val userDataResult = api.request(query)
		userDataResult.fold({ data ->
			try {
				val untisResponse = getJSON().decodeFromString<TimetableResponse>(data)

				if (untisResponse.result != null) {
					Log.d("TimetableLoaderDebug", "target $target (requestId $requestId): network request success, returning")

					val items = untisResponse.result.timetable.periods
					val timestamp = Instant.now().millis
					timetableDisplay.addTimetableItems(items.map { periodToTimegridItem(it, target.type) }, target.startDate, target.endDate, timestamp)
					Log.d("TimetableLoaderDebug", "target $target (requestId $requestId): saving to cache: $cache")
					cache.save(TimetableCache.CacheObject(timestamp, items))

					// TODO: Interpret masterData in the response
				} else {
					Log.d("TimetableLoaderDebug", "target $target (requestId $requestId): network request failed at Untis API level")
					timetableDisplay.onTimetableLoadingError(requestId, untisResponse.error?.code, untisResponse.error?.message)
				}
			} catch (e: Exception) {
				val msg = when (e) {
					//is JsonDecodingException -> formatJsonParsingException(e, data)
					else -> e.toString()
				}
				timetableDisplay.onTimetableLoadingError(requestId, CODE_REQUEST_PARSING_EXCEPTION, msg)
			}
		}, { error ->
			Log.d("TimetableLoaderDebug", "target $target (requestId $requestId): network request failed at OS level")
			timetableDisplay.onTimetableLoadingError(requestId, CODE_REQUEST_FAILED, error.message)
		})
	}

	/*private fun formatJsonParsingException(e: JsonDecodingException, jsonData: String): String {
		val errorMargin = 20
		val errorIndex: Int? = e.message?.let {
			it.split(" ")[3].let { i ->
				i.substring(0, i.length - 1)
			}.toIntOrNull()
		}

		return e.toString() + if (errorIndex != null)
			"\n(near " + jsonData.substring((errorIndex - errorMargin).coerceAtLeast(0), (errorIndex + errorMargin).coerceAtMost(jsonData.length)) + ")"
		else ""
	}*/

	private fun periodToTimegridItem(period: Period, type: String): TimegridItem {
		return TimegridItem(
				period.id.toLong(),
				period.startDateTime.toLocalDateTime().toDateTime(),
				period.endDateTime.toLocalDateTime().toDateTime(),
				type,
				PeriodData(timetableDatabaseInterface, period)
		)
	}

	fun repeat(requestId: Int, flags: Int = 0, proxyHost: String? = null) {
		Log.d("TimetableLoaderDebug", "target ${requestList[requestId]} (requestId $requestId): repeat")
		load(requestList[requestId], flags, proxyHost)
	}

	data class TimetableLoaderTarget(
			val startDate: UntisDate,
			val endDate: UntisDate,
			val id: Int,
			val type: String
	)
}
