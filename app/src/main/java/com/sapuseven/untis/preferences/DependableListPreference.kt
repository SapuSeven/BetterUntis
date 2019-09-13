package com.sapuseven.untis.preferences

import android.content.Context
import android.util.AttributeSet
import androidx.preference.ListPreference
import com.sapuseven.untis.R


class DependentListPreference constructor(context: Context, attrs: AttributeSet? = null) : ListPreference(context, attrs) {
	private var disabledDependentValue: String? = ""

	init {
		if (attrs != null) {
			val a = context.obtainStyledAttributes(attrs, R.styleable.DependentListPreference)
			disabledDependentValue = a.getString(R.styleable.DependentListPreference_disabledDependentValue)
			a.recycle()
		}
	}

	override fun setValue(value: String) {
		val oldValue = getValue()
		super.setValue(value)
		if (value != oldValue)
			notifyDependencyChange(shouldDisableDependents())
	}

	override fun shouldDisableDependents(): Boolean {
		return value == null || value == disabledDependentValue
	}
}