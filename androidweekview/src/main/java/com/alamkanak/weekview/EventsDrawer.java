package com.alamkanak.weekview;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import java.util.Calendar;
import java.util.List;

class EventsDrawer<T> {

	private WeekViewConfig config;
	private WeekViewDrawingConfig drawingConfig;
	private EventChipRectCalculator rectCalculator;

	EventsDrawer(WeekViewConfig config) {
		this.config = config;
		this.drawingConfig = config.drawingConfig;
		this.rectCalculator = new EventChipRectCalculator(config);
	}

	void drawSingleEvents(List<EventChip<T>> eventChips,
	                      DrawingContext drawingContext, Canvas canvas) {
		float startPixel = drawingContext.startPixel;
		long now = Calendar.getInstance().getTimeInMillis();

		// Draw single events
		for (Calendar day : drawingContext.dayRange) {
			if (config.isSingleDay()) {
				// Add a margin at the start if we're in day view. Otherwise, screen space is too
				// precious and we refrain from doing so.
				startPixel = startPixel + config.eventMarginHorizontal;
			}

			drawEventsForDate(eventChips, day, now, startPixel, canvas);

			// In the next iteration, start from the next day.
			startPixel += config.getTotalDayWidth();
		}
	}

	private void drawEventsForDate(List<EventChip<T>> eventChips, Calendar date, long nowMillis,
	                               float startFromPixel, Canvas canvas) {
		if (eventChips == null) {
			return;
		}

		for (int i = 0; i < eventChips.size(); i++) {
			final EventChip eventChip = eventChips.get(i);
			final WeekViewEvent event = eventChip.event;
			if (!event.isSameDay(date)) {
				continue;
			}

			final SplitRect chipRect = new SplitRect(
					rectCalculator.calculateSingleEvent(eventChip, startFromPixel),
					calculateDivision(event, nowMillis)
			);
			if (isValidSingleEventRect(chipRect)) {
				eventChip.rect = chipRect;
				eventChip.draw(config, canvas);
			} else {
				eventChip.rect = null;
			}
		}
	}

	private float calculateDivision(WeekViewEvent event, long nowMillis) {
		long eventStartMillis = event.getStartTime().getTimeInMillis();
		long eventEndMillis = event.getEndTime().getTimeInMillis();

		if (nowMillis <= eventStartMillis)
			return 0;
		else if (nowMillis >= eventEndMillis)
			return 1;
		else
			return (float) (nowMillis - eventStartMillis) / (float) (eventEndMillis - eventStartMillis);
	}

	/**
	 * Draw all the all-day events of a particular day.
	 *
	 * @param eventChips     The list of {@link EventChip}s to draw
	 * @param drawingContext The {@link DrawingContext} to use for drawing
	 * @param canvas         The canvas to draw upon.
	 */
	void drawAllDayEvents(List<EventChip<T>> eventChips,
	                      DrawingContext drawingContext, Canvas canvas) {
		if (eventChips == null) {
			return;
		}

		float startPixel = drawingContext.startPixel;

		for (Calendar day : drawingContext.dayRange) {
			if (config.isSingleDay()) {
				startPixel = startPixel + config.eventMarginHorizontal;
			}

			for (EventChip eventChip : eventChips) {
				final WeekViewEvent event = eventChip.event;
				if (!event.isSameDay(day)) {
					continue;
				}

				drawAllDayEvent(eventChip, startPixel, canvas);
			}

			startPixel += config.getTotalDayWidth();
		}

		// Hide events when they are in the top left corner
		final Paint headerBackground = drawingConfig.headerBackgroundPaint;

		float headerRowBottomLine = 0;
		if (config.showHeaderRowBottomLine) {
			headerRowBottomLine = config.headerRowBottomLineWidth;
		}

		final float height = drawingConfig.headerHeight + config.headerRowPadding * 2 - headerRowBottomLine;
		final float width = drawingConfig.timeTextWidth + config.timeColumnPadding * 2;

		canvas.clipRect(0, 0, width, height);
		canvas.drawRect(0, 0, width, height, headerBackground);

		canvas.restore();
		canvas.save();
	}

	private void drawAllDayEvent(EventChip eventChip, float startFromPixel, Canvas canvas) {
		final SplitRect chipRect = new SplitRect(rectCalculator.calculateAllDayEvent(eventChip, startFromPixel), 0);
		if (isValidAllDayEventRect(chipRect)) {
			eventChip.rect = chipRect;

			eventChip.draw(config, canvas);
		} else {
			eventChip.rect = null;
		}
	}

	private boolean isValidSingleEventRect(RectF rect) {
		final float totalHeaderHeight = drawingConfig.headerHeight
				+ config.headerRowPadding * 2
				+ drawingConfig.headerMarginBottom;

		return rect.left < rect.right
				&& rect.left < WeekView.getViewWidth()
				&& rect.top < WeekView.getViewHeight()
				&& rect.right > drawingConfig.timeColumnWidth
				&& rect.bottom > totalHeaderHeight;
	}

	private boolean isValidAllDayEventRect(RectF rect) {
		return rect.left < rect.right
				&& rect.left < WeekView.getViewWidth()
				&& rect.top < WeekView.getViewHeight()
				&& rect.right > drawingConfig.timeColumnWidth
				&& rect.bottom > 0;
	}
}
