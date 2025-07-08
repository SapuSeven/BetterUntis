package com.sapuseven.untis.api.model.request

import com.sapuseven.untis.api.model.untis.Auth
import kotlinx.serialization.Serializable

@Serializable
data class AuthTokenParams(
	val auth: Auth
) : BaseParams()
