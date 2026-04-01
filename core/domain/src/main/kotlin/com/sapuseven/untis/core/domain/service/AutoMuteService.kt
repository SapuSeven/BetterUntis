package com.sapuseven.untis.core.domain.service

import com.sapuseven.untis.core.model.user.User

interface AutoMuteService {
	fun setUser(user: User) {}

	fun isPermissionGranted(): Boolean
	fun isAutoMuteEnabled(): Boolean

	fun autoMuteEnable()
	fun autoMuteDisable()
	fun autoMuteStateOn()
	fun autoMuteStateOff()
}
