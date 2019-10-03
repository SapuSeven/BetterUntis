package com.sapuseven.untis.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.sapuseven.untis.R
import org.joda.time.DateTime

class DatePickerDialog : DialogFragment() {
	var dateSetListener: android.app.DatePickerDialog.OnDateSetListener? = null

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		val dialog: android.app.DatePickerDialog = arguments?.let {
			val year = it.getInt("year")
			val month = it.getInt("month")
			val day = it.getInt("day")
			android.app.DatePickerDialog(context!!, dateSetListener, year, month - 1, day)
		} ?: run {
			val now = DateTime.now()
			val year = now.year
			val month = now.monthOfYear
			val day = now.dayOfMonth
			android.app.DatePickerDialog(context!!, dateSetListener, year, month, day)
		}
		dialog.setButton(android.app.DatePickerDialog.BUTTON_NEUTRAL, getString(R.string.today)) { _, _ ->
			val dateTime = DateTime.now()
			dateSetListener?.onDateSet(dialog.datePicker, dateTime.year, dateTime.monthOfYear, dateTime.dayOfMonth)
		}
		return dialog
	}
}
