package com.sapuseven.untis.api.model.untis.masterdata

import com.sapuseven.untis.api.model.untis.masterdata.timegrid.Day
import com.sapuseven.untis.api.model.untis.masterdata.timegrid.Unit
import kotlinx.serialization.Serializable
import java.time.DayOfWeek
import java.time.LocalTime

@Serializable
data class TimeGrid(
	val days: List<Day>
) {
	companion object {
		fun generateDefault(): TimeGrid {
			val unitsForDay = (6..22).map { hourIndex -> // Range of hours to include
				Unit(
					hourIndex.toString(),
					LocalTime.of(hourIndex, 0),
					LocalTime.of(if (hourIndex < 23) hourIndex + 1 else 0, 0)
				)
			}

			return TimeGrid(
				// Range of week days to include (0 = Sunday, 1 = Monday, ...)
				(1..5).map {
					Day(DayOfWeek.of(it), unitsForDay)
				})
		}
	}

	fun hasEqualDays(): Boolean {
		for (i in 1..days.size) {
			if (days[i] != days[i - 1])
				return false
		}

		return true
	}
}
