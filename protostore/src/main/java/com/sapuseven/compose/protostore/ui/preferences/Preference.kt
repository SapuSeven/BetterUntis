package com.sapuseven.compose.protostore.ui.preferences

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import com.google.protobuf.MessageLite
import com.sapuseven.compose.protostore.data.SettingsRepository
import com.sapuseven.compose.protostore.ui.utils.conditional
import com.sapuseven.compose.protostore.ui.utils.disabled
import com.sapuseven.compose.protostore.ui.utils.ifNotNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * @param title A composable to show as the preference title
 * @param summary A composable to show as the preference summary.
 *  It should be a description of the preference and independent of the current value.
 * @param supportingContent A composable to show below the preference summary.
 * 	This is usually implemented by other types of preferences that show their own input method.
 * @param leadingContent A composable to show in front of the preference. Usually an icon.
 * @param trailingContent A composable to show after the preference. Usually a switch or button.
 * @param settingsRepository The repository that contains the user settings.
 * @param value A function that returns the current value of the preference.
 * @param enabledCondition A function that returns a boolean indicating whether the preference should be enabled or not.
 * @param highlight A boolean indicating whether the preference should be highlighted or not.
 * 	Highlighting is a one-time effect that is triggered when the preference is first shown.
 * 	It can be used to highlight a specific preference to the user.
 * @param scope A coroutine scope to launch coroutines in.
 * @param onClick A function that is called when the preference is clicked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <Model : MessageLite, Value> Preference(
	title: (@Composable () -> Unit),
	summary: (@Composable () -> Unit)? = null,
	supportingContent: @Composable ((value: Value, enabled: Boolean) -> Unit)? = null,
	leadingContent: (@Composable () -> Unit)? = null,
	trailingContent: @Composable ((value: Value, enabled: Boolean) -> Unit)? = null,
	settingsRepository: SettingsRepository<Model, *>,
	value: (Model) -> Value,
	enabledCondition: (Model) -> Boolean = { true },
	highlight: Boolean = false,
	scope: CoroutineScope = rememberCoroutineScope(),
	onClick: ((value: Value) -> Unit)? = null,
) {
	val data by settingsRepository.getSettings().collectAsState(null)
	val interactionSource = remember { MutableInteractionSource() }

	val isEnabled = settingsRepository.getSettings().map { enabledCondition(it) }
		.collectAsState(initial = true)

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

	ListItem(
		headlineContent = {
			Box(modifier = Modifier.disabled(!isEnabled.value)) {
				title()
			}
		},
		supportingContent = {
			Column {
				data?.let {
					Box(modifier = Modifier.disabled(!isEnabled.value)) {
						summary?.invoke()
					}

					supportingContent?.invoke(value(it), isEnabled.value)
				}
			}
		},
		leadingContent = leadingContent?.let {
			{
				Box(modifier = Modifier.disabled(!isEnabled.value)) {
					leadingContent()
				}
			}
		},
		trailingContent = { data?.let { trailingContent?.invoke(value(it), isEnabled.value) } },
		modifier = Modifier
			.conditional(isEnabled.value) {
				ifNotNull(onClick) { onClick ->
					clickable(
						interactionSource = interactionSource,
						indication = LocalIndication.current
					) { data?.let { onClick(value(it)) } }
				}
			}
	)
}

/**
 * A basic preference without any associated value.
 * Can be used to provide custom onClick actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Preference(
	title: (@Composable () -> Unit),
	summary: (@Composable () -> Unit)? = null,
	supportingContent: @Composable ((enabled: Boolean) -> Unit)? = null,
	leadingContent: (@Composable () -> Unit)? = null,
	trailingContent: @Composable ((enabled: Boolean) -> Unit)? = null,
	enabledCondition: () -> Boolean = { true },
	highlight: Boolean = false,
	scope: CoroutineScope = rememberCoroutineScope(),
	onClick: (() -> Unit)? = null,
) {
	val interactionSource = remember { MutableInteractionSource() }

	val isEnabled = enabledCondition()

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

	ListItem(
		headlineContent = {
			Box(modifier = Modifier.disabled(!isEnabled)) {
				title()
			}
		},
		supportingContent = {
			Column {
				Box(modifier = Modifier.disabled(!isEnabled)) {
					summary?.invoke()
				}

				supportingContent?.invoke(isEnabled)
			}
		},
		leadingContent = leadingContent?.let {
			{
				Box(modifier = Modifier.disabled(!isEnabled)) {
					leadingContent()
				}
			}
		},
		trailingContent = { trailingContent?.invoke(isEnabled) },
		modifier = Modifier
			.conditional(isEnabled) {
				ifNotNull(onClick) { onClick ->
					clickable(
						interactionSource = interactionSource,
						indication = LocalIndication.current
					) { onClick() }
				}
			}
	)
}
