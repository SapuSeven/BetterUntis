package com.alamkanak.weekview.drawers

import android.graphics.Canvas
import com.alamkanak.weekview.DateUtils.isSameDay
import com.alamkanak.weekview.DateUtils.today
import com.alamkanak.weekview.DrawingContext
import com.alamkanak.weekview.config.WeekViewConfig
import com.alamkanak.weekview.config.WeekViewDrawConfig
import java.util.*

class DayLabelDrawer(private val config: WeekViewConfig) : BaseDrawer {
	private val drawConfig: WeekViewDrawConfig = config.drawConfig

	override fun draw(drawingContext: DrawingContext, canvas: Canvas) {
		var startPixel = drawingContext.startPixel

		canvas.save()
		canvas.clipRect(drawConfig.timeColumnWidth.toInt(), 0, canvas.width, canvas.height)

		for (day in drawingContext.dayRange) {
			drawLabel(day, startPixel, canvas)

			if (config.isSingleDay)
				startPixel += config.eventMarginHorizontal

			startPixel += config.totalDayWidth
		}

		canvas.restore()
	}

	private fun drawLabel(day: Calendar, startPixel: Float, canvas: Canvas) {
		val today = today()
		val isSameDay = isSameDay(day, today)

		// Draw the day labels.
		val dayLabel = drawConfig.dateTimeInterpreter.interpretDate(day)
		val secondaryDayLabel = drawConfig.dateTimeInterpreter.interpretSecondaryDate(day)

		val x = startPixel + drawConfig.widthPerDay / 2
		var y = drawConfig.headerTextHeight + config.headerRowPadding

		val textPaint = if (isSameDay) drawConfig.todayHeaderTextPaint else drawConfig.headerTextPaint
		canvas.drawText(dayLabel, x, y, textPaint)

		y += config.headerRowTextSpacing + drawConfig.headerSecondaryTextHeight

		val secondaryTextPaint = if (isSameDay) drawConfig.todayHeaderSecondaryTextPaint else drawConfig.headerSecondaryTextPaint
		canvas.drawText(secondaryDayLabel, x, y, secondaryTextPaint)
	}
}
