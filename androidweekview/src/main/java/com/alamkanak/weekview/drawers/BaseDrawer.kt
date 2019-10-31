package com.alamkanak.weekview.drawers

import android.graphics.Canvas
import com.alamkanak.weekview.DrawingContext

interface BaseDrawer {
	fun draw(drawingContext: DrawingContext, canvas: Canvas)
}