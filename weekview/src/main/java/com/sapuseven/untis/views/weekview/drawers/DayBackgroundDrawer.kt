package com.sapuseven.untis.views.weekview.drawers

import android.graphics.Canvas
import com.sapuseven.untis.views.weekview.DateUtils.isSameDay
import com.sapuseven.untis.views.weekview.DrawingContext
import com.sapuseven.untis.views.weekview.WeekView
import com.sapuseven.untis.views.weekview.config.WeekViewConfig
import org.joda.time.DateTime
import kotlin.math.max

class DayBackgroundDrawer(private val config: WeekViewConfig) {
	internal fun draw(drawingContext: DrawingContext, canvas: Canvas) {
		var startPixel = drawingContext.startPixel

		for (day in drawingContext.dayRange) {
			val startX = max(startPixel, config.drawConfig.timeColumnWidth)
			drawDayBackground(day, startX, startPixel, canvas)

			if (config.isSingleDay) {
				// Add a margin at the start if we're in day view. Otherwise, screen space is too
				// precious and we refrain from doing so.
				startPixel += config.eventMarginHorizontal
			}

			// In the next iteration, start from the next day.
			startPixel += config.totalDayWidth
		}
	}

	private fun drawDayBackground(day: DateTime, startX: Float, startPixel: Float, canvas: Canvas) {
		val today = DateTime.now()
		val isToday = isSameDay(day, today)

		if (config.drawConfig.widthPerDay + startPixel - startX <= 0) return

		val height = WeekView.viewHeight

		val pastPaint = config.drawConfig.pastBackgroundPaint
		val futurePaint = config.drawConfig.futureBackgroundPaint

		val startY = config.drawConfig.headerHeight + config.drawConfig.currentOrigin.y
		val endX = startPixel + config.drawConfig.widthPerDay

		when {
			isToday -> {
				val now = DateTime.now()
				val minutesUntilNow = now.hourOfDay * 60 + now.minuteOfHour - config.startTime
				canvas.drawRect(startX, startY, endX, startY + minutesUntilNow / 60.0f * config.hourHeight, pastPaint)
				canvas.drawRect(startX, startY + minutesUntilNow / 60.0f * config.hourHeight, endX, height.toFloat(), futurePaint)
			}
			day < today -> canvas.drawRect(startX, startY, endX, height.toFloat(), pastPaint)
			else -> canvas.drawRect(startX, startY, endX, height.toFloat(), futurePaint)
		}
	}
}
