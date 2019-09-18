package com.alamkanak.weekview.loaders

import android.util.Log
import com.alamkanak.weekview.DateUtils
import com.alamkanak.weekview.DateUtils.today
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.WeekViewDisplayable
import com.alamkanak.weekview.WeekViewEvent
import java.util.*

/**
 * This class is responsible for loading [WeekViewEvent]s into [WeekView]. It can handle
 * both concrete [WeekViewEvent] objects and [WeekViewDisplayable] objects. The latter is
 * an interface that can be implemented in one's actual data class and handles the conversion to a
 * [WeekViewEvent].
 */
class WeekLoader<T> internal constructor(private var onWeekChangeListener: WeekViewLoader.PeriodChangeListener<T>) : WeekViewLoader<T> {
	companion object {
		private const val WEEKS_PER_YEAR = 53
	}

	override fun toWeekViewPeriodIndex(instance: Calendar): Double {
		return (instance.get(Calendar.YEAR) * WEEKS_PER_YEAR
				+ instance.get(Calendar.WEEK_OF_YEAR)
				+ (instance.get(Calendar.DAY_OF_WEEK) - 1) / 7.0)
	}

	override fun onLoad(periodIndex: Int): List<WeekViewEvent<T>> {
		Log.d("WeekLoader", "onLoad for $periodIndex")

		val year = periodIndex / WEEKS_PER_YEAR
		val week = periodIndex % WEEKS_PER_YEAR - 1

		val startDate = DateUtils.withTimeAtStartOfDay(today())
		startDate.set(Calendar.YEAR, year)
		startDate.set(Calendar.WEEK_OF_YEAR, week)
		startDate.set(Calendar.DAY_OF_WEEK, 1)

		val maxDays = startDate.getActualMaximum(Calendar.DAY_OF_WEEK)

		val endDate = DateUtils.withTimeAtEndOfDay(today())
		endDate.set(Calendar.YEAR, year)
		endDate.set(Calendar.WEEK_OF_YEAR, week)
		endDate.set(Calendar.DAY_OF_WEEK, maxDays)

		val displayableItems = onWeekChangeListener.onPeriodChange(startDate, endDate)

		val events = ArrayList<WeekViewEvent<T>>()
		for (displayableItem in displayableItems) {
			events.add(displayableItem.toWeekViewEvent())
		}

		return events
	}
}
