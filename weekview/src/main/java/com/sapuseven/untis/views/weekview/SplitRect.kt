package com.sapuseven.untis.views.weekview

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

class SplitRect(baseRect: RectF, private val division: Float) : RectF(baseRect) {
	fun drawTo(canvas: Canvas, rx: Float, ry: Float, topPaint: Paint, bottomPaint: Paint) {
		with(canvas) {
			when (division) {
				0f -> drawRoundRect(this@SplitRect, rx, ry, bottomPaint)
				1f -> drawRoundRect(this@SplitRect, rx, ry, topPaint)
				else -> {
					drawRoundRect(this@SplitRect, rx, ry, topPaint)
					save()
					clipRect(left, top + (bottom - top) * division, right, bottom)
					drawRoundRect(this@SplitRect, rx, ry, bottomPaint)
					restore()
				}
			}
		}
	}
}
