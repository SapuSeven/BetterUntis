package com.sapuseven.untis.preferences

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import androidx.preference.EditTextPreference
import com.sapuseven.untis.R

class UnitPreference(context: Context, attrs: AttributeSet?) : EditTextPreference(context, attrs) {
	private var unit: String? = ""

	init {
		with(context.obtainStyledAttributes(attrs, R.styleable.UnitPreference)) {
			unit = this.getString(R.styleable.UnitPreference_unit)
			this.recycle()
		}

		setOnBindEditTextListener { editText ->
			editText.inputType = InputType.TYPE_CLASS_NUMBER
			editText.selectAll()
		}
	}

	override fun getPersistedString(defaultReturnValue: String?) = getPersistedInt(defaultReturnValue?.toIntOrNull()
			?: 0).toString()

	override fun persistString(value: String): Boolean {
		val result = value.toIntOrNull()?.let { persistInt(it) } ?: false
		notifyChanged()
		return result
	}

	override fun getSummary() = text + unit
}
