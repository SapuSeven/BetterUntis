package com.sapuseven.untis.api.model.response

import kotlinx.serialization.Serializable

@Serializable
data class AuthTokenResponse(
		val result: AuthTokenResult? = null
) : BaseResponse()

@Serializable
data class AuthTokenResult(
	val token: String
)
