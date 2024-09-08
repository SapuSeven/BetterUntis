package com.sapuseven.compose.protostore.ui

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
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
import com.sapuseven.compose.protostore.data.MultiUserSettingsRepository
import com.sapuseven.compose.protostore.utils.conditional
import com.sapuseven.compose.protostore.utils.disabled
import com.sapuseven.compose.protostore.utils.ifNotNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <Model : MessageLite, Value> Preference(
	title: (@Composable () -> Unit),
	supportingContent: @Composable ((value: Value, enabled: Boolean) -> Unit)? = null,
	leadingContent: (@Composable () -> Unit)? = null,
	trailingContent: @Composable ((value: Value, enabled: Boolean) -> Unit)? = null,
	settingsRepository: MultiUserSettingsRepository<*, *, Model, *>,
	value: (Model) -> Value,
	enabledCondition: (Model) -> Boolean = { true },
	highlight: Boolean = false,
	scope: CoroutineScope = rememberCoroutineScope(),
	onClick: ((value: Value) -> Unit)? = null,
) {
	val data by settingsRepository.getUserSettings().collectAsState(null)
	val interactionSource = remember { MutableInteractionSource() }

	val isEnabled = settingsRepository.getUserSettings().map { enabledCondition(it) }.collectAsState(initial = true)

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
		supportingContent = supportingContent?.let {
			{ data?.let { supportingContent(value(it), isEnabled.value) } }
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
