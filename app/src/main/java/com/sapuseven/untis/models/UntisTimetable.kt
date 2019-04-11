package com.sapuseven.untis.models

import com.sapuseven.untis.models.untis.MasterData
import com.sapuseven.untis.models.untis.Timetable
import kotlinx.serialization.Serializable

@Serializable
data class UntisTimetable(
		val timetable: Timetable,
		val masterData: MasterData
)