package com.sapuseven.untis.views.weekview.listeners

import org.joda.time.DateTime

interface EmptyViewLongPressListener {

	/**
	 * Similar to [EmptyViewClickListener] but with long press.
	 *
	 * @param time A [DateTime] object set with the date and time of the long pressed position on the view.
	 */
	fun onEmptyViewLongPress(time: DateTime)
}
