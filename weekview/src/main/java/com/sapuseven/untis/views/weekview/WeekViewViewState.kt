package com.sapuseven.untis.views.weekview

import com.sapuseven.untis.views.weekview.config.WeekViewConfig
import org.joda.time.DateTime
import kotlin.math.max

class WeekViewViewState {
	internal var scrollToDay: DateTime? = null
	internal var scrollToHour = -1

	internal var isFirstDraw = true
	internal var areDimensionsInvalid = true

	var firstVisibleDay: DateTime? = null

	internal var shouldRefreshEvents: Boolean = false

	internal fun update(config: WeekViewConfig, listener: UpdateListener) {
		if (!areDimensionsInvalid) return

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
		areDimensionsInvalid = true
	}

	internal interface UpdateListener {
		fun goToDate(date: DateTime)
		fun goToHour(hour: Int)
	}
}
