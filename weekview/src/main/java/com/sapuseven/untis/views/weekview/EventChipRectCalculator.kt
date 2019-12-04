package com.sapuseven.untis.views.weekview

import android.graphics.RectF

import com.sapuseven.untis.views.weekview.config.WeekViewConfig

class EventChipRectCalculator internal constructor(private val config: WeekViewConfig) {
	/**
	 * Calculates the exact position and dimensions of an EventChip in the WeekView.
	 *
	 * @param eventChip An EventChip which contains position and dimensions as calculated by [EventChipProvider]
	 * @param startFromPixel Absolute x position of the first pixel of the day column to draw in
	 *
	 * @return A [RectF] at the absolute location and size of the EventChip's final position in the WeekView
	 */
	fun calculateEventRect(eventChip: EventChip<*>, startFromPixel: Float): RectF {
		val eventMargin = config.eventMarginVertical.toFloat() / 2

		val verticalOrigin = config.drawConfig.currentOrigin.y
		val widthPerDay = config.drawConfig.widthPerDay - config.columnGap

		// Calculate top
		val verticalDistanceFromTop = config.hourHeight.toFloat() * config.hoursPerDay() * eventChip.top / config.minutesPerDay()
		val top = verticalDistanceFromTop + verticalOrigin + config.drawConfig.headerHeight + eventMargin

		// Calculate bottom
		val verticalDistanceFromBottom = config.hourHeight.toFloat() * config.hoursPerDay() * eventChip.bottom / config.minutesPerDay()
		val bottom = verticalDistanceFromBottom + verticalOrigin + config.drawConfig.headerHeight - eventMargin

		val columns = 1 / eventChip.width // Determine the number of columns

		// Calculate left
		var left = startFromPixel
		left += config.columnGap / 2.0f // Start at offset to create column gap
		left += eventChip.left * widthPerDay // Add unnormalized x position
		left += eventChip.left * config.overlappingEventGap // Adjustment if overlapping (evaluates to 0 for single elements)

		// Calculate right
		var right = left // Start at calculated x position
		right += eventChip.width * widthPerDay // Add unnormalized width
		right -= config.overlappingEventGap * (columns - 1) / columns // Adjustment if overlapping (evaluates to 0 for single elements)

		return RectF(left, top, right, bottom)
	}
}
