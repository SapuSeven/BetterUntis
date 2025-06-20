package com.sapuseven.untis.services

import android.app.NotificationManager
import android.os.Build
import androidx.annotation.RequiresApi
import javax.inject.Inject

/**
 * Implementation of the AutoMuteService for API 23 (M) and above.
 *
 * This implementation utilizes the notification manager to set the notification interruption filter.
 */
@RequiresApi(Build.VERSION_CODES.M)
class AutoMuteServiceInterruptionFilterImpl @Inject constructor(
	private val notificationManager: NotificationManager,
) : AutoMuteService {
	override fun isPermissionGranted(): Boolean =
		notificationManager.isNotificationPolicyAccessGranted

	override fun isAutoMuteEnabled(): Boolean = true

	override fun autoMuteEnable() {
		// Nothing to do
	}

	override fun autoMuteDisable() {
		// Nothing to do
	}

	override fun autoMuteStateOn() {
		val interruptionFilter =
			if (/*allowPriority*/false)
				NotificationManager.INTERRUPTION_FILTER_PRIORITY
			else
				NotificationManager.INTERRUPTION_FILTER_NONE

		// TODO: Save original interruption filter
		notificationManager.setInterruptionFilter(interruptionFilter)
	}

	override fun autoMuteStateOff() {
		// TODO: Restore original interruption filter
		notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
	}
}
