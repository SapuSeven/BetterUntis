package com.sapuseven.untis.models

import com.sapuseven.untis.api.model.untis.masterdata.timegrid.Day
import com.sapuseven.untis.api.model.untis.masterdata.timegrid.Unit
import com.sapuseven.untis.data.database.entities.RoomEntity


data class RoomFinderHour(
	val timeGridDay: Day,
	val timeGridUnit: Unit
)

data class RoomFinderItem(
	val entity: RoomEntity,
	val states: List<Boolean>
) {
	fun freeHoursAt(hourIndex: Int): Int {
	    return states.drop(hourIndex).takeWhile { !it }.count()
	}
}
