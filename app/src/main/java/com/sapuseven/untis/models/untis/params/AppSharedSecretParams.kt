package com.sapuseven.untis.models.untis.params

import kotlinx.serialization.Serializable

@Serializable
data class AppSharedSecretParams(
		val userName: String,
		val password: String
) : BaseParams()