package com.sapuseven.untis.helpers.config

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

class PreferenceManager(val context: Context) {
	val defaultPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
}