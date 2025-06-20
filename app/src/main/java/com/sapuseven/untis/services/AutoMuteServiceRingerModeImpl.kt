package com.sapuseven.untis.services

import android.media.AudioManager
import javax.inject.Inject

class AutoMuteServiceRingerModeImpl @Inject constructor(
	private val audioManager: AudioManager,
) : AutoMuteService {
	override fun isPermissionGranted(): Boolean = true

	override fun isAutoMuteEnabled(): Boolean = true

	override fun autoMuteEnable() {
		// Nothing to do
	}

	override fun autoMuteDisable() {
		// Nothing to do
	}

	override fun autoMuteStateOn() {
		// TODO: Save original ringer mode
		audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
	}

	override fun autoMuteStateOff() {
		// TODO: Restore saved ringer mode
		audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
	}
}
