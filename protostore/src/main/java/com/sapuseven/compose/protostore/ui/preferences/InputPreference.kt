package com.sapuseven.compose.protostore.ui.preferences

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.protobuf.MessageLite
import com.sapuseven.compose.protostore.R
import com.sapuseven.compose.protostore.data.SettingsRepository
import com.sapuseven.compose.protostore.ui.utils.disabled
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun <Model : MessageLite, ModelBuilder : MessageLite.Builder> InputPreference(
	title: (@Composable () -> Unit),
	summary: (@Composable () -> Unit)? = null,
	supportingContent: @Composable ((value: String, enabled: Boolean) -> Unit)? = { currentValue, enabled ->
		DefaultSupportingContent(
			currentValue = currentValue,
			enabled = enabled
		)
	},
	leadingContent: (@Composable () -> Unit)? = null,
	trailingContent: (@Composable (value: String, enabled: Boolean) -> Unit)? = null,
	settingsRepository: SettingsRepository<Model, ModelBuilder>,
	value: (Model) -> String,
	scope: CoroutineScope = rememberCoroutineScope(),
	enabledCondition: (Model) -> Boolean = { true },
	highlight: Boolean = false,
	onValueChange: (ModelBuilder.(checked: String) -> Unit)? = null,
) {
	var dialogValue by remember { mutableStateOf("") }
	var showDialog by remember { mutableStateOf(false) }

	Preference(
		title = title,
		summary = summary,
		supportingContent = { currentValue, enabled ->
			if (currentValue.isNotEmpty()) {
				supportingContent?.invoke(currentValue, enabled)
			}
		},
		leadingContent = leadingContent,
		trailingContent = trailingContent,
		settingsRepository = settingsRepository,
		value = value,
		scope = scope,
		enabledCondition = enabledCondition,
		highlight = highlight,
		onClick = {
			dialogValue = it
			showDialog = true
		}
	)

	if (showDialog) {
		var input by remember { mutableStateOf(dialogValue) }

		PreferenceDialog(
			title = title,
			onConfirm = {
				showDialog = false
				scope.launch {
					settingsRepository.updateSettings {
						onValueChange?.invoke(this, input)
					}
				}
			},
			onDismiss = {
				showDialog = false
			}
		) {
			PreferenceDialogTextField(
				value = input,
				onValueChange = { input = it },
				modifier = Modifier.fillMaxWidth()
			)
		}
	}
}

@Composable
fun <Model : MessageLite, ModelBuilder : MessageLite.Builder> NumericInputPreference(
	title: (@Composable () -> Unit),
	summary: (@Composable () -> Unit)? = null,
	supportingContent: @Composable ((value: String, enabled: Boolean) -> Unit)? = { currentValue, enabled ->
		DefaultSupportingContent(
			currentValue = currentValue,
			enabled = enabled
		)
	},
	leadingContent: (@Composable () -> Unit)? = null,
	trailingContent: (@Composable (value: Int, enabled: Boolean) -> Unit)? = null,
	settingsRepository: SettingsRepository<Model, ModelBuilder>,
	value: (Model) -> Int,
	unit: String? = null,
	scope: CoroutineScope = rememberCoroutineScope(),
	enabledCondition: (Model) -> Boolean = { true },
	highlight: Boolean = false,
	onValueChange: (ModelBuilder.(checked: Int) -> Unit)? = null,
) {
	var dialogValue by remember { mutableIntStateOf(0) }
	var showDialog by remember { mutableStateOf(false) }

	Preference(
		title = title,
		summary = summary,
		supportingContent = { currentValue, enabled ->
			supportingContent?.invoke(unit?.let { "$currentValue $unit" }
				?: currentValue.toString(), enabled)
		},
		leadingContent = leadingContent,
		trailingContent = trailingContent,
		settingsRepository = settingsRepository,
		value = value,
		scope = scope,
		enabledCondition = enabledCondition,
		highlight = highlight,
		onClick = {
			dialogValue = it
			showDialog = true
		}
	)

	if (showDialog) {
		var input by remember { mutableStateOf(dialogValue.toString()) }

		PreferenceDialog(
			title = title,
			onConfirm = {
				showDialog = false
				scope.launch {
					settingsRepository.updateSettings {
						onValueChange?.invoke(this, input.toIntOrNull() ?: 0)
					}
				}
			},
			onDismiss = {
				showDialog = false
			}
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier.fillMaxWidth()
			) {
				PreferenceDialogTextField(
					value = input,
					onValueChange = { input = it },
					modifier = Modifier.weight(1f),
					keyboardType = KeyboardType.Number
				)

				unit?.let {
					Text(
						modifier = Modifier.padding(start = 8.dp),
						text = unit
					)
				}
			}
		}
	}
}

