package com.sapuseven.untis.ui.preferences

import ElementItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
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
import com.sapuseven.untis.data.repository.MasterDataRepository
import com.sapuseven.untis.data.settings.model.TimetableElement
import com.sapuseven.untis.persistence.entity.ElementEntity
import com.sapuseven.untis.ui.dialogs.ElementPickerDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
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
	elements: Map<ElementType, List<ElementEntity>>,
) {
	val selectedTypeState: State<ElementType?> = remember {
		settingsRepository.getSettings().map { settings ->
			ElementType.entries.firstOrNull { it.id == value(settings).elementType }
		}
	}.collectAsState(initial = null)
	var showDialog by remember { mutableStateOf(false) }

	Preference(
		title = title,
		summary = summary,
		supportingContent = { currentValue, _ ->
			currentValue.toElementEntity(elements)?.let {
				ElementItem(it) { shortName, _, _ ->
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
			showDialog = true;
		}
	)

	if (showDialog)
		ElementPickerDialog(
			title = title,
			elements = elements,
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
			initialType = selectedTypeState.value
		)
}

fun TimetableElement.toPeriodElement(): PeriodElement? =
	ElementType.entries.firstOrNull { it.id == elementType }?.let { PeriodElement(it, elementId) }

fun TimetableElement.toElementEntity(elements: Map<ElementType, List<ElementEntity>>): ElementEntity? =
	elements[ElementType.entries.firstOrNull { it.id == elementType }]?.firstOrNull { it.id == elementId }

fun TimetableElement.toElementEntity(masterDataRepository: MasterDataRepository): ElementEntity? =
	ElementType.entries.firstOrNull { it.id == elementType }?.let { type ->
		masterDataRepository.getElement(elementId, type)
	}

fun PeriodElement.toTimetableElement(): TimetableElement =
	TimetableElement.newBuilder().apply {
		elementId = id
		elementType = type.id
	}.build()

fun ElementEntity.toTimetableElement(): TimetableElement =
	TimetableElement.newBuilder().apply {
		elementId = id
		elementType = getType().id
	}.build()
