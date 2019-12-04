package com.sapuseven.untis.views.weekview.drawers

import android.graphics.Canvas
import android.graphics.RectF
import com.sapuseven.untis.views.weekview.*
import com.sapuseven.untis.views.weekview.config.WeekViewConfig
import org.joda.time.DateTime

class EventsDrawer<T>(private val config: WeekViewConfig) {
	private val rectCalculator: EventChipRectCalculator = EventChipRectCalculator(config)

	internal fun drawEvents(eventChips: List<EventChip<T>>, drawingContext: DrawingContext, canvas: Canvas) {
		var startPixel = drawingContext.startPixel
		val now = DateTime.now().millis

		val freeDays = mutableListOf<Pair<DateTime, Float>>()
		drawingContext.dayRange.forEach { day ->
			if (config.isSingleDay)
				startPixel += config.eventMarginHorizontal

			if (drawEventsForDate(eventChips, day, now, startPixel, canvas)) freeDays.add(day to startPixel)

			startPixel += config.totalDayWidth
		}
		drawingContext.freeDays = freeDays
	}

	private fun drawEventsForDate(eventChips: List<EventChip<T>>, date: DateTime, nowMillis: Long, startFromPixel: Float, canvas: Canvas): Boolean {
		var freeDays = true
		eventChips.forEach { eventChip ->
			val event = eventChip.event
			if (!event.isSameDay(date)) return@forEach else freeDays = false

			val chipRect = SplitRect(
					rectCalculator.calculateEventRect(eventChip, startFromPixel),
					calculateDivision(event, nowMillis)
			)
			if (isValidEventRect(chipRect)) {
				eventChip.rect = chipRect
				eventChip.draw(config, canvas)
			} else {
				eventChip.rect = null
			}
		}
		return freeDays
	}

	private fun calculateDivision(event: WeekViewEvent<*>, nowMillis: Long): Float {
		val eventStartMillis = event.startTime.millis
		val eventEndMillis = event.endTime.millis

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
