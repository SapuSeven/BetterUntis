package com.sapuseven.untis.models.untis

import kotlinx.serialization.Serializable

@Serializable
data class UntisAuth(
		val user: String,
		val otp: Long,
		val clientTime: Long
)