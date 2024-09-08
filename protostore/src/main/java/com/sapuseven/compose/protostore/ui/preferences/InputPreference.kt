package com.sapuseven.compose.protostore.ui.preferences

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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

@OptIn(ExperimentalMaterial3Api::class)
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

		AlertDialog(
			onDismissRequest = { showDialog = false },
			title = title,
			text = {
				// TODO: Find a way to reduce the TextField padding
				TextField(
					value = input,
					onValueChange = { input = it },
					singleLine = true,
					keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
					colors = TextFieldDefaults.colors(
						focusedContainerColor = Color.Transparent,
						unfocusedContainerColor = Color.Transparent,
						disabledContainerColor = Color.Transparent,
						errorContainerColor = Color.Transparent,
					),
					modifier = Modifier.fillMaxWidth()
				)
			},
			confirmButton = {
				TextButton(
					onClick = {
						showDialog = false
						scope.launch {
							settingsRepository.updateUserSettings {
								onValueChange?.invoke(this, input)
							}
						}
					}) {
					Text(stringResource(id = R.string.dialog_ok))
				}
			},
			dismissButton = {
				TextButton(onClick = { showDialog = false }) {
					Text(stringResource(id = R.string.dialog_cancel))
				}
			}
		)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
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

		AlertDialog(
			onDismissRequest = { showDialog = false },
			title = title,
			text = {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					modifier = Modifier.fillMaxWidth()
				) {
					// TODO: Find a way to reduce the TextField padding
					TextField(
						value = input,
						onValueChange = { input = it },
						singleLine = true,
						keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
						colors = TextFieldDefaults.colors(
							focusedContainerColor = Color.Transparent,
							unfocusedContainerColor = Color.Transparent,
							disabledContainerColor = Color.Transparent,
							errorContainerColor = Color.Transparent,
						),
						modifier = Modifier.weight(1f)
					)

					unit?.let {
						Text(
							modifier = Modifier.padding(start = 8.dp),
							text = unit
						)
					}
				}
			},
			confirmButton = {
				TextButton(
					onClick = {
						showDialog = false
						scope.launch {
							settingsRepository.updateUserSettings {
								onValueChange?.invoke(this, input.toIntOrNull() ?: 0)
							}
						}
					}) {
					Text(stringResource(id = R.string.dialog_ok))
				}
			},
			dismissButton = {
				TextButton(onClick = { showDialog = false }) {
					Text(stringResource(id = R.string.dialog_cancel))
				}
			}
		)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
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

		AlertDialog(
			onDismissRequest = { showDialog = false },
			title = title,
			text = {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					modifier = Modifier.fillMaxWidth()
				) {
					TextField(
						value = first,
						onValueChange = { first = it },
						singleLine = true,
						keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
						colors = TextFieldDefaults.colors(
							focusedContainerColor = Color.Transparent,
							unfocusedContainerColor = Color.Transparent,
							disabledContainerColor = Color.Transparent,
							errorContainerColor = Color.Transparent,
						),
						label = { Text(text = stringResource(R.string.preference_range_from)) },
						modifier = Modifier
                            .padding(end = 12.dp)
                            .weight(1f)
					)

					TextField(
						value = second,
						onValueChange = { second = it },
						singleLine = true,
						keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
						colors = TextFieldDefaults.colors(
							focusedContainerColor = Color.Transparent,
							unfocusedContainerColor = Color.Transparent,
							disabledContainerColor = Color.Transparent,
							errorContainerColor = Color.Transparent,
						),
						label = { Text(text = stringResource(R.string.preference_range_to)) },
						modifier = Modifier
                            .padding(start = 12.dp)
                            .weight(1f)
					)
				}
			},
			confirmButton = {
				TextButton(
					onClick = {
						showDialog = false
						scope.launch {
							settingsRepository.updateUserSettings {
								onValueChange?.invoke(
									this,
									if (first.isNotBlank() && second.isNotBlank()) "$first-$second" else ""
								)
							}
						}
					}) {
					Text(stringResource(id = R.string.dialog_ok))
				}
			},
			dismissButton = {
				TextButton(onClick = { showDialog = false }) {
					Text(stringResource(id = R.string.dialog_cancel))
				}
			}
		)
	}
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
	this.split("-").map { it.toIntOrNull() ?: return null }.toPair()
