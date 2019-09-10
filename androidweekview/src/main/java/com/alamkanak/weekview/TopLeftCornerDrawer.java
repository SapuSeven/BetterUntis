package com.alamkanak.weekview;

import android.graphics.Canvas;
import android.graphics.Rect;

public class TopLeftCornerDrawer {
	private WeekViewConfig config;
	private WeekViewDrawingConfig drawConfig;

	TopLeftCornerDrawer(WeekViewConfig config) {
		this.config = config;
		this.drawConfig = config.drawingConfig;
	}

	void draw(Canvas canvas) {
		final int headerHeight = (int) (drawConfig.headerHeight
				+ config.headerRowPadding * 2
				+ drawConfig.headerMarginBottom);
		final int timeColumnWidth = (int) drawConfig.timeColumnWidth;

		final Rect sourceRect = new Rect(0, 0, config.topLeftCornerDrawable.getIntrinsicWidth(), config.topLeftCornerDrawable.getIntrinsicHeight());
		final Rect destRect = new Rect(0, 0, timeColumnWidth, headerHeight);
		Rect bounds;

		if (destRect.height() >= destRect.width()) {
			int diff = (destRect.height() - sourceRect.height()) / 4;
			bounds = new Rect(config.topLeftCornerPadding, diff + config.topLeftCornerPadding,
					destRect.right - config.topLeftCornerPadding, destRect.bottom - diff - config.topLeftCornerPadding);
		} else {
			int diff = (destRect.width() - sourceRect.width()) / 4;
			bounds = new Rect(diff + config.topLeftCornerPadding, config.topLeftCornerPadding,
					destRect.right - diff + config.topLeftCornerPadding, destRect.bottom - config.topLeftCornerPadding);
		}

		config.topLeftCornerDrawable.setBounds(bounds);
		config.topLeftCornerDrawable.draw(canvas);
	}
}
