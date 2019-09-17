package com.alamkanak.weekview.listeners

import java.util.*

interface EmptyViewLongPressListener {

	/**
	 * Similar to [EmptyViewClickListener] but with long press.
	 *
	 * @param time: [Calendar] object set with the date and time of the long pressed position on the view.
	 */
	fun onEmptyViewLongPress(time: Calendar)
}
