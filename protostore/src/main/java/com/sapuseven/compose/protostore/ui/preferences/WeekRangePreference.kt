package com.sapuseven.compose.protostore.ui.preferences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import java.util.Locale

@Composable
fun <Model : MessageLite, ModelBuilder : MessageLite.Builder> WeekRangePreference(
	title: (@Composable () -> Unit),
	summary: (@Composable () -> Unit)? = null,
	//supportingContent: @Composable ((value: Float, enabled: Boolean) -> Unit)? = null,
	leadingContent: (@Composable () -> Unit)? = null,
	trailingContent: @Composable ((value: Set<String>, enabled: Boolean) -> Unit)? = null,
	settingsRepository: SettingsRepository<Model, ModelBuilder>,
	value: (Model) -> Set<String>,
	scope: CoroutineScope = rememberCoroutineScope(),
	enabledCondition: (Model) -> Boolean = { true },
	highlight: Boolean = false,
	onValueChange: (ModelBuilder.(value: Set<String>) -> Unit)? = null,
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
							settingsRepository.updateSettings {
								onValueChange?.invoke(this, dialogValue)
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

