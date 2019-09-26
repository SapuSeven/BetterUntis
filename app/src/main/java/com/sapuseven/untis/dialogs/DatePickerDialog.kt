package com.sapuseven.untis.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.sapuseven.untis.R
import java.util.*

class DatePickerDialog : DialogFragment() {
	var dateSetListener: android.app.DatePickerDialog.OnDateSetListener? = null

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		val dialog: android.app.DatePickerDialog = arguments?.let {
			val year = it.getInt("year")
			val month = it.getInt("month")
			val day = it.getInt("day")
			android.app.DatePickerDialog(context!!, dateSetListener, year, month - 1, day)
		} ?: run {
			val c = Calendar.getInstance()
			val year = c.get(Calendar.YEAR)
			val month = c.get(Calendar.MONTH)
			val day = c.get(Calendar.DAY_OF_MONTH)
			android.app.DatePickerDialog(context!!, dateSetListener, year, month, day)
		}
		dialog.setButton(android.app.DatePickerDialog.BUTTON_NEUTRAL, getString(R.string.today)) { _, _ ->
			val calendar = Calendar.getInstance()
			dateSetListener?.onDateSet(dialog.datePicker, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
		}
		return dialog
	}
}
