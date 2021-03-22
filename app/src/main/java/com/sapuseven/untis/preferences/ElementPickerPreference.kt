package com.sapuseven.untis.preferences

import android.content.Context
import android.content.SharedPreferences
import android.util.AttributeSet
import androidx.preference.Preference
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.models.untis.timetable.PeriodElement
import java.lang.StringBuilder

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

	fun setElement(elements: List<PeriodElement?>, type: TimetableDatabaseInterface.Type, display: String) {

		var stringBuilder = if (elements.isEmpty()) null else StringBuilder()
		if (!elements.isEmpty()) {
			var elementIterator = elements.iterator()
			stringBuilder?.append(elementIterator.next()?.id)
			while (elementIterator.hasNext()) stringBuilder?.append(",")?.append(elementIterator.next()?.id)
			summary = display
		} else {
			summary = null
		}

		val editor = sharedPreferences.edit()
		stringBuilder.let {
			editor.apply {
				putString(key, display)
				putString(key + KEY_SUFFIX_ID, stringBuilder.toString())
				putString(key + KEY_SUFFIX_TYPE, type.name)
			}
		} ?: run {
			editor.apply {
				remove(key)
				remove(key + KEY_SUFFIX_ID)
				remove(key + KEY_SUFFIX_TYPE)
			}
		}
		editor.apply()
		println(stringBuilder.toString())
	}

	fun getSavedType() = sharedPreferences.getString(key + KEY_SUFFIX_TYPE, null)
			?: TimetableDatabaseInterface.Type.CLASS.toString()
}
