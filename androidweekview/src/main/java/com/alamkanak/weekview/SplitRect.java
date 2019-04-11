package com.alamkanak.weekview;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

class SplitRect extends RectF {
	private float division;

	SplitRect(RectF baseRect, float division) {
		super(baseRect);
		this.division = division;
	}

	void drawTo(Canvas canvas, float rx, float ry, Paint topPaint, Paint bottomPaint) {
		if (division == 0) {
			canvas.drawRoundRect(this, rx, ry, bottomPaint);
		} else if (division == 1) {
			canvas.drawRoundRect(this, rx, ry, topPaint);
		} else {
			canvas.drawRoundRect(this, rx, ry, topPaint);
			canvas.save();
			canvas.clipRect(left, top + (bottom - top) * division, right, bottom);
			canvas.drawRoundRect(this, rx, ry, bottomPaint);
			canvas.restore();
		}
	}
}
