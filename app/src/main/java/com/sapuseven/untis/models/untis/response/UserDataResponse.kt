package com.sapuseven.untis.models.untis.response

import com.sapuseven.untis.models.UntisUserData
import kotlinx.serialization.Serializable

@Serializable
data class UserDataResponse(
		val result: UntisUserData? = null
) : BaseResponse()