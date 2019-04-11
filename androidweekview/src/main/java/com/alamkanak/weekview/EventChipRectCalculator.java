package com.alamkanak.weekview;

import android.graphics.RectF;

class EventChipRectCalculator {
	private WeekViewConfig config;

	EventChipRectCalculator(WeekViewConfig config) {
		this.config = config;
	}

	RectF calculateSingleEvent(EventChip eventChip, float startFromPixel) {
		final float eventMargin = config.eventMarginVertical;

		final float verticalOrigin = config.drawingConfig.currentOrigin.y;
		final float widthPerDay = config.drawingConfig.widthPerDay - config.columnGap;

		final float headerHeight = config.drawingConfig.headerHeight;
		final float headerPadding = config.headerRowPadding * 2;
		final float headerBottomMargin = config.drawingConfig.headerMarginBottom;
		final float totalHeaderHeight = headerHeight + headerPadding + headerBottomMargin;

		// Calculate top
		final float verticalDistanceFromTop = config.hourHeight * config.hoursPerDay() * eventChip.top / config.minutesPerDay();
		final float top = verticalDistanceFromTop + verticalOrigin + totalHeaderHeight + eventMargin;

		// Calculate bottom
		final float verticalDistanceFromBottom = config.hourHeight * config.hoursPerDay() * eventChip.bottom / config.minutesPerDay();
		final float bottom = verticalDistanceFromBottom + verticalOrigin + totalHeaderHeight - eventMargin;

		// Calculate left
		float left = startFromPixel + eventChip.left * widthPerDay;
		if (eventChip.left > 0) // all except first element
			left += config.overlappingEventGap / 2.0f;
		left += config.columnGap / 2.0f;

		// Calculate right
		float right = left + eventChip.width * widthPerDay;
		if (right < startFromPixel + widthPerDay) // all except last element
			right -= config.overlappingEventGap / 2.0f;
		if (eventChip.left > 0) // all except first element
			right -= config.overlappingEventGap / 2.0f;

		// this calculation is fast and simple, but suboptimal, as the first and last element
		// will be bigger (by overlappingEventGap/2). But most of the times there are a maximum
		// of two simultaneous lessons, so this is ok.

		return new RectF(left, top, right, bottom);
	}

	RectF calculateAllDayEvent(EventChip eventChip, float startFromPixel) {
		final float headerHeight = config.headerRowPadding * 2 + config.drawingConfig.headerMarginBottom;
		final float widthPerDay = config.drawingConfig.widthPerDay;
		final float halfTextHeight = config.drawingConfig.timeTextHeight / 2;

		// Calculate top
		final float top = headerHeight + halfTextHeight + config.eventMarginVertical;

		// Calculate bottom
		final float bottom = top + eventChip.bottom;

		// Calculate left
		float left = startFromPixel + eventChip.left * widthPerDay;
		if (left < startFromPixel) {
			left += config.overlappingEventGap;
		}

		// Calculate right
		float right = left + eventChip.width * widthPerDay;
		if (right < startFromPixel + widthPerDay) {
			right -= config.overlappingEventGap;
		}

		boolean hasNoOverlaps = (right == startFromPixel + widthPerDay);
		if (config.numberOfVisibleDays == 1 && hasNoOverlaps) {
			right -= config.eventMarginHorizontal * 2;
		}

		return new RectF(left, top, right, bottom);
	}

}
