package com.sapuseven.untis.models.untis.masterdata

import com.sapuseven.untis.models.untis.masterdata.timegrid.Day
import kotlinx.serialization.Serializable

@Serializable
data class TimeGrid(
		val days: List<Day>
) {
	fun hasEqualDays(): Boolean {
		for (i in 1..days.size) {
			if (days[i] != days[i - 1])
				return false
		}

		return true
	}
}