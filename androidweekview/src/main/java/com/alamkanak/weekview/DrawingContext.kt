package com.alamkanak.weekview

import com.alamkanak.weekview.config.WeekViewConfig
import java.util.*
import kotlin.math.ceil

class DrawingContext(val startPixel: Float) {
	var dayRange: List<Calendar> = emptyList()
	var freeDays: List<Pair<Calendar, Float>> = emptyList()

	companion object {
		internal fun create(config: WeekViewConfig, viewState: WeekViewViewState): DrawingContext {
			val drawConfig = config.drawConfig
			val totalDayWidth = config.totalDayWidth
			val leftDaysWithGaps = (ceil((drawConfig.currentOrigin.x / totalDayWidth).toDouble()) * -1).toInt()
			val startPixel = (drawConfig.currentOrigin.x
					+ totalDayWidth * leftDaysWithGaps
					+ drawConfig.timeColumnWidth)

			val dayRange = mutableListOf<Calendar>()
			if (config.isSingleDay) {
				val day = viewState.firstVisibleDay?.clone() as Calendar
				dayRange.add(day)
			} else {
				var offset = 0
				var today = DateUtils.today().get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY
				if (today == 0)
					today = 7

				if (today - Calendar.MONDAY + 1 < config.numberOfVisibleDays)
					offset = today - Calendar.MONDAY + 1

				val start = leftDaysWithGaps + 1 - offset + days(-offset, leftDaysWithGaps - offset, config.numberOfVisibleDays)
				dayRange.addAll(DateUtils.getDateRange(start, config.numberOfVisibleDays, Calendar.MONDAY, Calendar.MONDAY + config.numberOfVisibleDays - 1))
			}

			return DrawingContext(startPixel).apply { this.dayRange = dayRange }
		}

		fun days(start: Int, end: Int, weekLength: Int): Int {
			var days = (end - start) / weekLength * (7 - weekLength)

			if (start > end)
				days -= 7 - weekLength

			return days
		}
	}
}
