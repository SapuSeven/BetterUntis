package com.sapuseven.untis.interfaces

interface TimetableDisplay {
	//fun addTimetableItems(items: List<TimegridItem>, startDate: UntisDate, endDate: UntisDate, timestamp: Long)

	fun onTimetableLoadingError(requestId: Int, code: Int?, message: String?)
}
