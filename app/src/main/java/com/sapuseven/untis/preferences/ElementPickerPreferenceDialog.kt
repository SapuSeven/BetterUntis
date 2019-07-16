package com.sapuseven.untis.preferences

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceDialogFragmentCompat
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface

// Only dialog handling here (frontend)
class ElementPickerPreferenceDialog : PreferenceDialogFragmentCompat() {
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

	companion object {
		fun newInstance(key: String): ElementPickerPreferenceDialog {
			val fragment = ElementPickerPreferenceDialog()
			val bundle = Bundle(1)
			bundle.putString(ARG_KEY, key)
			fragment.arguments = bundle
			return fragment
		}
	}

	// TODO: No idea how this works, the code below is from past experiments
	/*init {
		dialogLayoutResource = R.layout.dialog_element_picker
	}

	protected fun showDialog(state: Bundle) {
		super.showDialog(state)

		dialog = getDialog() as AlertDialog
	}

	protected fun onBindDialogView(view: View) {
		super.onBindDialogView(view)

		tvTeachers = view.findViewById(R.id.tvTeachers)
		tvPersonal = view.findViewById(R.id.tvPersonal)
		tvClasses = view.findViewById(R.id.tvClasses)
		tvRooms = view.findViewById(R.id.tvRooms)

		defaultTextColor = tvPersonal!!.textColors

		tvPersonal!!.setOnClickListener({ v ->
			if (selectedType != Type.UNKNOWN) {
				selectedType = Type.UNKNOWN
				updateSelection()
			}
		})

		tvClasses!!.setOnClickListener({ v ->
			if (selectedType != Type.CLASS) {
				selectedType = Type.CLASS
				updateSelection()
			}
		})

		tvTeachers!!.setOnClickListener({ v ->
			if (selectedType != Type.TEACHER) {
				selectedType = Type.TEACHER
				updateSelection()
			}
		})

		tvRooms!!.setOnClickListener({ v ->
			if (selectedType != Type.ROOM) {
				selectedType = Type.ROOM
				updateSelection()
			}
		})

		elemList = view.findViewById(R.id.elemList)

		helper = object : ElementSelectionListHelper(context as Activity) {
			fun onItemSelected(position: Int) {
				super.onItemSelected(position)

				val elementName = ElementName(selectedType, getUserDataList())
				val list = getList()
				try {
					selectedName = list.get(position)
					selectedId = elementName.findFieldByValue("name", list.get(position), "id")

					val oldPosition = selectedPosition
					selectedPosition = position

					updateView(oldPosition)
					updateView(selectedPosition)
				} catch (e: JSONException) {
					e.printStackTrace() // Not expected to occur, but TODO: Handle this error anyways
				}

			}

			fun applyStyling(view: View, position: Int) {
				super.applyStyling(view, position)

				refreshStyling(view, position)
			}
		}

		val sharedPrefs = sharedPreferences
		selectedType = fromValue(sharedPrefs.getInt(key, UNKNOWN.value))
		selectedId = sharedPrefs.getInt(key + "_id", -1)
		selectedName = sharedPrefs.getString(key + "_name", "")

		updateSelection()

		selectedPosition = helper!!.getList().indexOf(selectedName)
		updateView(selectedPosition)

		setPositiveButtonText(R.string.ok)
		setNegativeButtonText(R.string.cancel)
	}

	private fun checkIfValid() {
		if (dialog != null)
			dialog!!.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = selectedType == Type.UNKNOWN || selectedPosition >= 0
	}*/

	override fun onDialogClosed(positiveResult: Boolean) {
		/*if (positiveResult) {
			/*saveSelected()
			if (selectedType != UNKNOWN)
				setSummary(selectedName)
			else
				setSummary("")*/
		}*/
	}
}
