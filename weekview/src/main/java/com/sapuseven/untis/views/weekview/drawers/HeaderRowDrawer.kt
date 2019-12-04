package com.sapuseven.untis.views.weekview.drawers

import android.graphics.Canvas
import android.graphics.Paint
import com.sapuseven.untis.views.weekview.DrawingContext
import com.sapuseven.untis.views.weekview.WeekView
import com.sapuseven.untis.views.weekview.config.WeekViewConfig

class HeaderRowDrawer(private val config: WeekViewConfig) : BaseDrawer {
	override fun draw(drawingContext: DrawingContext, canvas: Canvas) {
		val width = WeekView.viewWidth

		canvas.restore()
		canvas.save()

		// Hide everything in the top left corner
		canvas.clipRect(0f, 0f, config.drawConfig.timeColumnWidth, config.drawConfig.headerHeight)
		canvas.drawRect(0f, 0f, config.drawConfig.timeColumnWidth, config.drawConfig.headerHeight, config.drawConfig.headerBackgroundPaint)

		canvas.restore()
		canvas.save()

		// Clip to paint header row only.
		canvas.clipRect(config.drawConfig.timeColumnWidth, 0f, width.toFloat(), config.drawConfig.headerHeight)
		canvas.drawRect(0f, 0f, width.toFloat(), config.drawConfig.headerHeight, config.drawConfig.headerBackgroundPaint)

		canvas.restore()
		canvas.save()

		if (config.showHeaderRowBottomLine)
			drawHeaderBottomLine(config.drawConfig.headerHeight, width, canvas)
	}

	private fun drawHeaderBottomLine(headerHeight: Float, width: Int, canvas: Canvas) {
		val headerRowBottomLineWidth = config.headerRowBottomLineWidth
		val topMargin = headerHeight - headerRowBottomLineWidth

		val paint = Paint()
		paint.strokeWidth = headerRowBottomLineWidth.toFloat()
		paint.color = config.headerRowBottomLineColor

		canvas.drawLine(0f, topMargin, width.toFloat(), topMargin, paint)
	}
}
