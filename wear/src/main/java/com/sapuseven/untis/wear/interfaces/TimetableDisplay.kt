package com.sapuseven.untis.wear.interfaces

import com.sapuseven.untis.wear.data.TimeGridItem
import com.sapuseven.untis.models.untis.UntisDate

interface TimetableDisplay {
	fun addTimetableItems(items: List<TimeGridItem>, startDate: UntisDate, endDate: UntisDate, timestamp: Long)

	fun onTimetableLoadingError(requestId: Int, code: Int?, message: String?)
}
