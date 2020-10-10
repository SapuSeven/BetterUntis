package com.sapuseven.untis.data.timetable

import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.views.weekview.WeekViewEvent
import org.joda.time.DateTime

class TimegridItem(
		id: Long,
		val startDateTime: DateTime,
		val endDateTime: DateTime,
		contextType: String,
		val periodData: PeriodData,
		includeOrgIds: Boolean = true
) : WeekViewEvent<TimegridItem>(id, startTime = startDateTime, endTime = endDateTime) {

	init {
		periodData.setup()

		title = periodData.getShort(periodData.subjects, TimetableDatabaseInterface.Type.SUBJECT)
		top =
				if (contextType == TimetableDatabaseInterface.Type.TEACHER.name)
					periodData.getShortSpanned(periodData.classes, TimetableDatabaseInterface.Type.CLASS, includeOrgIds)
				else
					periodData.getShortSpanned(periodData.teachers, TimetableDatabaseInterface.Type.TEACHER, includeOrgIds)
		bottom =
				if (contextType == TimetableDatabaseInterface.Type.ROOM.name)
					periodData.getShortSpanned(periodData.classes, TimetableDatabaseInterface.Type.CLASS, includeOrgIds)
				else
					periodData.getShortSpanned(periodData.rooms, TimetableDatabaseInterface.Type.ROOM, includeOrgIds)

		hasIndicator = !periodData.element.homeWorks.isNullOrEmpty()
				|| periodData.element.text.lesson.isNotEmpty()
				|| periodData.element.text.substitution.isNotEmpty()
				|| periodData.element.text.info.isNotEmpty()
	}

	override fun toWeekViewEvent(): WeekViewEvent<TimegridItem> {
		return WeekViewEvent(id, title, top, bottom, startTime, endTime, color, pastColor, this, hasIndicator)
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
