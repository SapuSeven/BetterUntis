package com.sapuseven.untis.views.weekview.drawers

import android.graphics.Canvas
import com.sapuseven.untis.views.weekview.DrawingContext
import com.sapuseven.untis.views.weekview.WeekView
import com.sapuseven.untis.views.weekview.config.WeekViewConfig
import com.sapuseven.untis.views.weekview.config.WeekViewDrawConfig

class TimeColumnDrawer(private val config: WeekViewConfig) : BaseDrawer {
	private val drawConfig: WeekViewDrawConfig = config.drawConfig

	override fun draw(drawingContext: DrawingContext, canvas: Canvas) {
		var top = drawConfig.headerHeight
		val bottom = WeekView.viewHeight.toFloat()

		// Draw the background color for the time column.
		canvas.drawRect(0f, top, drawConfig.timeColumnWidth, bottom, drawConfig.timeColumnBackgroundPaint)

		canvas.restore()
		canvas.save()

		canvas.clipRect(0f, top, drawConfig.timeColumnWidth, bottom)

		// The original header height
		val headerHeight = top

		for (i in config.hourLines.indices) {
			val headerBottomMargin = drawConfig.headerMarginBottom + config.headerRowBottomLineWidth
			val hourTop = config.hourHeight * (config.hourLines[i] - config.startTime) / 60.0f
			top = headerHeight + drawConfig.currentOrigin.y + hourTop + headerBottomMargin

			val lastHourTop = if (i > 0) config.hourHeight * (config.hourLines[i - 1] - config.startTime) / 60.0f else 0.0f

			// Draw the text if its y position is not outside of the visible area.
			// The pivot point of the text is the point at the bottom-right corner.
			val time = drawConfig.dateTimeInterpreter.interpretTime(config.hourLines[i])

			if (top - (hourTop - lastHourTop) < bottom) {
				val bottomCoordinate = top - config.hourSeparatorStrokeWidth - config.timeColumnPadding / 2
				val topCoordinate = top - (hourTop - lastHourTop) - config.hourSeparatorStrokeWidth - config.timeColumnPadding / 2

				if (drawConfig.timeTextVisibility) {
					if (i % 2 == 0) {
						if (config.hourLines[i + 1] - config.hourLines[i] > 30)
							canvas.drawText(time, config.timeColumnPadding.toFloat(), top + drawConfig.timeTextHeight + config.timeColumnPadding / 2, drawConfig.timeTextTopPaint)
					} else
						if (config.hourLines[i] - config.hourLines[i - 1] > 30)
							canvas.drawText(time, config.timeColumnPadding + drawConfig.timeTextWidth, bottomCoordinate, drawConfig.timeTextBottomPaint)
				}

				if (i % 2 == 1)
					canvas.drawText(config.hourLabels[i / 2], config.timeColumnPadding + drawConfig.timeTextWidth / 2, topCoordinate + (bottomCoordinate - topCoordinate + drawConfig.timeCaptionHeight + config.timeColumnPadding) / 2, drawConfig.timeCaptionPaint)
			}
		}

		// Draw the vertical time column separator
		if (config.showTimeColumnSeparator) {
			val lineX = drawConfig.timeColumnWidth - config.timeColumnSeparatorStrokeWidth
			canvas.drawLine(lineX, drawConfig.headerHeight, lineX, bottom, drawConfig.timeColumnSeparatorPaint)
		}

		canvas.restore()
	}
}
