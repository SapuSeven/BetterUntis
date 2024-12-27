package com.sapuseven.untis.api.model.response

import com.sapuseven.untis.api.model.untis.MasterData
import com.sapuseven.untis.api.model.untis.messenger.MessengerSettings
import com.sapuseven.untis.api.model.untis.Settings
import com.sapuseven.untis.api.model.untis.UserData
import kotlinx.serialization.Serializable

@Serializable
data class UserDataResponse(
		val result: UserDataResult? = null
) : BaseResponse()

@Serializable
data class UserDataResult(
	val masterData: MasterData,
	val userData: UserData,
	val settings: Settings? = null,
	val messengerSettings: MessengerSettings? = null
)
