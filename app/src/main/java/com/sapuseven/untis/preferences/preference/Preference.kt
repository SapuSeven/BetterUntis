package com.sapuseven.untis.preferences.preference

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.sapuseven.untis.preferences.UntisPreferenceDataStore
import com.sapuseven.untis.ui.common.disabled
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> Preference(
	title: (@Composable () -> Unit),
	summary: (@Composable () -> Unit)? = null,
	icon: (@Composable () -> Unit)? = null,
	dependency: UntisPreferenceDataStore<*>? = null,
	dataStore: UntisPreferenceDataStore<T>,
	value: MutableState<T> = remember { mutableStateOf(dataStore.defaultValue) },
	supportingContent: @Composable ((value: T, enabled: Boolean) -> Unit)? = null,
	trailingContent: @Composable ((value: T, enabled: Boolean) -> Unit)? = null,
	scope: CoroutineScope = rememberCoroutineScope(),
	onClick: (value: T) -> Unit = {}
) {
	var enabled by remember {
		mutableStateOf(
			dependency?.isDefaultEnabled() ?: true
		)
	} // TODO: Make configurable

	scope.launch {
		awaitAll(
			async { dataStore.getValueFlow().collect { newValue -> value.value = newValue } },
			async { dependency?.getDependencyFlow()?.collect { enable -> enabled = enable } }
		)
	}

	ListItem(
		headlineText = {
			Box(modifier = Modifier.disabled(!enabled)) {
				title()
			}
		},
		supportingText = summary?.let {
			{
				Column {
					Box(modifier = Modifier.disabled(!enabled)) {
						summary()
					}

					supportingContent?.invoke(value.value, enabled)
				}
			}
		},
		leadingContent = icon?.let {
			{
				Box(modifier = Modifier.disabled(!enabled)) {
					icon()
				}
			}
		},
		trailingContent = { trailingContent?.invoke(value.value, enabled) },
		modifier = if (!enabled) Modifier else Modifier.clickable { onClick(value.value) }
	)
}
