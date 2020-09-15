package com.sapuseven.untis.models.untis.masterdata

import com.sapuseven.untis.models.untis.masterdata.timegrid.Day
import com.sapuseven.untis.models.untis.masterdata.timegrid.Unit
import kotlinx.serialization.Serializable
import org.joda.time.LocalDateTime

@Serializable
data class TimeGrid(
		val days: List<Day>
) {
	companion object {
		fun generateDefault(): TimeGrid {
			val dateTimeObject = LocalDateTime.now()

			return TimeGrid(
					// Range of week days to include (0 = Sunday, 1 = Monday, ...)
					(1..5).map {
						Day(
								dateTimeObject.withDayOfWeek(it).toString("E"),
								// Range of hours to include
								(6..22).map { hourIndex ->
									Unit(
											hourIndex.toString(),
											"T" + hourIndex.toString().padStart(2, '0') + ":00",
											"T" + (if (hourIndex < 23) hourIndex + 1 else 0).toString().padStart(2, '0') + ":00"
									)
								}
						)
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
