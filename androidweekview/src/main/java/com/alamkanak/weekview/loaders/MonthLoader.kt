package com.alamkanak.weekview.loaders

import com.alamkanak.weekview.DateUtils
import com.alamkanak.weekview.DateUtils.today
import com.alamkanak.weekview.WeekViewDisplayable
import com.alamkanak.weekview.WeekViewEvent
import java.util.*

/**
 * This class is responsible for loading [WeekViewEvent]s into [WeekView]. It can handle
 * both concrete [WeekViewEvent] objects and [WeekViewDisplayable] objects. The latter is
 * an interface that can be implemented in one's actual data class and handles the conversion to a
 * [WeekViewEvent].
 */
class MonthLoader<T> internal constructor(var onMonthChangeListener: MonthChangeListener<T>) : WeekViewLoader<T> {
	override fun toWeekViewPeriodIndex(instance: Calendar): Double {
		return ((instance.get(Calendar.YEAR) * 12).toDouble()
				+ instance.get(Calendar.MONTH).toDouble()
				+ (instance.get(Calendar.DAY_OF_MONTH) - 1) / 30.0)
	}

	override fun onLoad(periodIndex: Int): List<WeekViewEvent<T>> {
		val year = periodIndex / 12
		val month = periodIndex % 12

		val startDate = DateUtils.withTimeAtStartOfDay(today())
		startDate.set(Calendar.YEAR, year)
		startDate.set(Calendar.MONTH, month)
		startDate.set(Calendar.DAY_OF_MONTH, 1)

		val maxDays = startDate.getActualMaximum(Calendar.DAY_OF_MONTH)

		val endDate = DateUtils.withTimeAtEndOfDay(today())
		endDate.set(Calendar.YEAR, year)
		endDate.set(Calendar.MONTH, month)
		endDate.set(Calendar.DAY_OF_MONTH, maxDays)

		val displayableItems = onMonthChangeListener.onMonthChange(startDate, endDate)

		val events = ArrayList<WeekViewEvent<T>>()
		for (displayableItem in displayableItems) {
			events.add(displayableItem.toWeekViewEvent())
		}

		return events
	}

	interface MonthChangeListener<T> {

		/**
		 * Called when the month displayed in the [WeekView] changes.
		 * @param startDate A [Calendar] representing the start date of the month
		 * @param endDate A [Calendar] representing the end date of the month
		 * @return The list of [WeekViewDisplayable] of the provided month
		 */
		fun onMonthChange(startDate: Calendar, endDate: Calendar): List<WeekViewDisplayable<T>>
	}
}
