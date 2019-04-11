package com.alamkanak.weekview;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import androidx.annotation.NonNull;

/**
 * Created by Raquib-ul-Alam Kanak on 7/21/2014.
 * Website: http://april-shower.com
 */
public class WeekViewEvent<T> implements WeekViewDisplayable, Comparable<WeekViewEvent> {

	private static final int DEFAULT_COLOR = Color.parseColor("#9fc6e7"); // TODO: Different default color, but this is good for testing

	private long id;
	private String title;
	private String top;
	private String bottom;
	private Calendar startTime;
	private Calendar endTime;
	private int color;
	private int pastColor;
	private boolean isAllDay;

	private T data;

	public WeekViewEvent() {
	}

	public WeekViewEvent(long id, String title, Calendar startTime, Calendar endTime) {
		this(id, title, "", "", startTime, endTime);
	}

	public WeekViewEvent(long id, String title, String top, String bottom, Calendar startTime,
	                     Calendar endTime) {
		this(id, title, top, bottom, startTime, endTime, false);
	}

	public WeekViewEvent(long id, String title, String top, String bottom, Calendar startTime,
	                     Calendar endTime, boolean isAllDay) {
		this(id, title, top, bottom, startTime, endTime, 0, 0, isAllDay, null);
	}

	public WeekViewEvent(long id, Calendar startTime,
	                     Calendar endTime) {
		this(id, "", "", "", startTime, endTime);
	}

	public WeekViewEvent(long id, String title, String top, String bottom, Calendar startTime,
	                     Calendar endTime, int color, int pastColor, boolean isAllDay, T data) {
		this.id = id;
		this.title = title;
		this.top = top;
		this.bottom = bottom;
		this.startTime = startTime;
		this.endTime = endTime;
		this.color = color;
		this.pastColor = pastColor;
		this.isAllDay = isAllDay;
		this.data = data;
	}

	public Calendar getStartTime() {
		return startTime;
	}

	public void setStartTime(Calendar startTime) {
		this.startTime = startTime;
	}

	public Calendar getEndTime() {
		return endTime;
	}

	public void setEndTime(Calendar endTime) {
		this.endTime = endTime;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTop() {
		return top;
	}

	public void setTop(String top) {
		this.top = top;
	}

	public String getBottom() {
		return bottom;
	}

	public void setBottom(String bottom) {
		this.bottom = bottom;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public int getPastColor() {
		return pastColor;
	}

	public void setPastColor(int pastColor) {
		this.pastColor = pastColor;
	}

	int getColorOrDefault() {
		return (color != 0) ? color : DEFAULT_COLOR;
	}

	int getPastColorOrDefault() {
		return (pastColor != 0) ? pastColor : DEFAULT_COLOR;
	}

	boolean isAllDay() {
		return isAllDay;
	}

	public void setIsAllDay(boolean allDay) {
		this.isAllDay = allDay;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	boolean isSameDay(Calendar other) {
		return DateUtils.isSameDay(startTime, other);
	}

	boolean isSameDay(WeekViewEvent other) {
		return DateUtils.isSameDay(startTime, other.startTime);
	}

	boolean collidesWith(WeekViewEvent other) {
		long thisStart = startTime.getTimeInMillis();
		long thisEnd = endTime.getTimeInMillis();
		long otherStart = other.getStartTime().getTimeInMillis();
		long otherEnd = other.getEndTime().getTimeInMillis();
		return !((thisStart >= otherEnd) || (thisEnd <= otherStart));
	}

	@Override
	public int compareTo(@NonNull WeekViewEvent other) {
		Long thisStart = this.getStartTime().getTimeInMillis();
		Long otherStart = other.getStartTime().getTimeInMillis();

		int comparator = thisStart.compareTo(otherStart);
		if (comparator == 0) {
			Long thisEnd = this.getEndTime().getTimeInMillis();
			Long otherEnd = other.getEndTime().getTimeInMillis();
			comparator = thisEnd.compareTo(otherEnd);
		}

		return comparator;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		WeekViewEvent that = (WeekViewEvent) o;
		return id == that.id;
	}

	@Override
	public int hashCode() {
		return (int) (id ^ (id >>> 32));
	}

	/**
	 * Splits the {@link WeekViewEvent} by day into a list of {@link WeekViewEvent}s
	 *
	 * @return A list of {@link WeekViewEvent}
	 */
	List<WeekViewEvent<T>> splitWeekViewEvents() {
		List<WeekViewEvent<T>> events = new ArrayList<>();

		// The first millisecond of the next day is still the same day - no need to split events for this
		Calendar endTime = (Calendar) this.endTime.clone();
		endTime.add(Calendar.MILLISECOND, -1);

		if (!isSameDay(endTime)) {
			endTime = (Calendar) startTime.clone();
			endTime.set(Calendar.HOUR_OF_DAY, 23);
			endTime.set(Calendar.MINUTE, 59);

			WeekViewEvent<T> event1 = new WeekViewEvent<>(id, title, top, bottom, startTime, endTime, isAllDay);
			event1.setColor(color);
			event1.setPastColor(pastColor);
			events.add(event1);

			// Add other days.
			Calendar otherDay = (Calendar) startTime.clone();
			otherDay.add(Calendar.DATE, 1);

			while (!DateUtils.isSameDay(otherDay, this.endTime)) {
				Calendar overDay = (Calendar) otherDay.clone();
				overDay.set(Calendar.HOUR_OF_DAY, 0);
				overDay.set(Calendar.MINUTE, 0);

				Calendar endOfOverDay = (Calendar) overDay.clone();
				endOfOverDay.set(Calendar.HOUR_OF_DAY, 23);
				endOfOverDay.set(Calendar.MINUTE, 59);

				WeekViewEvent<T> eventMore = new WeekViewEvent<>(id, title, top, bottom, overDay, endOfOverDay, isAllDay);
				eventMore.setColor(color);
				eventMore.setPastColor(pastColor);
				events.add(eventMore);

				// Add next day.
				otherDay.add(Calendar.DATE, 1);
			}

			// Add last day.
			Calendar startTime = (Calendar) this.endTime.clone();
			startTime.set(Calendar.HOUR_OF_DAY, 0);
			startTime.set(Calendar.MINUTE, 0);

			WeekViewEvent<T> event2 = new WeekViewEvent<>(id, title, top, bottom, startTime, this.endTime, isAllDay);
			event2.setColor(color);
			event2.setPastColor(pastColor);
			events.add(event2);
		} else {
			events.add(this);
		}

		return events;
	}

	@Override
	public WeekViewEvent toWeekViewEvent() {
		return this;
	}

}
