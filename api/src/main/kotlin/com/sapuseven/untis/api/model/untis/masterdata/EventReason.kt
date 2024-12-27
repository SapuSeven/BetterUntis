package com.sapuseven.untis.api.model.untis.masterdata

import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import kotlinx.serialization.Serializable

@Serializable
data class EventReason(
	val id: Long,
	val name: String,
	val longName: String,
	val elementType: ElementType,
	val groupId: Long,
	val active: Boolean
)
