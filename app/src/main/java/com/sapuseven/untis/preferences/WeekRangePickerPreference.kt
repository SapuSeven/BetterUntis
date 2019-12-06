package com.sapuseven.untis.preferences

import android.content.Context
import android.util.AttributeSet
import androidx.preference.DialogPreference
import ca.antonious.materialdaypicker.MaterialDayPicker
import com.sapuseven.untis.R
import com.sapuseven.untis.helpers.toLocalizedString
import java.util.*

class WeekRangePickerPreference(context: Context?, attrs: AttributeSet?) : DialogPreference(context, attrs) {
	override fun getSummary(): CharSequence = generateSummary()

	private fun generateSummary(): String {
		val selectedDays = getPersistedStringSet(emptySet()).toList().map { MaterialDayPicker.Weekday.valueOf(it) }
		val selectionBounds = MaterialDayPicker.Weekday.getOrderedDaysOfWeek(Locale.getDefault()).filter { selectedDays.contains(it) }.bounds()

		return selectionBounds?.first?.toLocalizedString()?.let { first ->
			selectionBounds.second?.toLocalizedString()?.let { second ->
				context.getString(R.string.preference_week_custom_range_summary, first, second)
			} ?: run {
				context.getString(R.string.preference_week_custom_range_summary_short, first)
			}
		} ?: ""
	}

	fun refreshSummary() {
		summary = generateSummary()
	}
}

private fun <E> List<E>.bounds(): Pair<E, E?>? = if (size >= 1)
	first() to if (size >= 2) last() else null
else null
