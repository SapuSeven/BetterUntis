package com.sapuseven.untis.views.weekview.drawers

import android.graphics.Canvas
import com.sapuseven.untis.views.weekview.DrawingContext

interface BaseDrawer {
	fun draw(drawingContext: DrawingContext, canvas: Canvas)
}