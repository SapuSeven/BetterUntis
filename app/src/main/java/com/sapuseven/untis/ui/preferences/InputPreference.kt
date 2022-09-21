package com.sapuseven.untis.ui.preferences

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sapuseven.untis.R
import com.sapuseven.untis.preferences.UntisPreferenceDataStore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputPreference(
	title: (@Composable () -> Unit),
	icon: (@Composable () -> Unit)? = null,
	dependency: UntisPreferenceDataStore<*>? = null,
	dataStore: UntisPreferenceDataStore<String>
) {
	val value = remember { mutableStateOf(dataStore.defaultValue) }
	var showDialog by remember { mutableStateOf(false) }

	val scope = rememberCoroutineScope()

	Preference(
		title = title,
		summary = if (value.value.isNotBlank()) {
			{ Text(value.value) }
		} else null,
		icon = icon,
		dependency = dependency,
		dataStore = dataStore,
		value = value,
		onClick = { showDialog = true }
	)

	if (showDialog) {
		var input by remember { mutableStateOf(value.value) }

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
					colors = TextFieldDefaults.textFieldColors(
						containerColor = Color.Transparent
					),
					modifier = Modifier.fillMaxWidth()
				)
			},
			confirmButton = {
				TextButton(
					onClick = {
						showDialog = false
						scope.launch { dataStore.saveValue(input) }
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumericInputPreference(
	title: (@Composable () -> Unit),
	icon: (@Composable () -> Unit)? = null,
	unit: String? = null,
	dependency: UntisPreferenceDataStore<*>? = null,
	dataStore: UntisPreferenceDataStore<Int>
) {
	val value = remember { mutableStateOf(dataStore.defaultValue) }
	var showDialog by remember { mutableStateOf(false) }

	val scope = rememberCoroutineScope()

	Preference(
		title = title,
		summary = { Text(unit?.let { "${value.value} $unit" } ?: value.value.toString()) },
		icon = icon,
		dependency = dependency,
		dataStore = dataStore,
		value = value,
		onClick = { showDialog = true }
	)

	if (showDialog) {
		var input by remember { mutableStateOf(value.value.toString()) }

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
						colors = TextFieldDefaults.textFieldColors(
							containerColor = Color.Transparent
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
						scope.launch { dataStore.saveValue(input.toIntOrNull() ?: 0) }
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RangeInputPreference(
	title: (@Composable () -> Unit),
	icon: (@Composable () -> Unit)? = null,
	dependency: UntisPreferenceDataStore<*>? = null,
	dataStore: UntisPreferenceDataStore<String>
) {
	val value = remember { mutableStateOf(dataStore.defaultValue) }
	var showDialog by remember { mutableStateOf(false) }

	val scope = rememberCoroutineScope()

	Preference(
		title = title,
		summary = value.value.convertRangeToPair()?.let {
			{
				Text(stringResource(R.string.preference_timetable_range_desc, it.first, it.second))
			}
		},
		icon = icon,
		dependency = dependency,
		dataStore = dataStore,
		value = value,
		onClick = { showDialog = true }
	)

	if (showDialog) {
		val input = value.value.convertRangeToPair()
		var first by remember { mutableStateOf(input?.first?.toString() ?: "") }
		var second by remember { mutableStateOf(input?.second?.toString() ?: "") }

		AlertDialog(
			onDismissRequest = { showDialog = false },
			title = title,
			text = {
				// TODO: Extract string resources
				Row(
					verticalAlignment = Alignment.CenterVertically,
					modifier = Modifier.fillMaxWidth()
				) {
					TextField(
						value = first,
						onValueChange = { first = it },
						singleLine = true,
						keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
						colors = TextFieldDefaults.textFieldColors(
							containerColor = Color.Transparent
						),
						label = { Text(text = "from") },
						modifier = Modifier
							.padding(end = 12.dp)
							.weight(1f)
					)

					TextField(
						value = second,
						onValueChange = { second = it },
						singleLine = true,
						keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
						colors = TextFieldDefaults.textFieldColors(
							containerColor = Color.Transparent
						),
						label = { Text(text = "to") },
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
						scope.launch { dataStore.saveValue(if (first.isNotBlank() && second.isNotBlank()) "$first-$second" else "") }
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
}

private fun <E> List<E>.toPair(): Pair<E, E>? =
	if (this.size != 2) null else this.zipWithNext().first()

fun String.convertRangeToPair(): Pair<Int, Int>? =
	this.split("-").map { it.toIntOrNull() ?: return null }.toPair()
