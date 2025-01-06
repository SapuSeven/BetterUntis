package com.sapuseven.untis.api.model.request

import com.sapuseven.untis.api.model.untis.Auth
import com.sapuseven.untis.api.model.untis.enumeration.DeviceOs
import kotlinx.serialization.Serializable

@Serializable
data class UserDataParams(
	val elementId: Int = 0,
	val deviceOs: DeviceOs = DeviceOs.AND,
	val deviceOsVersion: String = "",
	val auth: Auth?
) : BaseParams()
