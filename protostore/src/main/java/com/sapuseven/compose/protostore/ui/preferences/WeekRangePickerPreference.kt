package com.sapuseven.compose.protostore.ui.preferences

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
import com.google.protobuf.MessageLite
import com.sapuseven.compose.protostore.R
import com.sapuseven.compose.protostore.data.SettingsRepository
import com.sapuseven.compose.protostore.ui.utils.SelectionState
import com.sapuseven.compose.protostore.ui.utils.WeekRangePicker
import com.sapuseven.compose.protostore.ui.utils.Weekday
import com.sapuseven.compose.protostore.ui.utils.bounds
import com.sapuseven.compose.protostore.ui.utils.toLocalizedString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun <Model : MessageLite, ModelBuilder : MessageLite.Builder> WeekRangePickerPreference(
	title: (@Composable () -> Unit),
	summary: (@Composable () -> Unit)? = null,
	//supportingContent: @Composable ((value: Float, enabled: Boolean) -> Unit)? = null,
	leadingContent: (@Composable () -> Unit)? = null,
	trailingContent: @Composable ((value: List<String>, enabled: Boolean) -> Unit)? = null,
	settingsRepository: SettingsRepository<Model, ModelBuilder>,
	value: (Model) -> List<String>,
	scope: CoroutineScope = rememberCoroutineScope(),
	enabledCondition: (Model) -> Boolean = { true },
	highlight: Boolean = false,
	onValueChange: (ModelBuilder.(value: List<String>) -> Unit)? = null,
) {
	var dialogValue by remember { mutableStateOf(emptySet<String>()) }
	var showDialog by remember { mutableStateOf(false) }

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
		summary = summary,
		supportingContent = { currentValue, _ ->
			if (currentValue.isNotEmpty()) {
				Text(generateSummary(currentValue.toSet()))
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
			dialogValue = it.toSet()
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
								id = R.string.dialog_reset
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
						scope.launch {
							settingsRepository.updateUserSettings {
								onValueChange?.invoke(this, dialogValue.toList())
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

