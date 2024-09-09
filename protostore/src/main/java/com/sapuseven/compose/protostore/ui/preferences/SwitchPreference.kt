package com.sapuseven.compose.protostore.ui.preferences

import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.google.protobuf.MessageLite
import com.sapuseven.compose.protostore.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun <Model : MessageLite, ModelBuilder : MessageLite.Builder> SwitchPreference(
	title: (@Composable () -> Unit),
	summary: (@Composable () -> Unit)? = null,
	supportingContent: @Composable ((value: Boolean, enabled: Boolean) -> Unit)? = null,
	leadingContent: (@Composable () -> Unit)? = null,
	settingsRepository: SettingsRepository<Model, ModelBuilder>,
	value: (Model) -> Boolean,
	scope: CoroutineScope = rememberCoroutineScope(),
	enabledCondition: (Model) -> Boolean = { true },
	highlight: Boolean = false,
	onValueChange: (ModelBuilder.(checked: Boolean) -> Unit)? = null,
) {
	Preference(
		title = title,
		summary = summary,
		supportingContent = supportingContent,
		leadingContent = leadingContent,
		trailingContent = { currentValue, enabled ->
			Switch(
				checked = currentValue,
				onCheckedChange = {
					scope.launch {
						settingsRepository.updateSettings {
							onValueChange?.invoke(this, it)
						}
					}
				},
				enabled = enabled
			)
		},
		settingsRepository = settingsRepository,
		value = value,
		scope = scope,
		enabledCondition = enabledCondition,
		highlight = highlight,
		onClick = { currentValue ->
			scope.launch {
				settingsRepository.updateSettings {
					onValueChange?.invoke(this, !currentValue)
				}
			}
		}
	)
}
