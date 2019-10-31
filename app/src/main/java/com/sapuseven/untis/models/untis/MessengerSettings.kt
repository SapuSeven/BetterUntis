package com.sapuseven.untis.models.untis

import kotlinx.serialization.Serializable

@Serializable
data class MessengerSettings(
		val serverUrl: String,
		val organizationId: String
)
