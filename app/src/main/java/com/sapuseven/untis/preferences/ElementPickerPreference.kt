package com.sapuseven.untis.preferences

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.models.untis.timetable.PeriodElement

class ElementPickerPreference(context: Context, attrs: AttributeSet) : Preference(context, attrs) {
	companion object {
		const val KEY_SUFFIX_ID = "_id"
		const val KEY_SUFFIX_TYPE = "_type"
	}

	override fun onAttached() {
		summary = sharedPreferences.getString(key, "")
	}

	fun setElement(element: PeriodElement?, displayName: String) {
		summary = displayName

		val editor = sharedPreferences.edit()
		element?.let {
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

	fun getSavedType() = sharedPreferences.getString(key + KEY_SUFFIX_TYPE, null)
			?: TimetableDatabaseInterface.Type.CLASS.toString()
}
