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
			android.app.DatePickerDialog(requireContext(), dateSetListener, year, month - 1, day)
		} ?: run {
			val now = DateTime.now()
			android.app.DatePickerDialog(requireContext(), dateSetListener, now.year, now.monthOfYear - 1, now.dayOfMonth)
		}
		dialog.setButton(android.app.DatePickerDialog.BUTTON_NEUTRAL, getString(R.string.all_dialog_datepicker_button_today)) { _, _ ->
			val dateTime = DateTime.now()
			dateSetListener?.onDateSet(dialog.datePicker, dateTime.year, dateTime.monthOfYear - 1, dateTime.dayOfMonth)
		}
		return dialog
	}
}
