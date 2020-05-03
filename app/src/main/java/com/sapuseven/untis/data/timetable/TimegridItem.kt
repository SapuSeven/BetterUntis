package com.sapuseven.untis.data.timetable

import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.views.weekview.WeekViewEvent
import org.joda.time.DateTime

class TimegridItem(
		id: Long,
		val startDateTime: DateTime,
		val endDateTime: DateTime,
		contextType: String,
		val periodData: PeriodData
) : WeekViewEvent<TimegridItem>(id, startTime = startDateTime, endTime = endDateTime) {

	init {
		periodData.setup()

		title = periodData.getShortTitle()
		top = if (contextType == TimetableDatabaseInterface.Type.TEACHER.name) periodData.getShortClasses() else periodData.getShortSpanned(periodData.teachers, TimetableDatabaseInterface.Type.TEACHER)
		bottom = if (contextType == TimetableDatabaseInterface.Type.ROOM.name) periodData.getShortClasses() else periodData.getShortSpanned(periodData.rooms, TimetableDatabaseInterface.Type.ROOM)
	}

	override fun toWeekViewEvent(): WeekViewEvent<TimegridItem> {
		return WeekViewEvent(id, title, top, bottom, startTime, endTime, color, pastColor, this)
	}

	fun mergeWith(items: MutableList<TimegridItem>): Boolean {
		items.toList().forEachIndexed { i, _ ->
			if (i >= items.size) return@forEachIndexed // Needed because the number of elements can change

			val candidate = items[i]

			if (candidate.startTime.dayOfYear != startTime.dayOfYear) return@forEachIndexed

			if (this.equalsIgnoreTime(candidate)) {
				endTime = candidate.endTime
				periodData.element.endDateTime = candidate.periodData.element.endDateTime
				items.removeAt(i)
				return true
			}
		}
		return false
	}

	fun mergeValuesWith(item: TimegridItem) {
		periodData.apply {
			classes.addAll(item.periodData.classes)
			teachers.addAll(item.periodData.teachers)
			subjects.addAll(item.periodData.subjects)
			rooms.addAll(item.periodData.rooms)
		}
	}

	fun equalsIgnoreTime(secondItem: TimegridItem) = periodData.element.equalsIgnoreTime(secondItem.periodData.element)
}
