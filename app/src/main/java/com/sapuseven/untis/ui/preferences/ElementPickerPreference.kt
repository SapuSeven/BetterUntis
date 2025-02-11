package com.sapuseven.untis.ui.preferences

import ElementItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.google.protobuf.MessageLite
import com.sapuseven.compose.protostore.data.SettingsRepository
import com.sapuseven.compose.protostore.ui.preferences.Preference
import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import com.sapuseven.untis.api.model.untis.timetable.PeriodElement
import com.sapuseven.untis.components.ElementPicker
import com.sapuseven.untis.helpers.SerializationUtils
import com.sapuseven.untis.ui.dialogs.ElementPickerDialogNew
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString

@Composable
fun <Model : MessageLite, ModelBuilder : MessageLite.Builder> ElementPickerPreference(
	title: (@Composable () -> Unit),
	summary: (@Composable () -> Unit)? = null,
	leadingContent: (@Composable () -> Unit)? = null,
	settingsRepository: SettingsRepository<Model, ModelBuilder>,
	value: (Model) -> String,
	scope: CoroutineScope = rememberCoroutineScope(),
	enabledCondition: (Model) -> Boolean = { true },
	highlight: Boolean = false,
	onValueChange: (ModelBuilder.(value: String) -> Unit)? = null,
	elementPicker: ElementPicker
) {
	var selectedType: ElementType? by remember { mutableStateOf(null) }
	var showDialog by remember { mutableStateOf(false) }

	Preference(
		title = title,
		summary = summary,
		supportingContent = { currentValue, _ ->
			if (currentValue.isNotEmpty()) {
				decodeStoredTimetableValue(currentValue)?.let {
					ElementItem(it, elementPicker) { shortName, _, _ ->
						Text(shortName)
					}
				}
			}
		},
		leadingContent = leadingContent,
		settingsRepository = settingsRepository,
		value = value,
		scope = scope,
		enabledCondition = enabledCondition,
		highlight = highlight,
		onClick = {
			selectedType = decodeStoredTimetableValue(value(settingsRepository.getSettingsDefaults()))?.type
			showDialog = true;
		}
	)

	if (showDialog)
		ElementPickerDialogNew(
			elementPicker = elementPicker,
			title = title,
			onDismiss = {
				showDialog = false
			},
			onSelect = { element ->
				showDialog = false
				scope.launch {
					settingsRepository.updateSettings {
						onValueChange?.invoke(
							this,
							element?.let { encodeStoredTimetableValue(it) } ?: "")
					}
				}
			},
			initialType = selectedType
		)
}

fun encodeStoredTimetableValue(value: PeriodElement): String =
	SerializationUtils.getJSON().encodeToString(value)

fun decodeStoredTimetableValue(value: String): PeriodElement? = try {
	SerializationUtils.getJSON().decodeFromString(value)
} catch (e: Throwable) {
	null
}
