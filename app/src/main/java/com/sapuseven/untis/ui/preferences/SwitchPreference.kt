package com.sapuseven.untis.ui.preferences

import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.sapuseven.untis.preferences.UntisPreferenceDataStore
import kotlinx.coroutines.launch

@Composable
fun SwitchPreference(
	title: (@Composable () -> Unit),
	summary: (@Composable () -> Unit)? = null,
	icon: (@Composable () -> Unit)? = null,
	onCheckedChange: ((checked: Boolean) -> Boolean)? = null,
	dependency: UntisPreferenceDataStore<*>? = null,
	dataStore: UntisPreferenceDataStore<Boolean>
) {
	val scope = rememberCoroutineScope()

	Preference(
		title = title,
		summary = summary,
		icon = icon,
		dependency = dependency,
		dataStore = dataStore,
		onClick = { value ->
			scope.launch { dataStore.saveValue(onCheckedChange?.invoke(!value) ?: !value) }
		},
		trailingContent = { value, enabled ->
			Switch(
				checked = value,
				onCheckedChange = { newValue ->
					scope.launch { dataStore.saveValue(onCheckedChange?.invoke(newValue) ?: newValue) }
				},
				enabled = enabled
			)
		}
	)
}
