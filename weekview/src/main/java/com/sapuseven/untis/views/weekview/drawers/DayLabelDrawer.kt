package com.sapuseven.untis.views.weekview.drawers

import android.graphics.Canvas
import com.sapuseven.untis.views.weekview.DateUtils.isSameDay
import com.sapuseven.untis.views.weekview.DrawingContext
import com.sapuseven.untis.views.weekview.config.WeekViewConfig
import com.sapuseven.untis.views.weekview.config.WeekViewDrawConfig
import org.joda.time.DateTime

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

	private fun drawLabel(day: DateTime, startPixel: Float, canvas: Canvas) {
		val isSameDay = isSameDay(day, DateTime.now())

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
