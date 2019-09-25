package com.alamkanak.weekview

import com.alamkanak.weekview.config.WeekViewConfig
import org.joda.time.DateTime
import kotlin.math.ceil

class DrawingContext(val startPixel: Float) {
	var dayRange: List<DateTime> = emptyList()
	var freeDays: List<Pair<DateTime, Float>> = emptyList()

	companion object {
		internal fun create(config: WeekViewConfig, viewState: WeekViewViewState): DrawingContext {
			val today = DateTime.now()
			val daysScrolled = (ceil((config.drawConfig.currentOrigin.x / config.totalDayWidth).toDouble()) * -1).toInt()
			val startPixel = (config.drawConfig.currentOrigin.x
					+ config.totalDayWidth * daysScrolled
					+ config.drawConfig.timeColumnWidth)

			val dayRange = mutableListOf<DateTime>()
			if (config.isSingleDay) {
				viewState.firstVisibleDay?.let { dayRange.add(it) }
			} else {
				val offset = DateUtils.offsetInWeek(today, config.firstDayOfWeek, config.numberOfVisibleDays)

				dayRange.addAll(DateUtils.getDateRange(today.plusDays(DateUtils.actualDays(daysScrolled, config.numberOfVisibleDays) - offset), config.numberOfVisibleDays, config.firstDayOfWeek, config.numberOfVisibleDays))
			}

			return DrawingContext(startPixel).apply { this.dayRange = dayRange }
		}
	}
}
