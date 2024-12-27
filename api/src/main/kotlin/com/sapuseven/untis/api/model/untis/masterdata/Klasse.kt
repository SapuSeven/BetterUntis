package com.sapuseven.untis.api.model.untis.masterdata

import com.sapuseven.untis.api.serializer.Date
import kotlinx.serialization.Serializable

@Serializable
data class Klasse(
	val id: Long,
	val name: String,
	val longName: String,
	val departmentId: Long ,
	val startDate: Date,
	val endDate: Date,
	val foreColor: String?,
	val backColor: String?,
	val active: Boolean,
	val displayable: Boolean
) : Comparable<String> {
	override fun compareTo(other: String) = if (
		name.contains(other, true)
		|| longName.contains(other, true)
	) 0 else name.compareTo(other)
}
