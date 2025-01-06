package com.sapuseven.untis.ui.dialogs

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.applyCanvas
import com.sapuseven.untis.BuildConfig
import com.sapuseven.untis.R
import com.sapuseven.untis.ui.common.LabeledCheckbox
import com.sapuseven.untis.ui.functional.None
import com.sapuseven.untis.ui.functional.bottomInsets
import io.sentry.Attachment
import io.sentry.Sentry
import io.sentry.UserFeedback
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackDialog(
	visible: Boolean,
	onDismiss: () -> Unit
) {
	val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
	val scope = rememberCoroutineScope()
	val context = LocalContext.current
	val view = LocalView.current

	var feedbackText by rememberSaveable { mutableStateOf("") }
	var screenshotState by rememberSaveable { mutableStateOf(false) }

	fun captureScreenshot() = ByteArrayOutputStream().use {
		val bitmap =
			Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888).applyCanvas {
				view.draw(this)
			}
		bitmap.compress(Bitmap.CompressFormat.PNG, 0, it)
		it.toByteArray()
	}

	fun sendFeedback(includeScreenshot: Boolean) = scope.launch {
		if (BuildConfig.DEBUG) {
			Toast
				.makeText(context, "Feedback not sent (developer build).", Toast.LENGTH_LONG)
				.show()
		} else {
			val screenshot = if (includeScreenshot) captureScreenshot() else null

			val sentryId = Sentry.captureMessage("WeekView Feedback") { scope ->
				screenshot?.let {
					scope.addAttachment(Attachment.fromScreenshot(it))
				}
			}
			Sentry.captureUserFeedback(UserFeedback(sentryId).apply {
				comments = feedbackText
			})
			Toast
				.makeText(context, "Feedback sent. Thanks!", Toast.LENGTH_LONG)
				.show()
		}

		feedbackText = ""
		screenshotState = false
		onDismiss()
	}

	LaunchedEffect(visible) {
		if (visible) {
			sheetState.show()
		} else {
			sheetState.hide()
		}
	}

	if (visible)
		ModalBottomSheet(
			onDismissRequest = onDismiss,
			sheetState = sheetState
		) {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.verticalScroll(rememberScrollState())
			) {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.SpaceBetween,
					modifier = Modifier
						.fillMaxWidth()
						.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
				) {
					Text(
						text = "Feedback",
						style = MaterialTheme.typography.headlineLarge
					)

					Icon(
						painter = painterResource(id = R.drawable.all_feedback),
						modifier = Modifier
							.size(72.dp)
							.padding(start = 16.dp, end = 8.dp),
						contentDescription = null
					)
				}

				Text(
					text = "Please use this form to send feedback about the timetable display.",
					modifier = Modifier
						.padding(horizontal = 16.dp)
				)

				OutlinedTextField(
					value = feedbackText,
					onValueChange = { feedbackText = it },
					label = { Text("Your feedback") },
					minLines = 4,
					maxLines = 12,
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = 16.dp, vertical = 16.dp)
				)

				LabeledCheckbox(
					label = { Text(text = "Include screenshot") },
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = 16.dp),
					checked = screenshotState,
					onCheckedChange = { screenshotState = it }
				)

				Row(
					horizontalArrangement = Arrangement.End,
					modifier = Modifier
						.fillMaxWidth()
						.padding(16.dp)
						.bottomInsets()

				) {
					Button(
						enabled = feedbackText.isNotBlank(),
						onClick = { sendFeedback(screenshotState) }
					) {
						Text(text = "Send")
					}
				}
			}
		}
}
