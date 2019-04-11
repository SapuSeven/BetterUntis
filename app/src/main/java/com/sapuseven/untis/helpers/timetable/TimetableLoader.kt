package com.sapuseven.untis.helpers.timetable

import android.content.Context
import com.sapuseven.untis.data.connectivity.UntisApiConstants
import com.sapuseven.untis.data.connectivity.UntisAuthentication
import com.sapuseven.untis.data.connectivity.UntisRequest
import com.sapuseven.untis.data.databases.User
import com.sapuseven.untis.data.timetable.PeriodData
import com.sapuseven.untis.data.timetable.TimegridItem
import com.sapuseven.untis.helpers.DateTimeUtils
import com.sapuseven.untis.helpers.SerializationUtils.getJSON
import com.sapuseven.untis.interfaces.TimetableDisplay
import com.sapuseven.untis.models.untis.UntisDate
import com.sapuseven.untis.models.untis.params.TimetableParams
import com.sapuseven.untis.models.untis.response.TimetableResponse
import com.sapuseven.untis.models.untis.timetable.Period
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.joda.time.Instant
import java.lang.ref.WeakReference

class TimetableLoader(context: WeakReference<Context>,
                      private val timetableDisplay: TimetableDisplay,
                      private val user: User,
                      private val timetableDatabaseInterface: TimetableDatabaseInterface) {
	private var api: UntisRequest = UntisRequest()
	private var query: UntisRequest.UntisRequestQuery = UntisRequest.UntisRequestQuery()
	private val cache: TimetableCache = TimetableCache(context)

	fun load(startDate: UntisDate, endDate: UntisDate, id: Int, type: String) = GlobalScope.launch(Dispatchers.Main) {
		// TODO: Add preference and check if a background refresh should be performed
		loadFromCache(startDate, endDate, id, type)
		loadFromServer(startDate, endDate, id, type)
	}

	private fun loadFromCache(startDate: UntisDate, endDate: UntisDate, id: Int, type: String) {
		cache.setTarget(startDate, endDate, id, type)

		if (cache.exists()) {
			val cached = cache.load()
			timetableDisplay.addData(cached.items.map { periodToTimegridItem(it, type) }, startDate, endDate, cached.timestamp)
		}
	}

	private suspend fun loadFromServer(startDate: UntisDate, endDate: UntisDate, id: Int, type: String) {
		cache.setTarget(startDate, endDate, id, type)

		query.url = user.apiUrl ?: UntisApiConstants.DEFAULT_PROTOCOL + user.url + UntisApiConstants.DEFAULT_WEBUNTIS_PATH
		query.school = user.school

		val params = TimetableParams(
				startDate,
				endDate,
				user.masterDataTimestamp,
				0,
				emptyList(),
				id,
				type,
				UntisAuthentication.getAuthObject(user.user, user.key)
		)

		query.data.method = UntisApiConstants.METHOD_GET_TIMETABLE
		query.data.params = listOf(params)

		val userDataResult = api.request(query)
		userDataResult.fold({ data ->
			val untisResponse = getJSON().parse(TimetableResponse.serializer(), data)

			if (untisResponse.result != null) {
				val items = untisResponse.result.timetable.periods
				val timestamp = Instant.now().millis
				timetableDisplay.addData(items.map { periodToTimegridItem(it, type) }, startDate, endDate, timestamp)
				cache.save(TimetableCache.CacheObject(timestamp, items))

				// TODO: Interpret masterData in the response
			} else {
				// TODO: Show error message
			}
		}, { error ->
			println("An error happened: ${error.exception}") // TODO: Localize and notify user
		})
	}

	private fun periodToTimegridItem(period: Period, type: String): TimegridItem {
		return TimegridItem(
				period.id.toLong(),
				DateTimeUtils.isoDateTimeNoSeconds().parseLocalDateTime(period.startDateTime),
				DateTimeUtils.isoDateTimeNoSeconds().parseLocalDateTime(period.endDateTime),
				type,
				PeriodData(timetableDatabaseInterface, period)
		)
	}
}