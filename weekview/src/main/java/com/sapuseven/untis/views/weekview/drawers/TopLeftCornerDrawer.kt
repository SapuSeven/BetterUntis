package com.sapuseven.untis.views.weekview.drawers

import android.graphics.Canvas
import android.graphics.Rect
import com.sapuseven.untis.views.weekview.DrawingContext
import com.sapuseven.untis.views.weekview.config.WeekViewConfig
import com.sapuseven.untis.views.weekview.config.WeekViewDrawConfig
import kotlin.math.min

class TopLeftCornerDrawer(private val config: WeekViewConfig) : BaseDrawer {
	private val drawConfig: WeekViewDrawConfig = config.drawConfig

	override fun draw(drawingContext: DrawingContext, canvas: Canvas) {
		config.topLeftCornerDrawable?.let { drawable ->
			val timeColumnWidth = drawConfig.timeColumnWidth.toInt()

			val destRect = Rect(0, 0, timeColumnWidth, drawConfig.headerHeight.toInt())

			val imageSize = destRect.shortestSide()
			val hDiff = destRect.width() - imageSize
			val vDiff = destRect.height() - imageSize

			drawable.bounds = Rect(
					hDiff / 2 + config.topLeftCornerPadding,
					vDiff / 2 + config.topLeftCornerPadding,
					hDiff / 2 + imageSize - config.topLeftCornerPadding,
					vDiff / 2 + imageSize - config.topLeftCornerPadding
			)

			drawable.draw(canvas)
		}
	}

	private fun Rect.shortestSide(): Int {
		return min(width(), height())
	}
}