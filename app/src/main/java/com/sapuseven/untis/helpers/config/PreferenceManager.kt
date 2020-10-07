package com.sapuseven.untis.helpers.config

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.ContextCompat
import java.io.File


class PreferenceManager(val context: Context, private var profileId: Long = 0) {
	lateinit var defaultPrefs: SharedPreferences

	init {
		reload(profileId)
	}

	fun reload(profileId: Long = 0) {
		this.profileId = if (profileId > 0L) profileId else androidx.preference.PreferenceManager.getDefaultSharedPreferences(context).getLong("profile", 0L)
		defaultPrefs = prefsForProfile(this.profileId)
	}

	fun prefsForProfile(profileId: Long): SharedPreferences {
		return context.getSharedPreferences("preferences_${profileId}", Context.MODE_PRIVATE)
	}

	fun saveProfileId(profileId: Long) {
		reload(profileId)

		val editor = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context).edit()
		editor.putLong("profile", profileId)
		editor.apply()
	}

	fun currentProfileId() = profileId

	fun deleteProfile(profileId: Long) {
		deleteSharedPreferences("preferences_${profileId}")

		if (this.profileId == profileId) reload(-1)
	}

	private fun deleteSharedPreferences(name: String): Boolean {
		getSharedPreferencesPath(name).apply {
			delete()
			return !exists()
		}
	}

	private fun getSharedPreferencesPath(name: String): File = File(getPreferencesDir(), "$name.xml")

	private fun getPreferencesDir(): File = File(ContextCompat.getDataDir(context), "shared_prefs")
}
