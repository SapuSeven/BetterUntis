package com.sapuseven.untis.helpers.config

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.ContextCompat
import java.io.File


class PreferenceHelper(val context: Context) {
	var sharedPrefs: SharedPreferences? = null

	fun loadProfile(profileId: Long) {
		sharedPrefs = if (profileId == 0L) null else loadPrefsForProfile(context, profileId)
	}

	fun loadSavedProfile() {
		loadProfile(loadProfileId())
	}

	fun loadPrefsForProfile(context: Context, profileId: Long): SharedPreferences {
		return context.getSharedPreferences("preferences_${profileId}", Context.MODE_PRIVATE)
	}

	fun loadProfileId(): Long =
			androidx.preference.PreferenceManager.getDefaultSharedPreferences(context).getLong("profile", 0L)

	fun saveProfileId(profileId: Long) =
			androidx.preference.PreferenceManager.getDefaultSharedPreferences(context).edit()
					.putLong("profile", profileId).apply()

	fun deleteProfileId() =
			androidx.preference.PreferenceManager.getDefaultSharedPreferences(context).edit()
					.remove("profile").apply()

	fun deleteProfile(profileId: Long) {
		sharedPrefs?.apply { edit().clear().apply() }
		deleteSharedPreferencesFile("preferences_${profileId}")

		loadProfile(0)
	}

	private fun deleteSharedPreferencesFile(name: String): Boolean {
		getSharedPreferencesPath(name).apply {
			delete()
			return !exists()
		}
	}

	private fun getSharedPreferencesPath(name: String): File = File(getPreferencesDir(), "$name.xml")

	private fun getPreferencesDir(): File = File(ContextCompat.getDataDir(context), "shared_prefs")

	private inline fun edit(operation: (SharedPreferences.Editor) -> Unit) {
		val editor = sharedPrefs!!.edit()
		operation(editor)
		editor.apply()
	}

	inline operator fun <reified T : Any> get(key: String): T {
		val res = context.resources

		return when (T::class) {
			String::class -> res.getString(res.getIdentifier(key + "_default", "string", context.packageName)).let { default ->
				(sharedPrefs?.getString(key, default) ?: default) as T
			}
			Int::class -> res.getInteger(res.getIdentifier(key + "_default", "integer", context.packageName)).let { default ->
				(sharedPrefs?.getInt(key, default) ?: default) as T
			}
			Boolean::class -> res.getBoolean(res.getIdentifier(key + "_default", "bool", context.packageName)).let { default ->
				(sharedPrefs?.getBoolean(key, default) ?: default) as T
			}
			else -> throw UnsupportedOperationException("Not yet implemented")
		}
	}

	inline operator fun <reified T : Any> get(key: String, defaultValue: T?): T = when (T::class) {
		String::class -> (sharedPrefs?.getString(key, defaultValue as? String ?: "") ?: defaultValue) as T
		Int::class -> (sharedPrefs?.getInt(key, defaultValue as? Int ?: -1) ?: defaultValue) as T
		Boolean::class -> (sharedPrefs?.getBoolean(key, defaultValue as? Boolean ?: false) ?: defaultValue) as T
		else -> throw UnsupportedOperationException("Not yet implemented")
	}

	operator fun set(key: String, value: Any?) = when (value) {
		is String? -> edit { it.putString(key, value) }
		is Int -> edit { it.putInt(key, value) }
		is Boolean -> edit { it.putBoolean(key, value) }
		else -> throw UnsupportedOperationException("Not yet implemented")
	}

	fun has(key: String): Boolean = sharedPrefs!!.contains(key)

	/*init {
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

	private fun getPreferencesDir(): File = File(ContextCompat.getDataDir(context), "shared_prefs")*/
}
