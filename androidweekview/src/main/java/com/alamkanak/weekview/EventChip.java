package com.alamkanak.weekview;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.MotionEvent;

/**
 * A class to hold reference to the events and their visual representation. An EventRect is
 * actually the rectangle that is drawn on the calendar for a given event. There may be more
 * than one rectangle for a single event (an event that expands more than one day). In that
 * case two instances of the EventRect will be used for a single event. The given event will be
 * stored in "originalEvent". But the event that corresponds to rectangle the rectangle
 * instance will be stored in "event".
 */
class EventChip<T> {

	WeekViewEvent<T> event;
	WeekViewEvent<T> originalEvent;

	SplitRect rect;
	float left;
	float width;
	float top;
	float bottom;

	/**
	 * Create a new instance of event rect. An EventRect is actually the rectangle that is drawn
	 * on the calendar for a given event. There may be more than one rectangle for a single
	 * event (an event that expands more than one day). In that case two instances of the
	 * EventRect will be used for a single event. The given event will be stored in
	 * "originalEvent". But the event that corresponds to rectangle the rectangle instance will
	 * be stored in "event".
	 *
	 * @param event         Represents the event which this instance of rectangle represents.
	 * @param originalEvent The original event that was passed by the user.
	 * @param rect          The rectangle.
	 */
	EventChip(WeekViewEvent<T> event, WeekViewEvent<T> originalEvent, SplitRect rect) {
		this.event = event;
		this.rect = rect;
		this.originalEvent = originalEvent;
	}

	void draw(WeekViewConfig config, Canvas canvas) {
		final float cornerRadius = config.eventCornerRadius;
		final Paint backgroundPaint = getBackgroundPaint();
		final Paint pastBackgroundPaint = getPastBackgroundPaint();

		// TODO: On overlapping events, the right most one ends up being a bit bigger than the rest - fix that
		rect.drawTo(canvas, cornerRadius, cornerRadius, pastBackgroundPaint, backgroundPaint);

		drawTitle(config, canvas);
	}

	private Paint getBackgroundPaint() {
		final Paint paint = new Paint();
		paint.setColor(event.getColorOrDefault());
		return paint;
	}

	private Paint getPastBackgroundPaint() {
		final Paint paint = new Paint();
		paint.setColor(event.getPastColorOrDefault());
		return paint;
	}

	private void drawTitle(WeekViewConfig config, Canvas canvas) {
		final boolean negativeWidth = (rect.right - rect.left - config.eventPadding * 2) < 0;
		final boolean negativeHeight = (rect.bottom - rect.top - config.eventPadding * 2) < 0;

		if (negativeWidth || negativeHeight) {
			return;
		}

		// Prepare the name of the event.
		final SpannableStringBuilder topBuilder = new SpannableStringBuilder();
		final SpannableStringBuilder titleBuilder = new SpannableStringBuilder();
		final SpannableStringBuilder bottomBuilder = new SpannableStringBuilder();

		if (event.getTop() != null)
			topBuilder.append(event.getTop());

		if (event.getTitle() != null)
			titleBuilder.append(event.getTitle());

		if (event.getBottom() != null)
			bottomBuilder.append(event.getBottom());

		final int availableHeight = (int) (rect.bottom - rect.top - config.eventPadding * 2);
		final int availableWidth = (int) (rect.right - rect.left - config.eventPadding * 2);

		// Get text dimensions.
		final TextPaint topPaint = config.drawingConfig.eventTopPaint;
		final TextPaint titlePaint = config.drawingConfig.eventTextPaint;
		final TextPaint bottomPaint = config.drawingConfig.eventBottomPaint;

		CharSequence eventTop = TextUtils.ellipsize(topBuilder, topPaint, availableWidth, TextUtils.TruncateAt.END);
		CharSequence eventTitle = TextUtils.ellipsize(titleBuilder, titlePaint, availableWidth, TextUtils.TruncateAt.END);
		CharSequence eventBottom = TextUtils.ellipsize(bottomBuilder, bottomPaint, availableWidth, TextUtils.TruncateAt.END);

		canvas.save();
		canvas.translate(rect.left + config.eventPadding, rect.top + config.eventPadding);

		if (config.eventSecondaryTextCentered) {
			canvas.drawText(eventTop.toString(), availableWidth / 2.0F, -topPaint.ascent(), topPaint);
			canvas.drawText(eventBottom.toString(), availableWidth / 2.0F, availableHeight, bottomPaint);
		} else {
			canvas.drawText(eventTop.toString(), 0, -topPaint.ascent(), topPaint);
			canvas.drawText(eventBottom.toString(), availableWidth, availableHeight, bottomPaint);
		}

		canvas.drawText(eventTitle.toString(), availableWidth / 2.0F, availableHeight / 2.0F - (titlePaint.descent() + titlePaint.ascent()) / 2, titlePaint);
		canvas.restore();
	}

	boolean isHit(MotionEvent e) {
		return rect != null && e.getX() > rect.left && e.getX() < rect.right && e.getY() > rect.top && e.getY() < rect.bottom;
	}

}
