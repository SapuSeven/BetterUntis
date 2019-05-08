package com.alamkanak.weekview;

import android.content.Context;
import android.text.format.DateFormat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by jesse on 6/02/2016.
 */
class DateUtils {
	@SuppressWarnings("SameParameterValue")
	static List<Calendar> getDateRange(int daysSinceToday, int size, int weekStart, int weekEnd) {
		final List<Calendar> days = new ArrayList<>();
		Calendar day;

		int today = today().get(Calendar.DAY_OF_WEEK);
		if (today > weekEnd) today -= 7;
		int offset = (today < weekStart) ? weekStart - today : 0;
		for (int dayNumber = daysSinceToday; days.size() <= size; dayNumber++) {
			day = today();
			day.add(Calendar.DATE, dayNumber - 1 + offset);

			if (day.get(Calendar.DAY_OF_WEEK) >= weekStart && day.get(Calendar.DAY_OF_WEEK) <= weekEnd)
				days.add(day);
		}

		return days;
	}

	static Calendar withTimeAtStartOfDay(Calendar date) {
		date.set(Calendar.HOUR_OF_DAY, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		return date;
	}

	static Calendar withTimeAtEndOfDay(Calendar date) {
		date.set(Calendar.HOUR_OF_DAY, 23);
		date.set(Calendar.MINUTE, 59);
		date.set(Calendar.SECOND, 59);
		date.set(Calendar.MILLISECOND, 999);
		return date;
	}

	static int getDaysUntilDate(Calendar date) {
		final long dateInMillis = date.getTimeInMillis();
		final long todayInMillis = today().getTimeInMillis();
		final long diff = dateInMillis - todayInMillis;
		return (int) (diff / Constants.DAY_IN_MILLIS);
	}

	static int getDisplayedDays(Calendar startDay, int size, int weekStart, int weekEnd) {
		int startDayIndex = startDay.get(Calendar.DAY_OF_WEEK);
		if (startDayIndex > weekEnd) startDayIndex -= 7; // TODO: Is this line correct?
		int offsetForWeekStart = (startDayIndex > weekStart) ? startDayIndex - weekStart : 0;

		int days = 0;

		for (int i = 0; i < Math.abs(size); i++) {
			startDay.add(Calendar.DATE, size > 0 ? 1 : -1);
			if (startDay.get(Calendar.DAY_OF_WEEK) >= weekStart && startDay.get(Calendar.DAY_OF_WEEK) <= weekEnd)
				days += size > 0 ? 1 : -1;
		}

		return days + offsetForWeekStart;
	}

	/**
	 * Checks if two hourLines are on the same day.
	 *
	 * @param dayOne The first day.
	 * @param dayTwo The second day.
	 * @return Whether the hourLines are on the same day.
	 */
	static boolean isSameDay(Calendar dayOne, Calendar dayTwo) {
		return dayOne.get(Calendar.YEAR) == dayTwo.get(Calendar.YEAR)
				&& dayOne.get(Calendar.DAY_OF_YEAR) == dayTwo.get(Calendar.DAY_OF_YEAR);
	}


	/**
	 * Returns a calendar instance at the start of this day
	 *
	 * @return the calendar instance
	 */
	static Calendar today() {
		return today(0);
	}

	/**
	 * Returns a calendar instance at the start of this day
	 *
	 * @param offset the days to add to today
	 * @return the calendar instance
	 */
	static Calendar today(int offset) {
		final Calendar today = Calendar.getInstance();
		today.set(Calendar.HOUR_OF_DAY, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.SECOND, 0);
		today.set(Calendar.MILLISECOND, 0);
		today.add(Calendar.DATE, offset);
		return today;
	}

	static SimpleDateFormat getTimeFormat(Context context) {
		return DateFormat.is24HourFormat(context)
				? new SimpleDateFormat("H:mm", Locale.getDefault())
				: new SimpleDateFormat("h:mm a", Locale.getDefault());
	}
}
