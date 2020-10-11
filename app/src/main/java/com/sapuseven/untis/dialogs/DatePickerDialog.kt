package com.sapuseven.untis.dialogs

import com.google.android.material.datepicker.MaterialDatePicker

class DatePickerDialog() {

	internal fun createFragment(): MaterialDatePicker<Long> {
		return  MaterialDatePicker.Builder.datePicker().setSelection(MaterialDatePicker.todayInUtcMilliseconds()).build()
	}

	/*	TODO: Add this button
		dialog.setButton(android.app.DatePickerDialog.BUTTON_NEUTRAL, getString(R.string.all_dialog_datepicker_button_today)) { _, _ ->
			val dateTime = DateTime.now()
			dateSetListener?.onDateSet(dialog.datePicker, dateTime.year, dateTime.monthOfYear - 1, dateTime.dayOfMonth)
		}
	}*/
}
