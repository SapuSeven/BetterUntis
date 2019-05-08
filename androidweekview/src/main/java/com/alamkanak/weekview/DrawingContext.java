package com.alamkanak.weekview;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

class DrawingContext {
	final List<Calendar> dayRange;
	final float startPixel;

	private DrawingContext(List<Calendar> dayRange, float startPixel) {
		this.dayRange = dayRange;
		this.startPixel = startPixel;
	}

	static DrawingContext create(WeekViewConfig config, WeekViewViewState viewState) {
		final WeekViewDrawingConfig drawConfig = config.drawingConfig;
		final float totalDayWidth = config.getTotalDayWidth();
		final int leftDaysWithGaps = (int) (Math.ceil(drawConfig.currentOrigin.x / totalDayWidth) * -1);
		float startPixel = drawConfig.currentOrigin.x
				+ totalDayWidth * leftDaysWithGaps
				+ drawConfig.timeColumnWidth;

		final List<Calendar> dayRange = new ArrayList<>();
		if (config.isSingleDay()) {
			final Calendar day = (Calendar) viewState.firstVisibleDay.clone();
			dayRange.add(day);
		} else {
			int offset = 0;
			int today = DateUtils.today().get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;
			if (today == 0)
				today = 7;

			if (today - Calendar.MONDAY + 1 < config.numberOfVisibleDays)
				offset = today - Calendar.MONDAY + 1;

			//final int start = (int) days(DateUtils.today(), DateUtils.today(leftDaysWithGaps + 1 - offset));
			final int start = leftDaysWithGaps + 1 - offset + days(-offset, leftDaysWithGaps - offset);
			final int size = config.numberOfVisibleDays + 1;
			// TODO: Dynamic week length
			dayRange.addAll(DateUtils.getDateRange(start, size, Calendar.MONDAY, Calendar.FRIDAY));

			// TODO: Remove debugging
			//config.debug = Integer.toString(leftDaysWithGaps);
			config.debug = (-offset) + "-" + (leftDaysWithGaps - offset);
			config.debug = Integer.toString(days(-offset, -offset + leftDaysWithGaps));
			//config.debug = Long.toString(leftDaysWithGaps);
			//config.debug = Long.toString(leftDaysWithGaps - offset) + "-" + (leftDaysWithGaps + 5 - offset);
		}

		return new DrawingContext(dayRange, startPixel);
	}

	private static int days(int start, int end) {
		int val = (end - start) / 5 * 2;

		if (start > end)
			val -= 2;

		return val;
	}
}
