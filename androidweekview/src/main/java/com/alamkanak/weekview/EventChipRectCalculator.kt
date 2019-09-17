package com.alamkanak.weekview

import android.graphics.RectF

import com.alamkanak.weekview.config.WeekViewConfig

class EventChipRectCalculator internal constructor(private val config: WeekViewConfig) {
	fun calculateSingleEvent(eventChip: EventChip<*>, startFromPixel: Float): RectF {
		val eventMargin = config.eventMarginVertical.toFloat()

		val verticalOrigin = config.drawConfig.currentOrigin.y
		val widthPerDay = config.drawConfig.widthPerDay - config.columnGap

		// Calculate top
		val verticalDistanceFromTop = config.hourHeight.toFloat() * config.hoursPerDay() * eventChip.top / config.minutesPerDay()
		val top = verticalDistanceFromTop + verticalOrigin + config.drawConfig.headerHeight + eventMargin

		// Calculate bottom
		val verticalDistanceFromBottom = config.hourHeight.toFloat() * config.hoursPerDay() * eventChip.bottom / config.minutesPerDay()
		val bottom = verticalDistanceFromBottom + verticalOrigin + config.drawConfig.headerHeight - eventMargin

		// Calculate left
		var left = startFromPixel + eventChip.left * widthPerDay
		if (eventChip.left > 0)
		// all except first element
			left += config.overlappingEventGap / 2.0f
		left += config.columnGap / 2.0f

		// Calculate right
		var right = left + eventChip.width * widthPerDay
		if (right < startFromPixel + widthPerDay)
		// all except last element
			right -= config.overlappingEventGap / 2.0f
		if (eventChip.left > 0)
		// all except first element
			right -= config.overlappingEventGap / 2.0f

		// this calculation is fast and simple, but suboptimal, as the first and last element
		// will be bigger (by overlappingEventGap/2). But most of the time there are a maximum
		// of two simultaneous lessons, so this is acceptable.

		return RectF(left, top, right, bottom)
	}
}
