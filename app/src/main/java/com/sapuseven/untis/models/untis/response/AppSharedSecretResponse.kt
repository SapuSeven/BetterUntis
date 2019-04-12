package com.sapuseven.untis.models.untis.response

import kotlinx.serialization.Serializable

@Serializable
data class AppSharedSecretResponse(
		val result: String? = ""
) : BaseResponse()