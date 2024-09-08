package com.sapuseven.compose.protostore.ui

import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.google.protobuf.MessageLite
import com.sapuseven.compose.protostore.data.MultiUserSettingsRepository
import kotlinx.coroutines.launch

@Composable
fun <Model : MessageLite, ModelBuilder : MessageLite.Builder> SwitchPreference(
	title: (@Composable () -> Unit),
	supportingContent: @Composable ((checked: Boolean, enabled: Boolean) -> Unit)? = null,
	leadingContent: (@Composable () -> Unit)? = null,
	onCheckedChange: (ModelBuilder.(checked: Boolean) -> Unit)? = null,
	settingsRepository: MultiUserSettingsRepository<*, *, Model, ModelBuilder>,
	transform: (Model) -> Boolean,
	//dependency: UntisPreferenceDataStore<*>? = null,
	//dataStore: UntisPreferenceDataStore<Boolean>
) {
	val scope = rememberCoroutineScope()

	Preference<Model, Boolean>(
		title = title,
		supportingContent = supportingContent,
		leadingContent = leadingContent,
		trailingContent = { value, enabled ->
			Switch(
				checked = value,
				onCheckedChange = {
					scope.launch {
						settingsRepository.updateUserSettings {
							onCheckedChange?.invoke(this, it)
						}
					}
				},
				enabled = enabled
			)
		},
		//dependency = dependency,
		//dataStore = dataStore,
		settingsRepository = settingsRepository,
		transform = transform,
		onClick = { value ->
			scope.launch {
				settingsRepository.updateUserSettings {
					onCheckedChange?.invoke(this, !value)
				}
			}
		}
	)
}
