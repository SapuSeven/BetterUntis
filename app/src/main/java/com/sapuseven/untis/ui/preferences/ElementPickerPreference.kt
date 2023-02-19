package com.sapuseven.untis.ui.preferences

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.text.style.TextOverflow
import com.sapuseven.untis.helpers.SerializationUtils
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.models.untis.timetable.PeriodElement
import com.sapuseven.untis.preferences.UntisPreferenceDataStore
import com.sapuseven.untis.ui.dialogs.ElementPickerDialog
import com.sapuseven.untis.ui.dialogs.ElementPickerDialogFullscreen
import com.sapuseven.untis.ui.dialogs.SubjectsElementDialog
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

@Composable
fun ElementPickerPreference(
	title: (@Composable () -> Unit),
	icon: (@Composable () -> Unit)? = null,
	dependency: UntisPreferenceDataStore<*>? = null,
	dataStore: UntisPreferenceDataStore<String>,
	timetableDatabaseInterface: TimetableDatabaseInterface,
	highlight: Boolean = false
) {
	val value = remember { mutableStateOf(dataStore.defaultValue) }
	var showDialog by remember { mutableStateOf(false) }

	val scope = rememberCoroutineScope()

	@Composable
	fun generateSummary(element: PeriodElement): String {
		return timetableDatabaseInterface.getShortName(element)
	}

	Preference(
		title = title,
		summary = decodeStoredTimetableValue(value.value)?.let {
			{ Text(generateSummary(it)) }
		},
		icon = icon,
		value = value,
		dependency = dependency,
		dataStore = dataStore,
		onClick = {
			showDialog = true
		},
		highlight = highlight
	)

	if (showDialog)
		ElementPickerDialog(
			title = title,
			timetableDatabaseInterface = timetableDatabaseInterface,
			onDismiss = {
				showDialog = false
			},
			onSelect = { element ->
				scope.launch { dataStore.saveValue(element?.let { encodeStoredTimetableValue(it) } ?: "") }
				showDialog = false
			},
			initialType = decodeStoredTimetableValue(value.value)?.let { TimetableDatabaseInterface.Type.valueOf(it.type) }
		)
}

@Composable
fun SubjectElementPickerPreference(
	title: (@Composable () -> Unit),
	icon: (@Composable () -> Unit)? = null,
	dependency: UntisPreferenceDataStore<*>? = null,
	dataStore: UntisPreferenceDataStore<String>,
	timetableDatabaseInterface: TimetableDatabaseInterface,
	highlight: Boolean = false
) {
	val value = remember { mutableStateOf(dataStore.defaultValue) }
	var showDialog by remember { mutableStateOf(false) }

	val scope = rememberCoroutineScope()

	@Composable
	fun generateSummary(elements: List<PeriodElement>): String {
		var subjects = ""
		for (element in elements) {
			subjects += "${timetableDatabaseInterface.getShortName(element)}, "
		}
		subjects = subjects.dropLast(2)
		return subjects
	}

	Preference(
		title = title,
		summary = {
			decodeMultipleStoredTimetableValues(value.value)?.let {
				if (it.isNotEmpty()) {
					 Text(generateSummary(it), overflow = TextOverflow.Ellipsis, maxLines = 1)
				}
			}
		},
		icon = icon,
		value = value,
		dependency = dependency,
		dataStore = dataStore,
		onClick = {
			showDialog = true
		},
		highlight = highlight
	)

	if (showDialog)
		SubjectsElementDialog(
			title = title,
			timetableDatabaseInterface = timetableDatabaseInterface,
			onDismiss = {
				showDialog = false
			},
			onMultiSelect = { elements ->
				scope.launch { dataStore.saveValue(encodeMultipleStoredTimeTableValues(elements)) }
				showDialog = false
			},
			selectedElements = decodeMultipleStoredTimetableValues(value.value)
		)
}

fun encodeStoredTimetableValue(value: PeriodElement): String =
	SerializationUtils.getJSON().encodeToString(value)

fun encodeMultipleStoredTimeTableValues(values: List<PeriodElement>): String =
	SerializationUtils.getJSON().encodeToString(values)

fun decodeStoredTimetableValue(value: String): PeriodElement? = try {
	SerializationUtils.getJSON().decodeFromString(value)
} catch (e: Throwable) {
	null
}

fun decodeMultipleStoredTimetableValues(values: String) : List<PeriodElement>? = try {
	SerializationUtils.getJSON().decodeFromString(values)
} catch (e: Throwable) {
	null
}
