package com.sapuseven.untis.services

interface AutoMuteService {
	fun isPermissionGranted(): Boolean
	fun isAutoMuteEnabled(): Boolean

	fun autoMuteEnable()
	fun autoMuteDisable()
	fun autoMuteStateOn()
	fun autoMuteStateOff()
}

class AutoMuteServiceStub : AutoMuteService {
	override fun isPermissionGranted(): Boolean = false
	override fun isAutoMuteEnabled(): Boolean = false

	override fun autoMuteEnable() {
		throw UnsupportedOperationException("Stub!")
	}

	override fun autoMuteDisable() {
		throw UnsupportedOperationException("Stub!")
	}

	override fun autoMuteStateOn() {
		throw UnsupportedOperationException("Stub!")
	}

	override fun autoMuteStateOff() {
		throw UnsupportedOperationException("Stub!")
	}
}
