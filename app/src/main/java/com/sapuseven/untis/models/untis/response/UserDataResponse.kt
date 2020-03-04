package com.sapuseven.untis.models.untis.response

import com.sapuseven.untis.models.untis.UntisMasterData
import com.sapuseven.untis.models.untis.UntisMessengerSettings
import com.sapuseven.untis.models.untis.UntisSettings
import com.sapuseven.untis.models.untis.UntisUserData
import kotlinx.serialization.Serializable

@Serializable
data class UserDataResponse(
		val result: UserDataResult? = null
) : BaseResponse()

@Serializable
data class UserDataResult(
		val masterData: UntisMasterData,
		val userData: UntisUserData,
		val settings: UntisSettings? = null,
		val messengerSettings: UntisMessengerSettings? = null
)
