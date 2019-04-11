package com.sapuseven.untis.models.untis.response

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
data class AppSharedSecretResponse(
		@Optional val result: String? = ""
) : BaseResponse()