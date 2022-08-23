package com.sapuseven.untis.views.weekview

import android.graphics.Color
import org.joda.time.DateTime
import java.util.*

open class WeekViewEvent<T>(
		var id: Long = 0,
		var title: CharSequence = "",
		var top: CharSequence = "",
		var bottom: CharSequence = "",
		var startTime: DateTime,
		var endTime: DateTime,
		var color: Int = 0,
		var pastColor: Int = 0,
		var textColor: Int = 0,
		var data: T? = null,
		var hasIndicator: Boolean = false
) : WeekViewDisplayable<T>, Comparable<WeekViewEvent<*>> {
	companion object {
		private val DEFAULT_COLOR = Color.parseColor("#9fc6e7") // TODO: Different default color, but this is good for testing
	}

	internal val colorOrDefault: Int
		get() = if (color != 0) color else DEFAULT_COLOR

	internal val pastColorOrDefault: Int
		get() = if (pastColor != 0) pastColor else DEFAULT_COLOR

	internal val textColorOrDefault: Int
		get() = if (textColor != 0) textColor else Color.BLACK

	internal fun isSameDay(other: DateTime?) = if (other == null) false else DateUtils.isSameDay(startTime, other)

	internal fun isSameDay(other: WeekViewEvent<*>) = DateUtils.isSameDay(startTime, other.startTime)

	internal fun collidesWith(other: WeekViewEvent<*>): Boolean {
		val thisStart = startTime.millis
		val thisEnd = endTime.millis
		val otherStart = other.startTime.millis
		val otherEnd = other.endTime.millis
		return !(thisStart >= otherEnd || thisEnd <= otherStart)
	}

	override fun compareTo(other: WeekViewEvent<*>): Int {
		val thisStart = this.startTime.millis
		val otherStart = other.startTime.millis

		var comparator = thisStart.compareTo(otherStart)
		if (comparator == 0) {
			val thisEnd = this.endTime.millis
			val otherEnd = other.endTime.millis
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
		var endTime = this.endTime.minusMillis(1)

		if (!isSameDay(endTime)) {
			endTime = startTime.withTime(23, 59, 0, 0)

			val event1 = WeekViewEvent<T>(id, title, top, bottom, startTime, endTime)
			event1.color = color
			event1.pastColor = pastColor
			events.add(event1)

			// Add other days.
			var otherDay = startTime.plusDays(1)

			while (!DateUtils.isSameDay(otherDay, this.endTime)) {
				val overDay = otherDay.withTime(0, 0, 0, 0)

				val endOfOverDay = overDay.withTime(23, 59, 0, 0)

				val eventMore = WeekViewEvent<T>(id, title, top, bottom, overDay, endOfOverDay)
				eventMore.color = color
				eventMore.pastColor = pastColor
				events.add(eventMore)

				// Add next day.
				otherDay = otherDay.plusDays(1)
			}

			// Add last day.
			val startTime = this.endTime.withTime(0, 0, 0, 0)

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
