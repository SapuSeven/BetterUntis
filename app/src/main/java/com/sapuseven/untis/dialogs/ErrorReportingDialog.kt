package com.sapuseven.untis.dialogs

import android.content.Context
import com.sapuseven.untis.R
import com.sapuseven.untis.helpers.issues.GithubIssue
import com.sapuseven.untis.helpers.issues.Issue

class ErrorReportingDialog(val context: Context) {
	fun showRequestErrorDialog(requestId: Int, code: Int?, message: String?) {
		val errorMessage = "Request ID: $requestId\nError code: $code\nError message: $message"
		// TODO: Localize
		val dialog = android.app.AlertDialog.Builder(context)
				.setTitle("Error Information")
				.setMessage(errorMessage)
				.setPositiveButton(R.string.all_ok) { dialog, _ ->
					dialog.dismiss()
				}
				.setNeutralButton(R.string.all_report) { _, _ ->
					GithubIssue(Issue.Type.CRASH, errorMessage).launch(context)
				}
				.create()

		dialog.show()
	}
}