package com.sapuseven.untis.api.model.untis.classreg

import kotlinx.serialization.Serializable

@Serializable
data class SeatingPlan(
	val id: Long,
	val name: String,
	val numberOfColumns: Int,
	val numberOfRows: Int,
	val students: List<SeatingPlanStudent> = emptyList()
)
