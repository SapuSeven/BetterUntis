package com.sapuseven.untis.dialogs

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sapuseven.untis.R
import com.sapuseven.untis.helpers.issues.GithubIssue
import com.sapuseven.untis.helpers.issues.Issue

class ErrorReportingDialog(val context: Context) {
	fun showRequestErrorDialog(requestId: Int, code: Int?, message: String?) {
		val errorMessage = context.getString(R.string.all_dialog_error_message, requestId, code, message)
		MaterialAlertDialogBuilder(context)
				.setTitle(R.string.all_dialog_error_title)
				.setMessage(errorMessage)
				.setPositiveButton(R.string.all_ok) { dialog, _ ->
					dialog.dismiss()
				}
				.setNeutralButton(R.string.all_report) { _, _ ->
					GithubIssue(Issue.Type.EXCEPTION, errorMessage).launch(context)
				}
				.show()
	}

	fun showGenericErrorDialog(message: String) {
		MaterialAlertDialogBuilder(context)
				.setTitle(R.string.all_dialog_error_title)
				.setMessage(message)
				.setPositiveButton(R.string.all_ok) { dialog, _ ->
					dialog.dismiss()
				}
				.setNeutralButton(R.string.all_report) { _, _ ->
					GithubIssue(Issue.Type.EXCEPTION, message).launch(context)
				}
				.show()
	}
}
