package com.sapuseven.untis.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.sapuseven.untis.R
import com.sapuseven.untis.data.timetable.PeriodData
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
private val json = Json {
	encodeDefaults = true
	prettyPrint = true
	prettyPrintIndent = "  "
}

@Composable
internal fun DebugInfoAction(
	title: @Composable (() -> Unit)? = null,
	content: @Composable (() -> Unit)? = null,
) {
	var showInfoDialog by remember { mutableStateOf(false) }

	IconButton(onClick = { showInfoDialog = true }) {
		Icon(
			painterResource(R.drawable.all_debug),
			contentDescription = "Debug info"
		)
	}

	if (showInfoDialog) {
		AlertDialog(
			onDismissRequest = { showInfoDialog = false },
			title = title,
			text = content,
			confirmButton = {
				TextButton(
					onClick = { showInfoDialog = false }) {
					Text(stringResource(R.string.all_ok))
				}
			}
		)
	}
}

@Composable
fun DebugDesclaimerAction() {
	DebugInfoAction(
		title = { Text("Debug information") }
	) {
		Text(
			"You are running a debug build of the app.\n\n" +
					"This means that the app is not optimized and you will see some additional settings and functions.\n" +
					"It is only recommended to use this variant when developing or gathering information about specific issues.\n" +
					"For normal daily use, you should switch to a stable release build of the app.\n\n" +
					"Please remember that diagnostic data may include personal details, " +
					"so it is your responsibility to check and obfuscate any gathered data before uploading."
		)
	}
}

@Composable
fun DebugTimetableItemDetailsAction(timegridItems: List<PeriodData>) {
	DebugInfoAction(
		title = { Text("Raw lesson details") }
	) {
		LazyColumn(
			verticalArrangement = Arrangement.spacedBy(16.dp),
			modifier = Modifier
				.fillMaxWidth()
		) {
			items(timegridItems) {
				Column(
					horizontalAlignment = Alignment.End,
					modifier = Modifier
						.clip(RoundedCornerShape(8.dp))
						.background(MaterialTheme.colorScheme.background)
						.padding(8.dp)
				) {
					RawText(item = it)
				}
			}
		}
	}
}

@Composable
private inline fun <reified T> RawText(item: T) {
	val clipboardManager: ClipboardManager = LocalClipboardManager.current
	val itemText = remember { json.encodeToString(item) }

	Text(
		color = MaterialTheme.colorScheme.onSurface,
		fontFamily = FontFamily.Monospace,
		text = itemText
	)
	TextButton(
		onClick = { clipboardManager.setText(AnnotatedString(itemText)) }
	) {
		Icon(
			painter = painterResource(R.drawable.all_copy),
			contentDescription = "Copy",
			modifier = Modifier
				.padding(end = 8.dp)
		)
		Text("Copy")
	}
}
