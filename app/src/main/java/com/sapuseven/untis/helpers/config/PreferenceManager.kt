package com.sapuseven.untis.helpers.config

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(val context: Context, private var profileId: Long = 0) {
	lateinit var defaultPrefs: SharedPreferences

	init {
		reload(profileId)
	}

	fun reload(profileId: Long) {
		this.profileId = if (profileId > 0L) profileId else androidx.preference.PreferenceManager.getDefaultSharedPreferences(context).getLong("profile", 0L)
		defaultPrefs = context.getSharedPreferences("preferences_${this.profileId}", Context.MODE_PRIVATE)
	}

	fun saveProfileId(profileId: Long) {
		reload(profileId)

		val editor = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context).edit()
		editor.putLong("profile", profileId)
		editor.apply()
	}

	fun currentProfileId() = profileId
}
