package com.sapuseven.untis.data.timetable

import com.alamkanak.weekview.WeekViewEvent
import com.sapuseven.untis.helpers.DateTimeUtils.toCalendar
import kotlinx.serialization.Serializable
import org.joda.time.LocalDateTime
import java.util.*

class TimegridItem(
		id: Long,
		val startDateTime: LocalDateTime,
		val endDateTime: LocalDateTime,
		contextType: String, // TODO: enum this somewhere
		val periodData: PeriodData)
	: WeekViewEvent<TimegridItem>(id, toCalendar(startDateTime), toCalendar(endDateTime)) {

	init {
		periodData.setup()

		title = periodData.getShortTitle()
		top = when (contextType) {
			"CLASS" -> periodData.getShortTeachers()
			"TEACHER" -> periodData.getShortClasses()
			"STUDENT" -> periodData.getShortTeachers()
			"ROOM" -> periodData.getShortTeachers()
			else -> ""
		}
		bottom = when (contextType) {
			"CLASS" -> periodData.getShortRooms()
			"TEACHER" -> periodData.getShortRooms()
			"STUDENT" -> periodData.getShortRooms()
			"ROOM" -> periodData.getShortClasses()
			else -> ""
		}
	}

	override fun toWeekViewEvent(): WeekViewEvent<TimegridItem> {
		// TODO: This is stupid! Make it compatible with using "this"
		return WeekViewEvent(id, title, top, bottom, startTime, endTime, color, pastColor, false, this)
	}

	fun mergeWith(items: MutableList<TimegridItem>): Boolean {
		var merged = false
		items.toList().forEachIndexed { i, _ ->
			if (i >= items.size)
				return@forEachIndexed

			val candidate = items[i]

			if (candidate.startTime.get(Calendar.DAY_OF_YEAR) != startTime.get(Calendar.DAY_OF_YEAR)) // No YEAR comparison needed as the max difference is a month
				return@forEachIndexed

			if (this.equalsIgnoreTime(candidate)) {
				endTime = candidate.endTime
				periodData.element.endDateTime = candidate.periodData.element.endDateTime
				//candidate.setHidden(true)
				items.removeAt(i)
				merged = true
			}
		}
		return merged
	}

	private fun equalsIgnoreTime(secondItem: TimegridItem): Boolean {
		// TODO: Optimize
		return periodData.element.`is` == secondItem.periodData.element.`is`
				&& periodData.element.can == secondItem.periodData.element.can
				&& periodData.element.elements == secondItem.periodData.element.elements
				&& periodData.element.text == secondItem.periodData.element.text // TODO: Maybe make this (and others) optional
				&& periodData.element.foreColor == secondItem.periodData.element.foreColor
				&& periodData.element.backColor == secondItem.periodData.element.backColor
				&& periodData.element.innerForeColor == secondItem.periodData.element.innerForeColor
				&& periodData.element.innerBackColor == secondItem.periodData.element.innerBackColor
				&& periodData.element.lessonId == secondItem.periodData.element.lessonId
	}
}