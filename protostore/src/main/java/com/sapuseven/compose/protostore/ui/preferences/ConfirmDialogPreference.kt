package com.sapuseven.compose.protostore.ui.preferences

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.google.protobuf.MessageLite
import com.sapuseven.compose.protostore.R
import kotlinx.coroutines.CoroutineScope

@Composable
fun ConfirmDialogPreference(
	title: (@Composable () -> Unit),
	summary: (@Composable () -> Unit)? = null,
	leadingContent: (@Composable () -> Unit)? = null,
	trailingContent: (@Composable (enabled: Boolean) -> Unit)? = null,
	dialogTitle: (@Composable () -> Unit) = title,
	dialogText: (@Composable () -> Unit)? = summary,
	onConfirm: () -> Unit = {},
	scope: CoroutineScope = rememberCoroutineScope(),
	enabledCondition: () -> Boolean = { true },
	highlight: Boolean = false,
) {
	var showDialog by remember { mutableStateOf(false) }

	Preference(
		title = title,
		summary = summary,
		leadingContent = leadingContent,
		trailingContent = trailingContent,
		scope = scope,
		enabledCondition = enabledCondition,
		highlight = highlight,
		onClick = {
			showDialog = true
		}
	)

	if (showDialog) {
		AlertDialog(
			onDismissRequest = {
				showDialog = false
			},
			title = dialogTitle,
			text = dialogText,
			confirmButton = {
				TextButton(onClick = {
					showDialog = false
					onConfirm()
				}) {
					Text(stringResource(id = R.string.dialog_ok))
				}
			},
			dismissButton = {
				TextButton(onClick = {
					showDialog = false
				}) {
					Text(stringResource(id = R.string.dialog_cancel))
				}
			}
		)
	}
}
