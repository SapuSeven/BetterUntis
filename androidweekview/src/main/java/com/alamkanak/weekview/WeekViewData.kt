package com.alamkanak.weekview

class WeekViewData<T> {
	var eventChips: MutableList<EventChip<T>>? = null

	var normalEventChips: MutableList<EventChip<T>> = mutableListOf()
	var allDayEventChips: MutableList<EventChip<T>> = mutableListOf()

	internal var previousPeriodEvents: List<WeekViewEvent<T>>? = null
	internal var currentPeriodEvents: List<WeekViewEvent<T>>? = null
	internal var nextPeriodEvents: List<WeekViewEvent<T>>? = null

	internal var fetchedPeriod = -1 // the middle period the calendar has fetched.

	internal fun setEventChips(eventChips: MutableList<EventChip<T>>) {
		this.eventChips = eventChips
		normalEventChips.clear()
		allDayEventChips.clear()

		for (eventChip in eventChips) {
			if (eventChip.event.isAllDay)
				allDayEventChips.add(eventChip)
			else
				normalEventChips.add(eventChip)
		}
	}

	internal fun clearEventChipsCache() {
		eventChips?.forEach {
			it.rect = null
		}
	}

	internal fun clear() {
		eventChips?.clear()
		previousPeriodEvents = null
		currentPeriodEvents = null
		nextPeriodEvents = null
		fetchedPeriod = -1
	}

	/**
	 * Shortcut for calling cacheEvent(WeekViewEvent<T>) on every list item.
	 *
	 * @param events The events to be cached.
	 */
	internal fun cacheEvents(events: List<WeekViewEvent<T>>) {
		for (event in events.sorted())
			cacheEvent(event)
	}

	/**
	 * Cache the event for smooth scrolling functionality.
	 *
	 * @param event The event to cache.
	 */
	private fun cacheEvent(event: WeekViewEvent<T>) {
		if (event.startTime >= event.endTime) return

		event.splitWeekViewEvents().forEach {
			eventChips?.add(EventChip(it, event, null))
		}
	}
}
