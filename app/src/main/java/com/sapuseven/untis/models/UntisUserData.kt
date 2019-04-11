package com.sapuseven.untis.models

import com.sapuseven.untis.models.untis.MasterData
import com.sapuseven.untis.models.untis.Settings
import com.sapuseven.untis.models.untis.UserData
import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
data class UntisUserData(
		val masterData: MasterData,
		val userData: UserData,
		val settings: Settings,
		@Optional val messengerSettings: Any? = null // This is a new element with unknown usage
)