package com.alamkanak.weekview.drawers

import android.graphics.Canvas
import android.graphics.RectF
import android.util.Log
import com.alamkanak.weekview.*
import com.alamkanak.weekview.config.WeekViewConfig
import java.util.*

class EventsDrawer<T>(private val config: WeekViewConfig) {
	private val rectCalculator: EventChipRectCalculator = EventChipRectCalculator(config)

	internal fun drawEvents(eventChips: List<EventChip<T>>, drawingContext: DrawingContext, canvas: Canvas) {
		var startPixel = drawingContext.startPixel
		val now = Calendar.getInstance().timeInMillis

		for (day in drawingContext.dayRange) {
			if (config.isSingleDay)
				startPixel += config.eventMarginHorizontal

			drawEventsForDate(eventChips, day, now, startPixel, canvas)

			startPixel += config.totalDayWidth
		}
	}

	private fun drawEventsForDate(eventChips: List<EventChip<T>>, date: Calendar, nowMillis: Long, startFromPixel: Float, canvas: Canvas) {
		val dateString = date.get(Calendar.DAY_OF_MONTH).toString() + "." + date.get(Calendar.MONTH).toString()

		eventChips.forEach { eventChip ->
			val event = eventChip.event
			if (date.get(Calendar.DAY_OF_MONTH) == 13)
				Log.d("EventsDrawer", "Drawing event ${eventChip.event.title} for date $dateString, item at ${eventChip.event.startTime.get(Calendar.DAY_OF_MONTH)}")
			if (!event.isSameDay(date)) return@forEach

			val chipRect = SplitRect(
					rectCalculator.calculateSingleEvent(eventChip, startFromPixel),
					calculateDivision(event, nowMillis)
			)
			if (isValidEventRect(chipRect)) {
				eventChip.rect = chipRect
				eventChip.draw(config, canvas)
			} else {
				eventChip.rect = null
			}
		}
		if (date.get(Calendar.DAY_OF_MONTH) == 13)
			Log.d("EventsDrawer", "Drawing cycle finished")
	}

	private fun calculateDivision(event: WeekViewEvent<*>, nowMillis: Long): Float {
		val eventStartMillis = event.startTime.timeInMillis
		val eventEndMillis = event.endTime.timeInMillis

		return when {
			nowMillis <= eventStartMillis -> 0f
			nowMillis >= eventEndMillis -> 1f
			else -> (nowMillis - eventStartMillis).toFloat() / (eventEndMillis - eventStartMillis).toFloat()
		}
	}

	private fun isValidEventRect(rect: RectF): Boolean {
		return (rect.left < rect.right
				&& rect.left < WeekView.viewWidth
				&& rect.top < WeekView.viewHeight
				&& rect.right > config.drawConfig.timeColumnWidth
				&& rect.bottom > config.drawConfig.headerHeight)
	}
}
