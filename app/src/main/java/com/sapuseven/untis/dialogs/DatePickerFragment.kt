package com.sapuseven.untis.dialogs

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import com.sapuseven.untis.R
import java.util.*

class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {
	var callback: (calendar: Calendar) -> Unit = {}

	companion object {
		const val DIALOG_THEME = "dialogTheme"
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		val dialog: DatePickerDialog
		val c = Calendar.getInstance()
		val year = c.get(Calendar.YEAR)
		val month = c.get(Calendar.MONTH)
		val day = c.get(Calendar.DAY_OF_MONTH)
		dialog = if (arguments?.containsKey(DIALOG_THEME) == true)
			DatePickerDialog(requireContext(), arguments!!.getInt(DIALOG_THEME), this, year, month, day)
		else
			DatePickerDialog(requireContext(), this, year, month, day)

		dialog.setButton(DatePickerDialog.BUTTON_NEUTRAL, getString(R.string.today)) { _, _ ->
			val calendar = Calendar.getInstance()
			onDateSet(DatePicker(context), calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
		}

		return dialog
	}

	override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
		val calendar = Calendar.getInstance()
		calendar.set(Calendar.YEAR, year)
		calendar.set(Calendar.MONTH, month)
		calendar.set(Calendar.DAY_OF_MONTH, day)
		callback(calendar)
	}
}
