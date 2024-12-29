package com.sapuseven.untis.ui.weekview

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.util.*


private enum class EventType {
	EVENT_START,
	EVENT_STOP
}

private data class TimelineEvent(
	val eventTime: LocalDateTime,
	val eventType: EventType,
	val eventId: Int,
	val eventData: Event<*>
)

private class EventComparator : Comparator<TimelineEvent> {
	override fun compare(o1: TimelineEvent, o2: TimelineEvent): Int {
		if (o1.eventTime > o2.eventTime) return 1
		if (o1.eventTime < o2.eventTime) return -1
		if (o1.eventType === EventType.EVENT_START && o2.eventType === EventType.EVENT_STOP) return 1
		if (o1.eventType === EventType.EVENT_STOP && o2.eventType === EventType.EVENT_START) return -1
		if (o1.eventId > o2.eventId) return 1
		if (o1.eventId < o2.eventId) return -1
		return 0
	}
}

// Based on https://stackoverflow.com/questions/53215825/return-optimized-x-coordinates-to-normalize-maximize-area-for-an-array-of-rectan#answer-53222638
// (Archive: https://web.archive.org/web/20230222010425/https://stackoverflow.com/questions/53215825/return-optimized-x-coordinates-to-normalize-maximize-area-for-an-array-of-rectan)
internal fun <T> arrangeEvents(events: List<Event<T>>, maxSimultaneous: Int) {
	val eventQueue: PriorityQueue<TimelineEvent> = PriorityQueue(EventComparator())
	val regionQueue: Queue<TimelineEvent> = LinkedList()
	val startedEventIds: MutableList<Int> = mutableListOf()

	events.forEachIndexed { i, event ->
		val startEvent = TimelineEvent(event.start, EventType.EVENT_START, i, event)
		eventQueue.add(startEvent)
		val stopEvent = TimelineEvent(event.end, EventType.EVENT_STOP, i, event)
		eventQueue.add(stopEvent)
	}

	while (!eventQueue.isEmpty()) {
		var overlap = 0
		var numOverlappping = 0
		var event: TimelineEvent
		while (!eventQueue.isEmpty()) { // take from the event queue
			event = eventQueue.remove()
			regionQueue.add(event) // save in the region queue

			// There may be a solution with using the region queue for this, but for now handle it manually.
			// If any 2+ events are started together, update the simultaneous event list for both.
			startedEventIds.forEach { events[it].simultaneousEvents.addAll(startedEventIds.map { events[it] }) }

			if (event.eventType === EventType.EVENT_START) {
				overlap++
				startedEventIds.add(event.eventId)
			} else {
				overlap--
				startedEventIds.remove(event.eventId)
			}
			if (overlap == 0) // reached the end of a region
				break
			if (overlap > numOverlappping) numOverlappping = overlap
		}

		// limit the overlap as specified by the function parameter
		if (numOverlappping > maxSimultaneous) numOverlappping = maxSimultaneous

		val usedColumns = IntArray(numOverlappping)
		for (i in 0 until numOverlappping) usedColumns[i] = -1
		while (!regionQueue.isEmpty()) {
			event = regionQueue.remove()
			if (event.eventType === EventType.EVENT_START) {
				// find an available column for this rectangle, and assign the X values
				for (column in 0 until numOverlappping) {
					if (usedColumns[column] < 0) {
						usedColumns[column] = event.eventId
						events[event.eventId].offsetSteps = column
						events[event.eventId].numSimultaneous = numOverlappping
						break
					}
				}
			} else {
				// free the column that's being used for this rectangle
				for (i in 0 until numOverlappping) {
					if (usedColumns[i] == event.eventId) {
						usedColumns[i] = -1
						break
					}
				}
			}
		}
	}

	eventQueue.clear()
	regionQueue.clear()
}
