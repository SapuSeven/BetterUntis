package com.sapuseven.untis.preferences

import android.content.Context
import android.util.AttributeSet
import androidx.preference.EditTextPreference
import com.sapuseven.untis.R

class RangePreference(context: Context, attrs: AttributeSet?) : EditTextPreference(context, attrs) {
	companion object {
		fun convertToPair(text: String?): Pair<Int, Int>? = text?.split("-")?.map {
			it.toIntOrNull() ?: 0
		}?.toPair()
	}

	override fun getSummary(): CharSequence = convertToPair(parseInput(text))?.let {
		context.getString(R.string.preference_timetable_range_desc, it.first, it.second)
	} ?: ""

	private fun parseInput(value: String?): String? = value?.let { Regex("[^\\d]*(\\d+)[^\\d]*(\\d+)[^\\d]*").replace(it, "$1-$2") }

	override fun setText(text: String?) = super.setText(parseInput(text))

	override fun shouldDisableDependents(): Boolean = super.shouldDisableDependents() || (convertToPair(text)?.first
			?: 1) <= 1
}

private fun <E> List<E>.toPair(): Pair<E, E>? = if (this.size != 2) null else this.zipWithNext().first()
