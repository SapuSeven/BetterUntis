package com.alamkanak.weekview

import android.graphics.Color
import java.util.*

open class WeekViewEvent<T>(
		var id: Long = 0,
		var title: String = "",
		var top: String = "",
		var bottom: String = "",
		var startTime: Calendar,
		var endTime: Calendar,
		var color: Int = 0,
		var pastColor: Int = 0,
		var data: T? = null
) : WeekViewDisplayable<T>, Comparable<WeekViewEvent<*>> {
	companion object {
		private val DEFAULT_COLOR = Color.parseColor("#9fc6e7") // TODO: Different default color, but this is good for testing
	}

	internal val colorOrDefault: Int
		get() = if (color != 0) color else DEFAULT_COLOR

	internal val pastColorOrDefault: Int
		get() = if (pastColor != 0) pastColor else DEFAULT_COLOR

	fun isSameDay(other: Calendar?): Boolean {
		return if (other == null) false else DateUtils.isSameDay(startTime, other)
	}

	internal fun isSameDay(other: WeekViewEvent<*>): Boolean {
		return DateUtils.isSameDay(startTime, other.startTime)
	}

	internal fun collidesWith(other: WeekViewEvent<*>): Boolean {
		val thisStart = startTime.timeInMillis
		val thisEnd = endTime.timeInMillis
		val otherStart = other.startTime.timeInMillis
		val otherEnd = other.endTime.timeInMillis
		return !(thisStart >= otherEnd || thisEnd <= otherStart)
	}

	override fun compareTo(other: WeekViewEvent<*>): Int {
		val thisStart = this.startTime.timeInMillis
		val otherStart = other.startTime.timeInMillis

		var comparator = thisStart.compareTo(otherStart)
		if (comparator == 0) {
			val thisEnd = this.endTime.timeInMillis
			val otherEnd = other.endTime.timeInMillis
			comparator = thisEnd.compareTo(otherEnd)
		}

		return comparator
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other == null || javaClass != other.javaClass) return false

		val that = other as WeekViewEvent<*>?
		return id == that!!.id
	}

	override fun hashCode(): Int {
		return (id xor id.ushr(32)).toInt()
	}

	/**
	 * Splits the [WeekViewEvent] by day into a list of [WeekViewEvent]s
	 *
	 * @return A list of [WeekViewEvent]
	 */
	internal fun splitWeekViewEvents(): List<WeekViewEvent<T>> {
		val events = ArrayList<WeekViewEvent<T>>()

		// The first millisecond of the next day is still the same day - no need to split events for this
		var endTime = this.endTime.clone() as Calendar
		endTime.add(Calendar.MILLISECOND, -1)

		if (!isSameDay(endTime)) {
			endTime = startTime.clone() as Calendar
			endTime.set(Calendar.HOUR_OF_DAY, 23)
			endTime.set(Calendar.MINUTE, 59)

			val event1 = WeekViewEvent<T>(id, title, top, bottom, startTime, endTime)
			event1.color = color
			event1.pastColor = pastColor
			events.add(event1)

			// Add other days.
			val otherDay = startTime.clone() as Calendar
			otherDay.add(Calendar.DATE, 1)

			while (!DateUtils.isSameDay(otherDay, this.endTime)) {
				val overDay = otherDay.clone() as Calendar
				overDay.set(Calendar.HOUR_OF_DAY, 0)
				overDay.set(Calendar.MINUTE, 0)

				val endOfOverDay = overDay.clone() as Calendar
				endOfOverDay.set(Calendar.HOUR_OF_DAY, 23)
				endOfOverDay.set(Calendar.MINUTE, 59)

				val eventMore = WeekViewEvent<T>(id, title, top, bottom, overDay, endOfOverDay)
				eventMore.color = color
				eventMore.pastColor = pastColor
				events.add(eventMore)

				// Add next day.
				otherDay.add(Calendar.DATE, 1)
			}

			// Add last day.
			val startTime = this.endTime.clone() as Calendar
			startTime.set(Calendar.HOUR_OF_DAY, 0)
			startTime.set(Calendar.MINUTE, 0)

			val event2 = WeekViewEvent<T>(id, title, top, bottom, startTime, endTime)
			event2.color = color
			event2.pastColor = pastColor
			events.add(event2)
		} else {
			events.add(this)
		}

		return events
	}

	override fun toWeekViewEvent(): WeekViewEvent<T> {
		return this
	}
}
