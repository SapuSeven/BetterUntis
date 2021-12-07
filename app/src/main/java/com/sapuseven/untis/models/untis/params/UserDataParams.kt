package com.sapuseven.untis.models.untis.params

import com.sapuseven.untis.models.untis.UntisAuth
import kotlinx.serialization.Serializable

@Serializable
data class UserDataParams(
		val elementId: Int = 0,
		val deviceOs: String = "AND",
		val deviceOsVersion: String = "",
		val auth: UntisAuth
) : BaseParams()
