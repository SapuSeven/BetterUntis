package com.sapuseven.untis.preferences.preference

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import com.sapuseven.untis.helpers.SerializationUtils
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.models.untis.timetable.PeriodElement
import com.sapuseven.untis.preferences.UntisPreferenceDataStore
import com.sapuseven.untis.ui.common.ElementPickerDialog
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

@Composable
fun ElementPickerPreference(
	title: (@Composable () -> Unit),
	icon: (@Composable () -> Unit)? = null,
	dependency: UntisPreferenceDataStore<*>? = null,
	dataStore: UntisPreferenceDataStore<String>,
	timetableDatabaseInterface: TimetableDatabaseInterface
) {
	fun encodeValue(value: PeriodElement): String =
		SerializationUtils.getJSON().encodeToString(value)

	fun decodeValue(value: String): PeriodElement? = try {
		SerializationUtils.getJSON().decodeFromString(value)
	} catch (e: Throwable) {
		null
	}

	val value = remember { mutableStateOf(dataStore.defaultValue) }
	var showDialog by remember { mutableStateOf(false) }

	val scope = rememberCoroutineScope()

	@Composable
	fun generateSummary(element: PeriodElement): String {
		return timetableDatabaseInterface.getShortName(element)
	}

	Preference(
		title = title,
		summary = decodeValue(value.value)?.let {
			{ Text(generateSummary(it)) }
		},
		icon = icon,
		value = value,
		dependency = dependency,
		dataStore = dataStore,
		onClick = {
			showDialog = true
		},
	)

	if (showDialog)
		ElementPickerDialog(
			title = title,
			timetableDatabaseInterface = timetableDatabaseInterface,
			onDismiss = {
				showDialog = false
			},
			onSelect = { element ->
				scope.launch { dataStore.saveValue(element?.let { encodeValue(it) } ?: "") }
				showDialog = false
			},
			initialType = decodeValue(value.value)?.let { TimetableDatabaseInterface.Type.valueOf(it.type) }
		)
}

