package com.sapuseven.untis.ui.weekview

import org.joda.time.LocalDateTime
import java.util.*


private enum class EventType {
	EVENT_START,
	EVENT_STOP
}

private data class TimelineEvent(
	val eventTime: LocalDateTime,
	val eventType: EventType,
	val eventId: Int
)

private class EventComparator : Comparator<TimelineEvent> {
	override fun compare(o1: TimelineEvent, o2: TimelineEvent): Int {
		if (o1.eventTime < o2.eventTime) return -1
		if (o1.eventTime > o2.eventTime) return 1
		if (o1.eventType === EventType.EVENT_START && o2.eventType === EventType.EVENT_STOP) return 1
		if (o1.eventType === EventType.EVENT_STOP && o2.eventType === EventType.EVENT_START) return -1
		if (o1.eventId < o2.eventId) return -1
		return if (o1.eventId > o2.eventId) 1 else 0
	}
}

// Based on https://stackoverflow.com/questions/53215825/return-optimized-x-coordinates-to-normalize-maximize-area-for-an-array-of-rectan#answer-53222638
// (Archive: https://web.archive.org/web/20230222010425/https://stackoverflow.com/questions/53215825/return-optimized-x-coordinates-to-normalize-maximize-area-for-an-array-of-rectan)
internal fun arrangeEvents(events: List<Event>, maxSimultaneous: Int) {
	val eventQueue: PriorityQueue<TimelineEvent> = PriorityQueue(EventComparator())
	val regionQueue: Queue<TimelineEvent> = LinkedList()

	for (i in 0 until events.size) {
		val startEvent = TimelineEvent(events[i].start, EventType.EVENT_START, i)
		eventQueue.add(startEvent)
		val stopEvent = TimelineEvent(events[i].end, EventType.EVENT_STOP, i)
		eventQueue.add(stopEvent)
	}

	while (!eventQueue.isEmpty()) {
		var overlap = 0
		var numOverlappping = 0
		var event: TimelineEvent
		while (!eventQueue.isEmpty()) { // take from the event queue
			event = eventQueue.remove()
			regionQueue.add(event) // save in the region queue
			if (event.eventType === EventType.EVENT_START) overlap++ else overlap--
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
