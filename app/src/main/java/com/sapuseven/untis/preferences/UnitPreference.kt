package com.sapuseven.untis.preferences

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.preference.EditTextPreference
import com.sapuseven.untis.R

// TODO: Change the input EditText to only accept numbers
class UnitPreference : EditTextPreference {
	private var unit: String? = ""

	constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
		init(attrs)
	}

	constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
		init(attrs)
	}

	private fun init(attrs: AttributeSet) {
		with(context.obtainStyledAttributes(attrs, R.styleable.UnitPreference)) {
			unit = this.getString(R.styleable.UnitPreference_unit)
			this.recycle()
		}
	}

	override fun getPersistedString(defaultReturnValue: String?): String {
		return if (sharedPreferences.contains(key)) {
			val intValue = getPersistedInt(0)
			intValue.toString()
		} else {
			defaultReturnValue ?: ""
		}
	}

	override fun persistString(value: String): Boolean {
		val intValue: Int
		try {
			intValue = Integer.valueOf(value)
		} catch (e: NumberFormatException) {
			// TODO: Log error
			summary = "Invalid value" // TODO: Localize
			return false
		}

		summary = value + unit
		return persistInt(intValue)
	}

	override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
		return Integer.decode(a.getString(index)!!).toString()
	}
}
