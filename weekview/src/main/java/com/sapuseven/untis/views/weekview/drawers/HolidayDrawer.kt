package com.sapuseven.untis.views.weekview.drawers

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import com.sapuseven.untis.views.weekview.DrawingContext
import com.sapuseven.untis.views.weekview.HolidayChip
import com.sapuseven.untis.views.weekview.config.WeekViewConfig

class HolidayDrawer(private val config: WeekViewConfig) : BaseDrawer {
	var holidayChips: List<HolidayChip> = emptyList()

	override fun draw(drawingContext: DrawingContext, canvas: Canvas) {
		val text = mutableListOf<String>()
		drawingContext.freeDays.forEach { (first, second) ->
			holidayChips.forEach {
				if (it.isOnDay(first))
					text.add(it.text)
			}
			drawHoliday(text.joinToString(" / "), second, canvas)
			text.clear()
		}
	}

	private fun drawHoliday(text: String, startFromPixel: Float, canvas: Canvas) {
		if (text.isBlank()) return

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
