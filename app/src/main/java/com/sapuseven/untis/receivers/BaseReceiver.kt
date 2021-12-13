package com.sapuseven.untis.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sapuseven.untis.helpers.config.PreferenceHelper

abstract class BaseReceiver : BroadcastReceiver() {
	protected lateinit var preferences: PreferenceHelper

	override fun onReceive(context: Context, intent: Intent) {
		preferences = PreferenceHelper(context)
		//preferences.loadProfile(profileId) TODO: Load profile id from intent extra
	}
}
