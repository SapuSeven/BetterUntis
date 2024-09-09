package com.sapuseven.compose.protostore.ui.preferences

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.google.protobuf.MessageLite
import com.sapuseven.compose.protostore.R
import com.sapuseven.compose.protostore.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun <Model : MessageLite, ModelBuilder : MessageLite.Builder> ListPreference(
	title: (@Composable () -> Unit),
	summary: (@Composable () -> Unit)? = null,
	//summary: @Composable ((selected: Pair<String, String>) -> Unit)? = null,
	supportingContent: @Composable ((value: Pair<String, String>, enabled: Boolean) -> Unit)? = null,
	leadingContent: (@Composable () -> Unit)? = null,
	trailingContent: (@Composable (value: String, enabled: Boolean) -> Unit)? = null,
	settingsRepository: SettingsRepository<Model, ModelBuilder>,
	value: (Model) -> String,
	scope: CoroutineScope = rememberCoroutineScope(),
	enabledCondition: (Model) -> Boolean = { true },
	highlight: Boolean = false,
	entries: Array<String>, // Compatibility with legacy array resources
	entryLabels: Array<String>, // Compatibility with legacy array resources
	onValueChange: (ModelBuilder.(value: String) -> Unit)? = null,
) {
	var dialogValue by remember { mutableStateOf("") }
	var showDialog by remember { mutableStateOf(false) }

	val entryLabelMap = remember { mapOf(pairs = entries.zip(entryLabels).toTypedArray()) }

	Preference(
		title = title,
		summary = summary,
		supportingContent = { currentValue, enabled ->
			if (currentValue.isNotEmpty()) {
				val selected = currentValue to entryLabelMap.getOrElse(currentValue) { currentValue }
				supportingContent?.invoke(selected, enabled)
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

	if (showDialog)
		AlertDialog(
			onDismissRequest = { showDialog = false },
			title = title,
			text = {
				LazyColumn(modifier = Modifier.fillMaxWidth()) {
					items(entries.toList()) {
						Row(
							modifier = Modifier
								.fillMaxWidth()
								.selectableGroup()
								.clickable(
									role = Role.RadioButton
								) {
									showDialog = false
									scope.launch {
										settingsRepository.updateSettings {
											onValueChange?.invoke(this, it)
										}
									}
								},
							verticalAlignment = Alignment.CenterVertically
						) {
							RadioButton(
								selected = dialogValue == it,
								onClick = {
									showDialog = false
									scope.launch {
										settingsRepository.updateSettings {
											onValueChange?.invoke(this, it)
										}
									}
								}
							)

							Text(
								modifier = Modifier.weight(1f),
								text = entryLabelMap.getOrElse(it) { it }
							)
						}
					}
				}
			},
			confirmButton = {
				TextButton(
					onClick = {
						showDialog = false
					}) {
					Text(stringResource(id = R.string.dialog_cancel))
				}
			}
		)
}

@Composable
fun <Model : MessageLite, ModelBuilder : MessageLite.Builder> MultiSelectListPreference(
	title: (@Composable () -> Unit),
	summary: (@Composable () -> Unit)? = null,
	//summary: @Composable ((selected: Pair<String, String>) -> Unit)? = null,
	supportingContent: @Composable ((value: Set<String>, enabled: Boolean) -> Unit)? = null,
	leadingContent: (@Composable () -> Unit)? = null,
	trailingContent: (@Composable (value: Set<String>, enabled: Boolean) -> Unit)? = null,
	settingsRepository: SettingsRepository<Model, ModelBuilder>,
	value: (Model) -> Set<String>,
	scope: CoroutineScope = rememberCoroutineScope(),
	enabledCondition: (Model) -> Boolean = { true },
	highlight: Boolean = false,
	entries: Array<String>, // Compatibility with legacy array resources
	entryLabels: Array<String>, // Compatibility with legacy array resources
	onValueChange: (ModelBuilder.(value: Set<String>) -> Unit)? = null,
) {
	var showDialog by remember { mutableStateOf(false) }
	val dialogValues = remember { mutableStateMapOf<String, Boolean>() }

	val entryLabelMap = remember { mapOf(pairs = entries.zip(entryLabels).toTypedArray()) }

	Preference(
		title = title,
		summary = summary,
		supportingContent = supportingContent,
		leadingContent = leadingContent,
		trailingContent = trailingContent,
		settingsRepository = settingsRepository,
		value = value,
		scope = scope,
		enabledCondition = enabledCondition,
		highlight = highlight,
		onClick = {
			dialogValues.apply {
				entries.forEach { entry ->
					this[entry] = it.contains(entry)
				}
			}
			showDialog = true
		}
	)

	if (showDialog)
		AlertDialog(
			onDismissRequest = { showDialog = false },
			title = title,
			text = {
				LazyColumn(modifier = Modifier.fillMaxWidth()) {
					items(dialogValues.keys.toList()) {
						Row(
							modifier = Modifier
								.fillMaxWidth()
								.clickable(
									role = Role.Checkbox
								) {
									dialogValues[it] = !dialogValues.getOrElse(it) { false }
								},
							verticalAlignment = Alignment.CenterVertically
						) {
							Checkbox(
								checked = dialogValues.getOrElse(it) { false },
								onCheckedChange = { newValue ->
									dialogValues[it] = newValue
								}
							)

							Text(
								modifier = Modifier.weight(1f),
								text = entryLabelMap.getOrElse(it) { it }
							)
						}
					}
				}
			},
			confirmButton = {
				TextButton(
					onClick = {
						showDialog = false
						scope.launch {
							settingsRepository.updateSettings {
								onValueChange?.invoke(this, dialogValues.filter { it.value }.keys)
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
