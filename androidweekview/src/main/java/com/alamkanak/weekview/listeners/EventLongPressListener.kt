package com.alamkanak.weekview.listeners

import android.graphics.RectF

interface EventLongPressListener<T> {

	/**
	 * Similar to [EventClickListener] but with a long press.
	 *
	 * @param data:     event clicked.
	 * @param eventRect: view containing the clicked event.
	 */
	fun onEventLongPress(data: T, eventRect: RectF)

}
