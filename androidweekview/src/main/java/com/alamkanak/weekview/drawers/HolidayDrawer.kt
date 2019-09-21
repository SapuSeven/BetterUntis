package com.alamkanak.weekview.drawers

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import com.alamkanak.weekview.DrawingContext
import com.alamkanak.weekview.HolidayChip
import com.alamkanak.weekview.config.WeekViewConfig

class HolidayDrawer(private val config: WeekViewConfig) : BaseDrawer {
	var holidayChips = emptyList<HolidayChip>()

	override fun draw(drawingContext: DrawingContext, canvas: Canvas) {
		var startPixel = drawingContext.startPixel

		val text = mutableListOf<String>()
		for (day in drawingContext.dayRange) {
			holidayChips.forEach {
				if (it.isOnDay(day))
					text.add(it.text)
			}
			drawHoliday(text.joinToString(" / "), startPixel, canvas)
			text.clear()

			if (config.isSingleDay)
				startPixel += config.eventMarginHorizontal

			startPixel += config.totalDayWidth
		}
	}

	private fun drawHoliday(text: String, startFromPixel: Float, canvas: Canvas) {
		val paint = Paint(Paint.ANTI_ALIAS_FLAG)
		paint.strokeWidth = 5f
		paint.color = config.defaultEventColor

		val bounds = Rect()
		config.drawConfig.eventTextPaint.getTextBounds(text, 0, text.length, bounds)

		val holidayPadding = 50f

		canvas.save()
		canvas.translate(startFromPixel, config.drawConfig.headerHeight)
		canvas.rotate(90f)

		canvas.drawText(text, holidayPadding, (config.drawConfig.widthPerDay - bounds.height()) / -2f, config.drawConfig.holidayTextPaint)
		canvas.restore()
	}
}
