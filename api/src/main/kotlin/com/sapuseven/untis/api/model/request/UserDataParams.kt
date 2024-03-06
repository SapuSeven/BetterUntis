package com.sapuseven.untis.api.model.request

import kotlinx.serialization.Serializable

@Serializable
data class UserDataParams(
		val elementId: Int = 0,
		val deviceOs: String = "AND",
		val deviceOsVersion: String = ""
) : BaseParams()
