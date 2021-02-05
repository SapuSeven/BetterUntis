package com.sapuseven.untis.preferences

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.models.untis.timetable.PeriodElement
import kotlin.text.StringBuilder

class SubjectPickerPreference(context: Context, attrs: AttributeSet) : Preference(context, attrs) {

	override fun onAttached() {
		summary = sharedPreferences.getString(key, "")
	}

	fun setElement(elements: List<PeriodElement>, names: String) {
		val editor = sharedPreferences.edit()
		if (elements.isEmpty()) {
			editor.remove(key + "_ids")
			editor.remove(key)
		} else {
			var iterator = elements.iterator()
			var stringBuilder = StringBuilder()
			stringBuilder.append(iterator.next().id)
			while (iterator.hasNext()) stringBuilder.append(",").append(iterator.next().id)
			editor.putString(key + "_ids", stringBuilder.toString())
			editor.putString(key, names)
			editor.apply()
		}
	}
}
