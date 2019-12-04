package com.sapuseven.untis.views.weekview.listeners

import org.joda.time.DateTime

interface EmptyViewClickListener {

	/**
	 * Triggered when the users clicks on a empty space of the calendar.
	 *
	 * @param time A [DateTime] object set with the date and time of the clicked position on the view.
	 */
	fun onEmptyViewClicked(time: DateTime)
}
