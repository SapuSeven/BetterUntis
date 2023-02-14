package com.sapuseven.untis.ui.preferences

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import com.sapuseven.untis.preferences.UntisPreferenceDataStore
import com.sapuseven.untis.ui.common.conditional
import com.sapuseven.untis.ui.common.disabled
import com.sapuseven.untis.ui.common.ifNotNull
import kotlinx.coroutines.*

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
	onClick: ((value: T) -> Unit)? = null,
	highlight: Boolean = false
) {
	var enabled by remember {
		mutableStateOf(
			dependency?.isDefaultEnabled() ?: true
		)
	} // TODO: Make configurable

	val interactionSource = remember { MutableInteractionSource() }

	if (highlight)
		LaunchedEffect(Unit) {
			scope.launch {
				val press = PressInteraction.Press(Offset.Zero)
				delay(100)
				interactionSource.emit(press)
				delay(3000)
				interactionSource.emit(PressInteraction.Release(press))
			}
		}

	LaunchedEffect(Unit) {
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
		modifier = Modifier
			.conditional(enabled) {
				ifNotNull(value = onClick) { onClick ->
					clickable(
						interactionSource = interactionSource,
						indication = LocalIndication.current
					) { onClick(value.value) }
				}
			}
	)
}
