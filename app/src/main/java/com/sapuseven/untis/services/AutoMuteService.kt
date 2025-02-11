package com.sapuseven.untis.services

interface AutoMuteService {
	fun isPermissionGranted(): Boolean
	fun isAutoMuteEnabled(): Boolean

	fun autoMuteEnable()
	fun autoMuteDisable()
	fun autoMuteStateOn()
	fun autoMuteStateOff()
}
