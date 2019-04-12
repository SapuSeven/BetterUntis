package com.sapuseven.untis.adapters

import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.joda.time.Days

class RoomFinderAdapterItem(val name: String, var loading: Boolean) : Comparable<RoomFinderAdapterItem> {
	var states: BooleanArray = BooleanArray(0)
	private var startDate: DateTime = DateTime.now()
	var hourIndex: Int = 0

	val isOutdated: Boolean
		get() = Days.daysBetween(startDate, DateTime.now().withDayOfWeek(DateTimeConstants.MONDAY)).days != 0

	fun getState(index: Int): Int {
		if (loading)
			return STATE_LOADING
		var i = 0
		var hours = 0
		while (index + i < states.size && !states[index + i]) {
			hours++
			i++
		}
		return hours
	}

	override fun compareTo(other: RoomFinderAdapterItem): Int {
		val state1 = getState(hourIndex)
		val state2 = other.getState(other.hourIndex)

		return when {
			state1 < state2 -> 1
			state1 > state2 -> -1
			else -> name.compareTo(other.name)
		}
	}

	override fun hashCode(): Int {
		return name.hashCode()
	}

	override fun equals(other: Any?): Boolean {
		return other is RoomFinderAdapterItem && other.name == name
	}

	companion object {
		const val STATE_OCCUPIED = 0
		const val STATE_FREE = 1
		const val STATE_LOADING = -1
	}
}
