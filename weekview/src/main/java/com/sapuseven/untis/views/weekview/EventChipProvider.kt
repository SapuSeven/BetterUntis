package com.sapuseven.untis.views.weekview

import android.view.View
import com.sapuseven.untis.views.weekview.config.WeekViewConfig
import com.sapuseven.untis.views.weekview.loaders.WeekViewLoader
import org.joda.time.DateTime
import java.util.*
import kotlin.math.max

internal class EventChipProvider<T>(
		private val config: WeekViewConfig,
		private val data: WeekViewData<T>,
		private val viewState: WeekViewViewState
) {
	var weekViewLoader: WeekViewLoader<T>? = null

	fun loadEventsIfNecessary(view: View, day: DateTime) {
		if (view.isInEditMode) return

		weekViewLoader?.toWeekViewPeriodIndex(day)?.toInt()?.let { periodIndexToLoad ->
			if (data.fetchedPeriod != periodIndexToLoad || viewState.shouldRefreshEvents)
				loadEventsAndCalculateEventChipPositions(view, periodIndexToLoad)
			viewState.shouldRefreshEvents = false
		}
	}

	private fun loadEventsAndCalculateEventChipPositions(view: View, periodIndex: Int) {
		if (weekViewLoader == null && !view.isInEditMode) throw IllegalStateException("You must provide a PeriodChangeListener")

		if (viewState.shouldRefreshEvents)
			data.clear()

		loadEvents(periodIndex)

		computePositionOfEvents(data.eventChips)
	}

	private fun loadEvents(periodToFetch: Int) {
		var previousPeriodEvents: List<WeekViewEvent<T>>? = null
		var currentPeriodEvents: List<WeekViewEvent<T>>? = null
		var nextPeriodEvents: List<WeekViewEvent<T>>? = null

		// Check if periods are already fetched and skip them
		if (data.previousPeriodEvents != null
				&& data.currentPeriodEvents != null
				&& data.nextPeriodEvents != null) {
			when (periodToFetch) {
				data.fetchedPeriod - 1 -> {
					currentPeriodEvents = data.previousPeriodEvents
					nextPeriodEvents = data.currentPeriodEvents
				}
				data.fetchedPeriod -> {
					previousPeriodEvents = data.previousPeriodEvents
					currentPeriodEvents = data.currentPeriodEvents
					nextPeriodEvents = data.nextPeriodEvents
				}
				data.fetchedPeriod + 1 -> {
					previousPeriodEvents = data.currentPeriodEvents
					currentPeriodEvents = data.nextPeriodEvents
				}
			}
		}

		if (currentPeriodEvents == null) currentPeriodEvents = weekViewLoader?.onLoad(periodToFetch)?.sorted()
		if (previousPeriodEvents == null) previousPeriodEvents = weekViewLoader?.onLoad(periodToFetch - 1)?.sorted()
		if (nextPeriodEvents == null) nextPeriodEvents = weekViewLoader?.onLoad(periodToFetch + 1)?.sorted()

		// Clear events.
		data.eventChips.clear()
		previousPeriodEvents?.let { data.cacheEvents(it) }
		currentPeriodEvents?.let { data.cacheEvents(it) }
		nextPeriodEvents?.let { data.cacheEvents(it) }

		data.previousPeriodEvents = previousPeriodEvents
		data.currentPeriodEvents = currentPeriodEvents
		data.nextPeriodEvents = nextPeriodEvents
		data.fetchedPeriod = periodToFetch
	}

	/**
	 * Calculates the left and right positions of each events. This comes handy especially if events
	 * are overlapping.
	 *
	 * @param eventChips The events along with their wrapper class.
	 */
	private fun computePositionOfEvents(eventChips: List<EventChip<T>>) {
		// Make "collision groups" for all events that collide with others.
		val collisionGroups = mutableListOf<MutableList<EventChip<*>>>()
		for (eventChip in eventChips) {
			var isPlaced = false

			outerLoop@ for (collisionGroup in collisionGroups) {
				for (groupEvent in collisionGroup) {
					if (groupEvent.event.collidesWith(eventChip.event)) {
						collisionGroup.add(eventChip)
						isPlaced = true
						break@outerLoop
					}
				}
			}

			if (!isPlaced) {
				val newGroup = ArrayList<EventChip<*>>()
				newGroup.add(eventChip)
				collisionGroups.add(newGroup)
			}
		}

		for (collisionGroup in collisionGroups) {
			expandEventsToMaxWidth(collisionGroup)
		}
	}

	/**
	 * Expands all the events to maximum possible width. The events will try to occupy maximum
	 * space available horizontally.
	 *
	 * @param collisionGroup The group of events which overlap with each other.
	 */
	private fun expandEventsToMaxWidth(collisionGroup: List<EventChip<*>>) {
		// Expand the events to maximum possible width.
		val columns = mutableListOf<MutableList<EventChip<*>>>()
		columns.add(ArrayList())

		for (eventChip in collisionGroup) {
			var isPlaced = false

			for (column in columns) {
				if (column.isEmpty()) {
					column.add(eventChip)
					isPlaced = true
				} else if (!eventChip.event.collidesWith(column[column.size - 1].event)) {
					column.add(eventChip)
					isPlaced = true
					break
				}
			}

			if (!isPlaced) {
				val newColumn = ArrayList<EventChip<*>>()
				newColumn.add(eventChip)
				columns.add(newColumn)
			}
		}

		// Calculate left and right position for all the events.
		// Get the maxRowCount by looking in all columns.
		var maxRowCount = 0
		for (column in columns) {
			maxRowCount = max(maxRowCount, column.size)
		}

		for (i in 0 until maxRowCount) {
			// Set the left and right values of the event.
			var j = 0.0f
			for (column in columns) {
				if (column.size >= i + 1) {
					val eventChip = column[i]
					eventChip.width = 1.0f / columns.size
					eventChip.left = j / columns.size

					eventChip.top = (eventChip.event.startTime.hourOfDay * 60 + eventChip.event.startTime.minuteOfHour - config.startTime).toFloat()
					eventChip.bottom = (eventChip.event.endTime.hourOfDay * 60 + eventChip.event.endTime.minuteOfHour - config.startTime).toFloat()
				}
				j++
			}
		}
	}
}
