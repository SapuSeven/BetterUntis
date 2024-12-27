package com.sapuseven.untis.api.model.untis.classreg

import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import kotlinx.serialization.Serializable

@Serializable
data class ClassRegEvent(
	val id: Long,
	val eventReasonId: Long,
	val elementId: Long,
	val elementType: ElementType?,
	val text: String?,
	val dateTime: String?,
	val write: Boolean,
	val delete: Boolean
)
