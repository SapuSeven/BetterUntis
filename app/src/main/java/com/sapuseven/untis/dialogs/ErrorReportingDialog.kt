package com.sapuseven.untis.dialogs

import android.content.Context
import com.sapuseven.untis.R

class ErrorReportingDialog(val context: Context) {
	fun showRequestErrorDialog(requestId: Int, code: Int?, message: String?) {
		// TODO: Localize
		val dialog = android.app.AlertDialog.Builder(context)
				.setTitle("Error Information")
				.setMessage("Request ID: $requestId\nError code: $code\nError message: $message")
				.setPositiveButton(R.string.all_ok) { dialog, _ ->
					dialog.dismiss()
				}
				.create()

		// TODO: Add button to create a GitHub issue
		dialog.show()
	}
}