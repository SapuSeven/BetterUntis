package com.sapuseven.untis.views.weekview.drawers

import android.graphics.Canvas

import com.sapuseven.untis.views.weekview.DrawingContext
import com.sapuseven.untis.views.weekview.WeekView
import com.sapuseven.untis.views.weekview.config.WeekViewConfig
import kotlin.math.max

class BackgroundGridDrawer(private val config: WeekViewConfig) : BaseDrawer {
	override fun draw(drawingContext: DrawingContext, canvas: Canvas) {
		val size = drawingContext.dayRange.size

		var startPixel = drawingContext.startPixel

		val startX = max(startPixel, config.drawConfig.timeColumnWidth)
		drawGrid(calcHourLines(), startX, startPixel, canvas)

		for (i in 0 until size) {

			if (config.isSingleDay) {
				// Add a margin at the start if we're in day view. Otherwise, screen space is too
				// precious and we refrain from doing so.
				startPixel += config.eventMarginHorizontal.toFloat()
			}

			// In the next iteration, start from the next day.
			startPixel += config.totalDayWidth
		}
	}

	private fun calcHourLines(): FloatArray {
		val drawConfig = config.drawConfig
		val height = WeekView.viewHeight
		var lineCount = ((height - drawConfig.headerHeight) / config.hourHeight).toInt() + 1
		lineCount *= (config.visibleDays + 1)
		return FloatArray(lineCount * 4)
	}

	private fun drawGrid(hourLines: FloatArray, startX: Float, startPixel: Float, canvas: Canvas) {
		if (config.showHourSeparator)
			drawHourLines(hourLines, startX, startPixel, canvas)
		if (config.showDaySeparator)
			drawDaySeparators(startPixel, canvas)
	}

	private fun drawDaySeparators(startPixel: Float, canvas: Canvas) {
		val days = config.visibleDays
		val widthPerDay = config.totalDayWidth

		val height = WeekView.viewHeight

		for (i in 0 until days) {
			val start = startPixel + widthPerDay * (i + 1)
			canvas.drawLine(start, config.drawConfig.headerHeight, start, config.drawConfig.headerHeight + height, config.drawConfig.daySeparatorPaint)
		}
	}

	private fun drawHourLines(hourLines: FloatArray, startX: Float, startPixel: Float, canvas: Canvas) {
		val height = WeekView.viewHeight

		var i = 0
		for (hour in 1 until config.hourLines.size) {
			if (config.hourLines[hour] == config.hourLines[hour - 1]) continue

			val heightOfHour = config.hourHeight * (config.hourLines[hour] - config.startTime) / 60.0f
			val top = config.drawConfig.headerHeight + config.drawConfig.currentOrigin.y + heightOfHour

			val widthPerDay = config.totalDayWidth
			val separatorWidth = config.hourSeparatorStrokeWidth

			val isNotHiddenByHeader = top > config.drawConfig.headerHeight - separatorWidth
			val isWithinVisibleRange = top < height
			val isVisibleHorizontally = startPixel + widthPerDay - startX > 0

			if (isNotHiddenByHeader && isWithinVisibleRange && isVisibleHorizontally) {
				hourLines[i * 4] = 0f
				hourLines[i * 4 + 1] = top
				hourLines[i * 4 + 2] = canvas.width.toFloat()
				hourLines[i * 4 + 3] = top
				i++
			}
		}

		canvas.drawLines(hourLines, config.drawConfig.hourSeparatorPaint)
	}
}
