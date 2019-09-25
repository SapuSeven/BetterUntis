package com.alamkanak.weekview.drawers

import android.graphics.Canvas
import com.alamkanak.weekview.DateUtils.isSameDay
import com.alamkanak.weekview.DrawingContext
import com.alamkanak.weekview.config.WeekViewConfig
import com.alamkanak.weekview.config.WeekViewDrawConfig
import org.joda.time.DateTime
import java.lang.Math.max
import java.util.*
import java.util.Calendar.HOUR_OF_DAY
import java.util.Calendar.MINUTE

class NowLineDrawer(private val config: WeekViewConfig) : BaseDrawer {
	private val drawConfig: WeekViewDrawConfig = config.drawConfig

	override fun draw(drawingContext: DrawingContext, canvas: Canvas) {
		if (!config.showNowLine) return

		var startPixel = drawingContext.startPixel

		for (day in drawingContext.dayRange) {
			val isSameDay = isSameDay(day, DateTime.now())
			val startX = max(startPixel, drawConfig.timeColumnWidth)

			if (config.isSingleDay) {
				// Add a margin at the start if we're in day view. Otherwise, screen space is too
				// precious and we refrain from doing so.
				startPixel += config.eventMarginHorizontal
			}

			// Draw the line at the current time.
			if (isSameDay)
				drawLine(startX, startPixel, canvas)

			// In the next iteration, start from the next day.
			startPixel += config.totalDayWidth
		}
	}

	private fun drawLine(startX: Float, startPixel: Float, canvas: Canvas) {
		val startY = drawConfig.headerHeight + drawConfig.currentOrigin.y
		val now = Calendar.getInstance()

		// Draw line
		val minutesUntilNow = now.get(HOUR_OF_DAY) * 60 + now.get(MINUTE) - config.startTime
		val lineStartY = startY + minutesUntilNow / 60.0f * config.hourHeight
		canvas.drawLine(startX, lineStartY, startPixel + drawConfig.widthPerDay, lineStartY, drawConfig.nowLinePaint)
	}
}
