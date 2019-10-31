package com.alamkanak.weekview.listeners

import org.joda.time.DateTime

interface ScrollListener {
	/**
	 * Called when the first visible day has changed.
	 *
	 *
	 * (this will also be called during the first drawTimeColumn of the WeekView)
	 *
	 * @param newFirstVisibleDay The new first visible day
	 * @param oldFirstVisibleDay The old first visible day (is null on the first call).
	 */
	fun onFirstVisibleDayChanged(newFirstVisibleDay: DateTime, oldFirstVisibleDay: DateTime?)
}
