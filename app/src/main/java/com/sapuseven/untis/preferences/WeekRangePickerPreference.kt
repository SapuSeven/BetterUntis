package com.sapuseven.untis.preferences

import android.content.Context
import android.util.AttributeSet
import androidx.preference.DialogPreference
import ca.antonious.materialdaypicker.MaterialDayPicker
import com.sapuseven.untis.R
import java.text.SimpleDateFormat
import java.util.*

class WeekRangePickerPreference(context: Context?, attrs: AttributeSet?) : DialogPreference(context, attrs) {
	override fun getSummary(): CharSequence {
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
}

private fun MaterialDayPicker.Weekday.toLocalizedString(): String =
		SimpleDateFormat("EEEE", Locale.getDefault()).format(Calendar.getInstance().apply {
			set(Calendar.DAY_OF_WEEK, toCalendar())
		}.time)

private fun MaterialDayPicker.Weekday.toCalendar(): Int = when (this) {
	MaterialDayPicker.Weekday.SUNDAY -> Calendar.SUNDAY
	MaterialDayPicker.Weekday.MONDAY -> Calendar.MONDAY
	MaterialDayPicker.Weekday.TUESDAY -> Calendar.TUESDAY
	MaterialDayPicker.Weekday.WEDNESDAY -> Calendar.WEDNESDAY
	MaterialDayPicker.Weekday.THURSDAY -> Calendar.THURSDAY
	MaterialDayPicker.Weekday.FRIDAY -> Calendar.FRIDAY
	MaterialDayPicker.Weekday.SATURDAY -> Calendar.SATURDAY
}

private fun <E> List<E>.bounds(): Pair<E, E?>? = if (size >= 1)
	first() to if (size >= 2) last() else null
else null
