package com.sapuseven.untis.helpers.config

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(val context: Context) {
	var profileId: Long = 0

	init {
		reload()
	}

	fun reload() {
		profileId = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context).getLong("profile", 0L)
	}

	fun saveProfileId(profileId: Long) {
		val editor = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context).edit()
		editor.putLong("profile", profileId)
		editor.apply()
	}

	val defaultPrefs: SharedPreferences = context.getSharedPreferences("preferences_$profileId", Context.MODE_PRIVATE)
}
