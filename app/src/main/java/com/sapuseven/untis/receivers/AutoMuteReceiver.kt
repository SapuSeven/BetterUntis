package com.sapuseven.untis.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.sapuseven.untis.services.AutoMuteService
import com.sapuseven.untis.ui.pages.settings.UserSettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class AutoMuteReceiver @Inject constructor(
	userSettingsRepositoryFactory: UserSettingsRepository.Factory,
	private val autoMuteService: AutoMuteService
) : BroadcastReceiver() {
	val settingsRepository = userSettingsRepositoryFactory.create()

	companion object {
		const val EXTRA_BOOLEAN_MUTE = "com.sapuseven.untis.automute.mute"
		const val EXTRA_INT_ID = "com.sapuseven.untis.automute.id"
		const val EXTRA_LONG_USER_ID = "com.sapuseven.untis.automute.userId"

		const val PREFERENCE_KEY_INTERRUPTION_FILTER = "automuteInterruptionFilterBackup"
		const val PREFERENCE_KEY_RINGER_MODE = "automuteRingerModeBackup"
	}

	override fun onReceive(context: Context, intent: Intent) = runBlocking {
		Log.d(
			"AutoMuteReceiver",
			"AutoMuteReceiver received, mute = ${intent.getBooleanExtra(EXTRA_BOOLEAN_MUTE, false)}"
		)

		if (intent.hasExtra(EXTRA_BOOLEAN_MUTE)) {
			if (intent.getBooleanExtra(EXTRA_BOOLEAN_MUTE, false)) {
				// Mute
				if (!automuteEnable) return@runBlocking

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					val notificationManager =
						context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
					val interruptionFilter =
						if (automutePriority)
							NotificationManager.INTERRUPTION_FILTER_NONE
						else
							NotificationManager.INTERRUPTION_FILTER_PRIORITY

					if (!prefs.contains(PREFERENCE_KEY_INTERRUPTION_FILTER)) {
						editor.putInt(
							PREFERENCE_KEY_INTERRUPTION_FILTER,
							notificationManager.currentInterruptionFilter
						)
						Log.d(
							"AutoMuteReceiver",
							"Saved interruption filter: ${notificationManager.currentInterruptionFilter}"
						)
					}
					notificationManager.setInterruptionFilter(interruptionFilter)
				} else {
					val audioManager =
						context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
					editor.putInt(PREFERENCE_KEY_RINGER_MODE, audioManager.ringerMode)
					audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
				}
			} else {
				// Unmute
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					val notificationManager =
						context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
					notificationManager.setInterruptionFilter(
						prefs.getInt(
							PREFERENCE_KEY_INTERRUPTION_FILTER,
							NotificationManager.INTERRUPTION_FILTER_ALL
						)
					)
					editor.remove(PREFERENCE_KEY_INTERRUPTION_FILTER)
				} else {
					val audioManager =
						context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
					audioManager.ringerMode =
						prefs.getInt(PREFERENCE_KEY_RINGER_MODE, AudioManager.RINGER_MODE_NORMAL)
				}
			}

			editor.apply()
		}
	}
}
