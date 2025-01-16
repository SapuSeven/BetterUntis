package com.sapuseven.untis.ui.preferences

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.text.style.TextOverflow
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
	multiSelect: Boolean = false,
	highlight: Boolean = false,
) {
	val value = remember { mutableStateOf(dataStore.defaultValue) }
	var showDialog by remember { mutableStateOf(false) }

	val scope = rememberCoroutineScope()

	@Composable
	fun generateSummary(elements: List<PeriodElement>): String = elements.joinToString { element ->
		timetableDatabaseInterface.getShortName(element)
	}

	@Composable
	fun summary(value: String) {
		decodeStoredTimetableValue(value)?.let {
			if (it.isNotEmpty()) {
				Text(generateSummary(it), overflow = TextOverflow.Ellipsis, maxLines = 1)
			}
		}
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
					scope.launch {
						dataStore.saveValue(element?.let { encodeStoredTimetableValue(listOf(it)) } ?: "")
					}
					showDialog = false
				}
			},
			multiSelect = multiSelect,
			onMultiSelect = { elements ->
				scope.launch { dataStore.saveValue(encodeStoredTimetableValue(elements)) }
			},
			initialSelection = decodeStoredTimetableValue(value.value),
			hideTypeSelection = type != null,
			initialType = type ?:
					decodeStoredTimetableValue(value.value)
					?.firstOrNull()
					?.let { TimetableDatabaseInterface.Type.valueOf(it.type)},
		)
}

fun encodeStoredTimetableValue(values: List<PeriodElement>): String =
	SerializationUtils.getJSON().encodeToString(values)

fun decodeStoredTimetableValue(values: String): List<PeriodElement>? = try {
	SerializationUtils.getJSON().decodeFromString(values)
} catch (e: Throwable) {
	// Backwards-compatibility when the value isn't stored as a list yet
	try {
	    listOf(SerializationUtils.getJSON().decodeFromString(values))
	} catch (e: Throwable) {
		null
	}
}
