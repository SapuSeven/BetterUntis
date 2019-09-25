package com.alamkanak.weekview.drawers

import android.graphics.Canvas
import com.alamkanak.weekview.DateUtils.isSameDay
import com.alamkanak.weekview.DrawingContext
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.config.WeekViewConfig
import org.joda.time.DateTime
import java.lang.Math.max
import java.util.*
import java.util.Calendar.HOUR_OF_DAY
import java.util.Calendar.MINUTE

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

		if (config.showDistinctPastFutureColor) {
			val pastPaint = config.drawConfig.pastBackgroundPaint
			val futurePaint = config.drawConfig.futureBackgroundPaint

			val startY = config.drawConfig.headerHeight + config.drawConfig.currentOrigin.y
			val endX = startPixel + config.drawConfig.widthPerDay

			when {
				isToday -> {
					val now = Calendar.getInstance()
					val minutesUntilNow = now.get(HOUR_OF_DAY) * 60 + now.get(MINUTE) - config.startTime
					canvas.drawRect(startX, startY, endX, startY + minutesUntilNow / 60.0f * config.hourHeight, pastPaint)
					canvas.drawRect(startX, startY + minutesUntilNow, endX, height.toFloat(), futurePaint)
				}
				day < today -> canvas.drawRect(startX, startY, endX, height.toFloat(), pastPaint)
				else -> canvas.drawRect(startX, startY, endX, height.toFloat(), futurePaint)
			}
		} else {
			val todayPaint = config.drawConfig.getTodayBackgroundPaint(isToday)
			val right = startPixel + config.drawConfig.widthPerDay
			canvas.drawRect(startX, config.drawConfig.headerHeight, right, height.toFloat(), todayPaint)
		}
	}
}
