package com.alamkanak.weekview;

import android.graphics.Canvas;

class TimeColumnDrawer {

	private WeekViewConfig config;
	private WeekViewDrawingConfig drawingConfig;

	TimeColumnDrawer(WeekViewConfig config) {
		this.config = config;
		this.drawingConfig = config.drawingConfig;
	}

	void drawTimeColumn(Canvas canvas) {
		float top = drawingConfig.headerHeight
				+ config.headerRowPadding * 2
				+ config.headerRowBottomLineWidth;
		final int bottom = WeekView.getViewHeight();

		final float bottomTimeOffset = 8f;

		// Draw the background color for the header column.
		canvas.drawRect(0, top, drawingConfig.timeColumnWidth, bottom, drawingConfig.headerColumnBackgroundPaint);

		canvas.restore();
		canvas.save();

		canvas.clipRect(0, top, drawingConfig.timeColumnWidth, bottom);

		// The original header height
		final float headerHeight = top;

		for (int i = 0; i < config.hourLines.length; i++) {
			final float headerBottomMargin = drawingConfig.headerMarginBottom + config.headerRowBottomLineWidth;
			final float hourTop = config.hourHeight * (config.hourLines[i] - config.startTime) / 60.0f;
			top = headerHeight + drawingConfig.currentOrigin.y + hourTop + headerBottomMargin;

			final float lastHourTop = i > 0 ? config.hourHeight * (config.hourLines[i - 1] - config.startTime) / 60.0f : 0;

			// Draw the text if its y position is not outside of the visible area. The pivot point
			// of the text is the point at the bottom-right corner.
			final String time = drawingConfig.dateTimeInterpreter.interpretTime(config.hourLines[i]);
			if (time == null)
				throw new IllegalStateException("A DateTimeInterpreter must not return null time");

			if (top - (hourTop - lastHourTop) < bottom) {
				final float x = drawingConfig.timeTextWidth + config.timeColumnPadding;

				if (i % 2 == 0)
					canvas.drawText(time, config.timeColumnPadding, top + drawingConfig.timeTextHeight, drawingConfig.timeTextTopPaint);
				else
					canvas.drawText(time, x, top - bottomTimeOffset, drawingConfig.timeTextBottomPaint);


				if (i % 2 == 1) {
					canvas.drawText(String.valueOf(i / 2 + 1), x / 2, top - (hourTop - lastHourTop) / 2 + (bottomTimeOffset * 1.5f), drawingConfig.timeCaptionPaint);
				}
			}
		}

		// Draw the vertical time column separator
		if (config.showTimeColumnSeparator) {
			final float lineX = drawingConfig.timeColumnWidth - config.timeColumnSeparatorStrokeWidth;
			canvas.drawLine(lineX, headerHeight, lineX, bottom, drawingConfig.timeColumnSeparatorPaint);
		}

		canvas.restore();
	}

}
