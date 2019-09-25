package com.alamkanak.weekview

import com.alamkanak.weekview.config.WeekViewConfig
import java.util.*
import kotlin.math.ceil

class DrawingContext(val startPixel: Float) {
	var dayRange: List<Calendar> = emptyList()
	var freeDays: List<Pair<Calendar, Float>> = emptyList()

	companion object {
		internal fun create(config: WeekViewConfig, viewState: WeekViewViewState): DrawingContext {
			val today = DateUtils.today()
			val daysScrolled = (ceil((config.drawConfig.currentOrigin.x / config.totalDayWidth).toDouble()) * -1).toInt()
			val startPixel = (config.drawConfig.currentOrigin.x
					+ config.totalDayWidth * daysScrolled
					+ config.drawConfig.timeColumnWidth)

			val dayRange = mutableListOf<Calendar>()
			if (config.isSingleDay) {
				val day = viewState.firstVisibleDay?.clone() as Calendar
				dayRange.add(day)
			} else {
				val offset = DateUtils.offsetInWeek(today, config.firstDayOfWeek, config.numberOfVisibleDays)
				val startDay = today.clone() as Calendar
				startDay.add(Calendar.DATE, DateUtils.actualDays(daysScrolled, config.numberOfVisibleDays) - offset)

				dayRange.addAll(DateUtils.getDateRange(startDay, config.numberOfVisibleDays, config.firstDayOfWeek, config.numberOfVisibleDays))
			}

			return DrawingContext(startPixel).apply { this.dayRange = dayRange }
		}
	}
}
