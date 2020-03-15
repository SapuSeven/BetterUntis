package com.sapuseven.untis.models.untis

import kotlinx.serialization.Serializable

@Serializable
data class UntisMessengerSettings(
		val serverUrl: String,
		val organizationId: String
)
