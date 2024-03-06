package com.sapuseven.untis.api.model.response

import kotlinx.serialization.Serializable

@Serializable
data class AppSharedSecretResponse(
		val result: String? = ""
) : BaseResponse()
