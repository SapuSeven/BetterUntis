package com.sapuseven.untis.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.util.Log
import com.sapuseven.untis.helpers.config.PreferenceManager
import com.sapuseven.untis.helpers.config.PreferenceUtils


class AutoMuteReceiver : BroadcastReceiver() {
	companion object {
		const val EXTRA_INT_ID = "com.sapuseven.untis.automute.id"
		const val EXTRA_BOOLEAN_MUTE = "com.sapuseven.untis.automute.mute"

		const val PREFERENCE_KEY_INTERRUPTION_FILTER = "interruption_filter"
		const val PREFERENCE_KEY_RINGER_MODE = "ringer_mode"
	}

	override fun onReceive(context: Context, intent: Intent) {
		Log.d("AutoMuteReceiver", "AutoMuteReceiver received, mute = ${intent.getBooleanExtra(EXTRA_BOOLEAN_MUTE, false)}")

		val preferenceManager = PreferenceManager(context)
		if (!PreferenceUtils.getPrefBool(preferenceManager, "preference_automute_enable")) return

		if (intent.hasExtra(EXTRA_BOOLEAN_MUTE)) {
			val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
			val editor = prefs.edit()

			if (intent.getBooleanExtra(EXTRA_BOOLEAN_MUTE, false)) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
					val interruptionFilter =
						if(PreferenceUtils.getPrefBool(preferenceManager, "preference_automute_mute_priority"))
							NotificationManager.INTERRUPTION_FILTER_NONE
						else
							NotificationManager.INTERRUPTION_FILTER_PRIORITY

					if (!prefs.contains(PREFERENCE_KEY_INTERRUPTION_FILTER)) {
						editor.putInt(PREFERENCE_KEY_INTERRUPTION_FILTER, notificationManager.currentInterruptionFilter)
						Log.d("AutoMuteReceiver", "Saved interruption filter: ${notificationManager.currentInterruptionFilter}")
					}
					notificationManager.setInterruptionFilter(interruptionFilter)
				} else {
					val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
					editor.putInt(PREFERENCE_KEY_RINGER_MODE, audioManager.ringerMode)
					audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
				}
			} else {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
					notificationManager.setInterruptionFilter(prefs.getInt(PREFERENCE_KEY_INTERRUPTION_FILTER, NotificationManager.INTERRUPTION_FILTER_ALL))
					editor.remove(PREFERENCE_KEY_INTERRUPTION_FILTER)
				} else {
					val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
					audioManager.ringerMode = prefs.getInt(PREFERENCE_KEY_RINGER_MODE, AudioManager.RINGER_MODE_NORMAL)
				}
			}

			editor.apply()
		}
	}
}
