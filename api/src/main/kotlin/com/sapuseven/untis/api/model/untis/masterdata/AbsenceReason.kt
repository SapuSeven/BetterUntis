package com.sapuseven.untis.api.model.untis.masterdata

import kotlinx.serialization.Serializable

@Serializable
data class AbsenceReason(
	val id: Long,
	val name: String,
	val longName: String,
	val active: Boolean,
	val automaticNotificationEnabled: Boolean? = null // Note: This value seems to be present in the response, but missing in the Untis Mobile sources
)
