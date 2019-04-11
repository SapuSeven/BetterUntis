package com.sapuseven.untis.preferences

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.preference.DialogPreference
import androidx.preference.Preference
import com.sapuseven.untis.R
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface

class ElementPickerPreference(context: Context, attrs: AttributeSet) : DialogPreference(context, attrs) {
	private var tvPersonal: TextView? = null
	private var tvClasses: TextView? = null
	private var tvTeachers: TextView? = null
	private var tvRooms: TextView? = null

	private var selectedType: TimetableDatabaseInterface.Type? = null
	private var selectedName: String? = null
	private var selectedId = -1
	private var selectedPosition = -1

	private var defaultTextColor: ColorStateList? = null
	private var defaultItemTextColor: ColorStateList? = null
	private var elemList: ViewGroup? = null
	private var dialog: AlertDialog? = null
	private val DIALOG_FRAGMENT_TAG = "ElementPickerPreference"


	override fun getDialogLayoutResource(): Int {
		return R.layout.dialog_element_picker
	}

	/*val summary: CharSequence?
		get() {
			if (selectedName == null) {
				val sharedPrefs = sharedPreferences
				selectedType = TimetableDatabaseInterface.Type.valueOf(sharedPrefs.getString(key, null) ?: "")
				selectedId = sharedPrefs.getInt(key + "_id", -1)
				selectedName = sharedPrefs.getString(key + "_name", "")
			}

			return if (selectedType != null)
				selectedName
			else
				""
		}

	init {
		dialogLayoutResource = R.layout.dialog_element_picker
	}

	protected fun onDialogClosed(positiveResult: Boolean) {
		super.onDialogClosed(positiveResult)

		if (positiveResult) {
			saveSelected()
			if (selectedType != UNKNOWN)
				setSummary(selectedName)
			else
				setSummary("")
		}
	}

	private fun saveSelected() {
		val editor = getEditor()
		editor.putInt(key, selectedType.value)
		editor.putInt(key + "_id", selectedId)
		editor.putString(key + "_name", selectedName)
		editor.commit()
	}*/
}
