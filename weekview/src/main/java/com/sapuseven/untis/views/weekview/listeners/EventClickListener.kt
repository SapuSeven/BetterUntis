package com.sapuseven.untis.views.weekview.listeners

import android.graphics.RectF

interface EventClickListener<T> {

	/**
	 * Triggered when clicked on one existing event
	 *
	 * @param data:     event clicked.
	 * @param eventRect: view containing the clicked event.
	 */
	fun onEventClick(data: T, eventRect: RectF)
}
