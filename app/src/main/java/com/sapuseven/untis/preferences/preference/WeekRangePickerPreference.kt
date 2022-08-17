package com.sapuseven.untis.preferences.preference

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.sapuseven.untis.R
import com.sapuseven.untis.helpers.toLocalizedString
import com.sapuseven.untis.preferences.UntisPreferenceDataStore
import com.sapuseven.untis.ui.common.SelectionState
import com.sapuseven.untis.ui.common.WeekRangePicker
import com.sapuseven.untis.ui.common.Weekday
import com.sapuseven.untis.ui.common.bounds
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun WeekRangePickerPreference(
	title: (@Composable () -> Unit),
	icon: (@Composable () -> Unit)? = null,
	dependency: UntisPreferenceDataStore<*>? = null,
	dataStore: UntisPreferenceDataStore<Set<String>>
) {
	val value = remember { mutableStateOf(dataStore.defaultValue) }
	var dialogValue by remember { mutableStateOf(dataStore.defaultValue) }
	var showDialog by remember { mutableStateOf(false) }

	val scope = rememberCoroutineScope()

	@Composable
	fun generateSummary(value: Set<String>): String {
		val selectedDays = value.toList().map { Weekday.valueOf(it) }
		val selectionBounds = Weekday.getOrderedDaysOfWeek(Locale.getDefault())
			.filter { selectedDays.contains(it) }.bounds()

		return selectionBounds?.first?.toLocalizedString()?.let { first ->
			selectionBounds.second?.toLocalizedString()?.let { second ->
				stringResource(R.string.preference_week_custom_range_summary, first, second)
			} ?: run {
				stringResource(R.string.preference_week_custom_range_summary_short, first)
			}
		} ?: ""
	}

	Preference(
		title = title,
		summary = if (value.value.isNotEmpty()) {
			{ Text(generateSummary(value.value)) }
		} else null,
		icon = icon,
		value = value,
		dependency = dependency,
		dataStore = dataStore,
		onClick = {
			dialogValue = value.value
			showDialog = true
		},
	)

	if (showDialog)
		AlertDialog(
			onDismissRequest = { showDialog = false },
			title = {
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.SpaceBetween,
					verticalAlignment = Alignment.CenterVertically
				) {
					title()

					IconButton(onClick = { dialogValue = emptySet() }) {
						Icon(
							imageVector = Icons.Outlined.Refresh,
							contentDescription = stringResource(
								id = R.string.all_reset
							)
						)
					}
				}
			},
			text = {
				WeekRangePicker(
					value = SelectionState(dialogValue.map { Weekday.valueOf(it) }),
					onValueChange = { newValue ->
						dialogValue = newValue.selectedDays.map { it.name }.toSet()
					}
				)
			},
			confirmButton = {
				TextButton(
					onClick = {
						showDialog = false
						scope.launch { dataStore.saveValue(dialogValue) }
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

