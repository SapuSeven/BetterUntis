package com.sapuseven.untis.ui.preferences

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import com.sapuseven.untis.helpers.SerializationUtils
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.models.untis.timetable.PeriodElement
import com.sapuseven.untis.preferences.UntisPreferenceDataStore
import com.sapuseven.untis.ui.dialogs.ElementPickerDialog
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
	type: TimetableDatabaseInterface.Type? = null,
	highlight: Boolean = false,
	multiSelect: Boolean = false,
	showSearch: Boolean = false
) {
	val value = remember { mutableStateOf(dataStore.defaultValue) }
	var showDialog by remember { mutableStateOf(false) }

	val scope = rememberCoroutineScope()

	@Composable
	fun generateSummary(element: PeriodElement): String {
		return timetableDatabaseInterface.getShortName(element)
	}

	@Composable
	fun generateSummary(elements: List<PeriodElement>): String {
		var subjects = ""
		for (element in elements) {
			subjects += "${timetableDatabaseInterface.getShortName(element)}, "
		}
		subjects = subjects.dropLast(2)
		return subjects
	}

	@Composable
	fun summary(value: String) {
		Text(text = if (multiSelect) {
			decodeMultipleStoredTimetableValues(value)?.let { generateSummary(elements = it) } ?: ""
		} else {
			decodeStoredTimetableValue(value)?.let { generateSummary(element = it) } ?: ""
		}
		)
	}

	Preference(
		title = title,
		summary = { summary(value = value.value) },
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
				if (!multiSelect) {
					scope.launch { dataStore.saveValue(element?.let { encodeStoredTimetableValue(it) } ?: "") }
					showDialog = false
				}
			},
			multiSelect = multiSelect,
			onMultiSelect = { elements ->
				scope.launch {
					dataStore.saveValue(encodeMultipleStoredTimeTableValues(elements))
				}
			},
			selectedItems = if (multiSelect) {
				decodeMultipleStoredTimetableValues(value.value)
			} else {
				null
			},
			showSearch = showSearch,
			hideTypeSelection = type != null,
			initialType = type ?: decodeStoredTimetableValue(value.value)?.let { TimetableDatabaseInterface.Type.valueOf(it.type) }
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

fun decodeMultipleStoredTimetableValues(values: String): List<PeriodElement>? = try {
	SerializationUtils.getJSON().decodeFromString(values)
} catch (e: Throwable) {
	null
}
