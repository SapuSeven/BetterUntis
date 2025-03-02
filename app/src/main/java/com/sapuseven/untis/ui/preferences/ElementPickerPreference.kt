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
import com.sapuseven.untis.data.settings.model.TimetableElement
import com.sapuseven.untis.ui.dialogs.ElementPickerDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun <Model : MessageLite, ModelBuilder : MessageLite.Builder> ElementPickerPreference(
	title: (@Composable () -> Unit),
	summary: (@Composable () -> Unit)? = null,
	leadingContent: (@Composable () -> Unit)? = null,
	settingsRepository: SettingsRepository<Model, ModelBuilder>,
	value: (Model) -> TimetableElement,
	scope: CoroutineScope = rememberCoroutineScope(),
	enabledCondition: (Model) -> Boolean = { true },
	highlight: Boolean = false,
	onValueChange: (ModelBuilder.(value: TimetableElement) -> Unit)? = null,
	elementPicker: ElementPicker
) {
	var selectedType: ElementType? by remember { mutableStateOf(null) }
	var showDialog by remember { mutableStateOf(false) }

	Preference(
		title = title,
		summary = summary,
		supportingContent = { currentValue, _ ->
			currentValue.toPeriodElement()?.let {
				ElementItem(it, elementPicker) { shortName, _, _ ->
					Text(shortName)
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
			selectedType = value(settingsRepository.getSettingsDefaults()).toPeriodElement()?.type
			showDialog = true;
		}
	)

	if (showDialog)
		ElementPickerDialog(
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
							element?.toTimetableElement() ?: TimetableElement.getDefaultInstance()
						)
					}
				}
			},
			initialType = selectedType
		)
}

fun TimetableElement.toPeriodElement(): PeriodElement? =
	ElementType.entries.firstOrNull { it.id == elementType }?.let { PeriodElement(it, elementId) }

fun PeriodElement.toTimetableElement(): TimetableElement =
	TimetableElement.newBuilder().apply {
		elementId = id
		elementType = type.id
	}.build()
