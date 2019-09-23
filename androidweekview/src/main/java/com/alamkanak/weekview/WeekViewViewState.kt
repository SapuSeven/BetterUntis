package com.alamkanak.weekview

import com.alamkanak.weekview.config.WeekViewConfig
import java.lang.Math.max
import java.util.*

class WeekViewViewState {
	internal var scrollToDay: Calendar? = null
	internal var scrollToHour = -1

	internal var isFirstDraw = true
	internal var areDimensionsInvalid = true

	var firstVisibleDay: Calendar? = null

	internal var shouldRefreshEvents: Boolean = false

	internal fun update(config: WeekViewConfig, listener: UpdateListener) {
		if (!areDimensionsInvalid) {
			return
		}

		val height = WeekView.viewHeight

		config.effectiveMinHourHeight = max(
				config.minHourHeight,
				((height - config.drawConfig.headerHeight) / config.hoursPerDay()).toInt()
		)

		areDimensionsInvalid = false
		scrollToDay?.let {
			listener.goToDate(it)
		}

		areDimensionsInvalid = false
		if (scrollToHour >= 0) {
			listener.goToHour(scrollToHour)
		}

		scrollToDay = null
		scrollToHour = -1
		areDimensionsInvalid = false
	}

	internal fun invalidate() {
		areDimensionsInvalid = false
	}

	internal interface UpdateListener {
		fun goToDate(date: Calendar)
		fun goToHour(hour: Int)
	}
}
