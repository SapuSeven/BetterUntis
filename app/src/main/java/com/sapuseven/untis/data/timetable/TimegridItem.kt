package com.sapuseven.untis.data.timetable

import com.alamkanak.weekview.WeekViewEvent
import com.sapuseven.untis.helpers.DateTimeUtils.toCalendar
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import org.joda.time.LocalDateTime
import java.util.*

class TimegridItem(
		id: Long,
		val startDateTime: LocalDateTime,
		val endDateTime: LocalDateTime,
		contextType: String,
		val periodData: PeriodData
) : WeekViewEvent<TimegridItem>(id, startTime = toCalendar(startDateTime), endTime = toCalendar(endDateTime)) {

	init {
		periodData.setup()

		title = periodData.getShortTitle()
		top = if (contextType == TimetableDatabaseInterface.Type.TEACHER.name) periodData.getShortClasses() else periodData.getShortTeachers()
		bottom = if (contextType == TimetableDatabaseInterface.Type.ROOM.name) periodData.getShortClasses() else periodData.getShortRooms()
	}

	override fun toWeekViewEvent(): WeekViewEvent<TimegridItem> {
		return WeekViewEvent(id, title, top, bottom, startTime, endTime, color, pastColor, this)
	}

	fun mergeWith(items: MutableList<TimegridItem>): Boolean {
		items.toList().forEachIndexed { i, _ ->
			if (i >= items.size) return@forEachIndexed // Needed because the number of elements can change

			val candidate = items[i]

			if (candidate.startTime.get(Calendar.DAY_OF_YEAR) != startTime.get(Calendar.DAY_OF_YEAR)) return@forEachIndexed

			if (this.equalsIgnoreTime(candidate)) {
				endTime = candidate.endTime
				periodData.element.endDateTime = candidate.periodData.element.endDateTime
				items.removeAt(i)
				return true
			}
		}
		return false
	}

	fun equalsIgnoreTime(secondItem: TimegridItem) = periodData.element.equalsIgnoreTime(secondItem.periodData.element)
}
