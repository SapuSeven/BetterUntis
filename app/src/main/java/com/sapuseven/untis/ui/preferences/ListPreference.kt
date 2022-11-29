package com.sapuseven.untis.ui.preferences

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
import com.sapuseven.untis.R
import com.sapuseven.untis.preferences.UntisPreferenceDataStore
import kotlinx.coroutines.launch

@Composable
fun ListPreference(
	title: @Composable () -> Unit,
	summary: @Composable ((selected: Pair<String, String>) -> Unit)? = null,
	icon: @Composable (() -> Unit)? = null,
	dependency: UntisPreferenceDataStore<*>? = null,
	dataStore: UntisPreferenceDataStore<String>,
	entries: Array<String>, // Compatibility with legacy array resources
	entryLabels: Array<String> // Compatibility with legacy array resources
) {
	val value = remember { mutableStateOf(dataStore.defaultValue) }
	var showDialog by remember { mutableStateOf(false) }
	val entryLabelMap = remember { mapOf(pairs = entries.zip(entryLabels).toTypedArray()) }

	val scope = rememberCoroutineScope()

	Preference(
		title = title,
		summary = if (summary != null) {
			{
				val selected = value.value to entryLabelMap.getOrDefault(value.value, value.value)
				summary(selected)
			}
		} else null,
		icon = icon,
		dependency = dependency,
		dataStore = dataStore,
		value = value,
		onClick = { showDialog = true }
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
									scope.launch { dataStore.saveValue(it) }
								},
							verticalAlignment = Alignment.CenterVertically
						) {
							RadioButton(
								selected = value.value == it,
								onClick = {
									showDialog = false
									scope.launch { dataStore.saveValue(it) }
								}
							)

							Text(
								modifier = Modifier.weight(1f),
								text = entryLabelMap.getOrDefault(it, it)
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
					Text(stringResource(id = R.string.all_cancel))
				}
			}
		)
}

@Composable
fun MultiSelectListPreference(
	title: @Composable () -> Unit,
	summary: @Composable (() -> Unit)? = null,
	icon: @Composable (() -> Unit)? = null,
	dependency: UntisPreferenceDataStore<*>? = null,
	dataStore: UntisPreferenceDataStore<Set<String>>,
	entries: Array<String>, // Compatibility with legacy array resources
	entryLabels: Array<String> // Compatibility with legacy array resources
) {
	val value = remember { mutableStateOf(dataStore.defaultValue) }
	var showDialog by remember { mutableStateOf(false) }
	val dialogItems = remember { mutableStateMapOf<String, Boolean>() }
	val entryLabelMap = remember { mapOf(pairs = entries.zip(entryLabels).toTypedArray()) }

	val scope = rememberCoroutineScope()

	Preference(
		title = title,
		summary = summary,
		icon = icon,
		dependency = dependency,
		dataStore = dataStore,
		value = value,
		onClick = {
			dialogItems.apply {
				entries.forEach { entry ->
					this[entry] = value.value.contains(entry)
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
					items(dialogItems.keys.toList()) {
						Row(
							modifier = Modifier
								.fillMaxWidth()
								.clickable(
									role = Role.Checkbox
								) {
									dialogItems[it] = !dialogItems.getOrDefault(it, false)
								},
							verticalAlignment = Alignment.CenterVertically
						) {
							Checkbox(
								checked = dialogItems.getOrDefault(it, false),
								onCheckedChange = { newValue ->
									dialogItems[it] = newValue
								}
							)

							Text(
								modifier = Modifier.weight(1f),
								text = entryLabelMap.getOrDefault(it, it)
							)
						}
					}
				}
			},
			confirmButton = {
				TextButton(
					onClick = {
						showDialog = false
						scope.launch { dataStore.saveValue(dialogItems.filter { it.value }.keys) }
					}) {
					Text(stringResource(id = R.string.all_ok))
				}
			},
			dismissButton = {
				TextButton(onClick = { showDialog = false }) {
					Text(stringResource(id = R.string.all_cancel))
				}
			}
		)
}
