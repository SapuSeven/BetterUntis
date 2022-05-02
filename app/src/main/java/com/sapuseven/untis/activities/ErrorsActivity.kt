package com.sapuseven.untis.activities

import android.os.Bundle
import android.view.View.GONE
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet
import com.sapuseven.untis.R
import io.sentry.Sentry
import io.sentry.UserFeedback
import io.sentry.protocol.SentryId
import kotlinx.android.synthetic.main.activity_errors.*


class ErrorsActivity : BaseActivity() {
	companion object {
		const val EXTRA_BOOLEAN_SHOW_CRASH_MESSAGE = "com.sapuseven.activities.errors.crashmessage"
		const val EXTRA_BOOLEAN_SENTRY_ID = "com.sapuseven.activities.errors.sentryid"
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContentView(R.layout.activity_errors)

		findViewById<TextView>(R.id.textview_crash_id).text =
			getString(R.string.errors_crash_id, intent.getStringExtra(EXTRA_BOOLEAN_SENTRY_ID))

		if (!intent.getBooleanExtra(EXTRA_BOOLEAN_SHOW_CRASH_MESSAGE, false)) {
			textview_crash_title.visibility = GONE
			textview_crash_message.visibility = GONE
			with(ConstraintSet()) {
				clone(constraintlayout_root)
				connect(
					textview_errors_report.id, ConstraintSet.TOP,
					constraintlayout_root.id, ConstraintSet.TOP
				)
				applyTo(constraintlayout_root)
			}
		}

		button_dismiss.setOnClickListener {
			finish()
		}

		button_report.setOnClickListener {
			sendUserFeedback()
			finish()
		}
	}

	private fun sendUserFeedback() {
		val userFeedback = UserFeedback(
			intent.getStringExtra(EXTRA_BOOLEAN_SENTRY_ID)?.let { SentryId(it) }
				?: SentryId.EMPTY_ID,
			edittext_crash_name.text.toString(),
			edittext_crash_email.text.toString(),
			edittext_crash_feedback.text.toString()
		)
		Sentry.captureUserFeedback(userFeedback)
	}
}
