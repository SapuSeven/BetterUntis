package com.sapuseven.untis.preferences

import android.content.Context
import android.content.SharedPreferences
import android.util.AttributeSet
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import com.sapuseven.untis.models.untis.timetable.PeriodElement


class ElementPickerPreference(context: Context, attrs: AttributeSet) : Preference(context, attrs) {
	private lateinit var prefs: SharedPreferences

	companion object {
		const val KEY_SUFFIX_ID = "_id"
		const val KEY_SUFFIX_TYPE = "_type"
	}

	override fun onAttached() {
		prefs = PreferenceManager.getDefaultSharedPreferences(context)

		summary = prefs.getString(key, "")
	}

	fun setElement(element: PeriodElement?, displayName: String) {
		val editor = prefs.edit()
		element?.let {
			summary = element.toString()

			editor.apply {
				putString(key, displayName)
				putInt(key + KEY_SUFFIX_ID, element.id)
				putString(key + KEY_SUFFIX_TYPE, element.type)
			}
		} ?: run {
			editor.apply {
				remove(key)
				remove(key + KEY_SUFFIX_ID)
				remove(key + KEY_SUFFIX_TYPE)
			}
		}
		editor.apply()
	}
}