@Composable
fun <Model : MessageLite, ModelBuilder : MessageLite.Builder> RangeInputPreference(
	title: (@Composable () -> Unit),
	summary: (@Composable () -> Unit)? = null,
	supportingContent: @Composable ((value: String, enabled: Boolean) -> Unit)? = { currentValue, enabled ->
		DefaultSupportingContent(
			currentValue = currentValue,
			enabled = enabled
		)
	},
	leadingContent: (@Composable () -> Unit)? = null,
	trailingContent: (@Composable (value: String, enabled: Boolean) -> Unit)? = null,
	settingsRepository: SettingsRepository<Model, ModelBuilder>,
	value: (Model) -> String,
	scope: CoroutineScope = rememberCoroutineScope(),
	enabledCondition: (Model) -> Boolean = { true },
	highlight: Boolean = false,
	onValueChange: (ModelBuilder.(checked: String) -> Unit)? = null,
) {
	var dialogValue by remember { mutableStateOf<Pair<Int, Int>?>(null) }
	var showDialog by remember { mutableStateOf(false) }

	Preference(
		title = title,
		summary = summary,
		supportingContent = { currentValue, enabled ->
			if (currentValue.isNotEmpty()) {
				supportingContent?.invoke(currentValue, enabled)
			}
		},
		leadingContent = leadingContent,
		trailingContent = trailingContent,
		settingsRepository = settingsRepository,
		value = value,
		scope = scope,
		enabledCondition = enabledCondition,
		highlight = highlight,
		onClick = {
			dialogValue = it.convertRangeToPair()
			showDialog = true
		}
	)

	if (showDialog) {
		val input = dialogValue
		var first by remember { mutableStateOf(input?.first?.toString() ?: "") }
		var second by remember { mutableStateOf(input?.second?.toString() ?: "") }

		PreferenceDialog(
			title = title,
			onConfirm = {
				showDialog = false
				scope.launch {
					settingsRepository.updateSettings {
						onValueChange?.invoke(
							this,
							if (first.isNotBlank() && second.isNotBlank()) "$first-$second" else ""
						)
					}
				}
			},
			onDismiss = {
				showDialog = false
			}
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier.fillMaxWidth()
			) {
				PreferenceDialogTextField(
					value = first,
					onValueChange = { first = it },
					modifier = Modifier
						.padding(end = 12.dp)
						.weight(1f),
					label = stringResource(R.string.preference_range_from),
					keyboardType = KeyboardType.Number
				)

				PreferenceDialogTextField(
					value = second,
					onValueChange = { second = it },
					modifier = Modifier
						.padding(start = 12.dp)
						.weight(1f),
					label = stringResource(R.string.preference_range_to),
					keyboardType = KeyboardType.Number
				)
			}
		}
	}
}

@Composable
private fun PreferenceDialog(
	title: @Composable () -> Unit,
	confirmButtonText: String = stringResource(id = R.string.dialog_ok),
	dismissButtonText: String = stringResource(id = R.string.dialog_cancel),
	onConfirm: () -> Unit,
	onDismiss: () -> Unit,
	content: @Composable () -> Unit
) {
	AlertDialog(
		onDismissRequest = { onDismiss() },
		title = title,
		text = content,
		confirmButton = {
			TextButton(onClick = onConfirm) {
				Text(confirmButtonText)
			}
		},
		dismissButton = {
			TextButton(onClick = onDismiss) {
				Text(dismissButtonText)
			}
		}
	)
}

@Composable
private fun PreferenceDialogTextField(
	value: String,
	onValueChange: (String) -> Unit,
	modifier: Modifier = Modifier,
	label: String? = null,
	keyboardType: KeyboardType = KeyboardType.Text
) {
	// TODO: Find a way to reduce the TextField padding
	TextField(
		value = value,
		onValueChange = onValueChange,
		singleLine = true,
		keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
		colors = TextFieldDefaults.colors(
			focusedContainerColor = Color.Transparent,
			unfocusedContainerColor = Color.Transparent,
			disabledContainerColor = Color.Transparent,
			errorContainerColor = Color.Transparent,
		),
		label = label?.let { { Text(text = it) } },
		modifier = modifier
	)
}

@Composable
fun DefaultSupportingContent(
	currentValue: String,
	enabled: Boolean
) {
	Text(text = currentValue, modifier = Modifier.disabled(!enabled))
}

private fun <E> List<E>.toPair(): Pair<E, E>? =
	if (this.size != 2) null else this.zipWithNext().first()

fun String.convertRangeToPair(): Pair<Int, Int>? =
	this.split("-").mapNotNull { it.toIntOrNull() }.toPair()
