package com.sapuseven.compose.protostore.ui

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.datastore.core.DataStore
import com.sapuseven.compose.protostore.utils.conditional
import com.sapuseven.compose.protostore.utils.disabled
import com.sapuseven.compose.protostore.utils.ifNotNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <Model, Value> Preference(
	title: (@Composable () -> Unit),
	supportingContent: @Composable ((value: Value, enabled: Boolean) -> Unit)? = null,
	trailingContent: @Composable ((value: Value, enabled: Boolean) -> Unit)? = null,
	leadingContent: (@Composable () -> Unit)? = null,
	onClick: ((value: Value) -> Unit)? = null,
	dataSource: Flow<Model>,
	transform: (Model) -> Value,
	/*dependency: UntisPreferenceDataStore<*>? = null,
	dataStore: UntisPreferenceDataStore<T>,
	scope: CoroutineScope = rememberCoroutineScope(),
	highlight: Boolean = false*/
) {
	val data by dataSource.collectAsState(null)

	val enabled = true
	/*var enabled by remember {
		mutableStateOf(
			dependency?.isDefaultEnabled() ?: true
		)
	} // TODO: Make configurable


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
	}*/
	val interactionSource = remember { MutableInteractionSource() }

	ListItem(
		headlineContent = {
			Box(modifier = Modifier.disabled(!enabled)) {
				title()
			}
		},
		supportingContent = supportingContent?.let {
			{ data?.let { supportingContent(transform(it), enabled) } }
		},
		leadingContent = leadingContent?.let {
			{
				Box(modifier = Modifier.disabled(!enabled)) {
					leadingContent()
				}
			}
		},
		trailingContent = { data?.let { trailingContent?.invoke(transform(it), enabled) } },
		modifier = Modifier
			.conditional(enabled) {
				ifNotNull(onClick) { onClick ->
					clickable(
						interactionSource = interactionSource,
						indication = LocalIndication.current
					) { data?.let { onClick(transform(it)) } }
				}
			}
	)
}
