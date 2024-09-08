package com.sapuseven.compose.protostore.ui

import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.google.protobuf.GeneratedMessageLite
import com.google.protobuf.MessageLite
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.reflect.KMutableProperty0

@Composable
fun <Model : MessageLite> SwitchPreference(
	title: (@Composable () -> Unit),
	supportingContent: @Composable ((checked: Boolean, enabled: Boolean) -> Unit)? = null,
	leadingContent: (@Composable () -> Unit)? = null,
	onCheckedChange: ((checked: Boolean) -> Unit)? = null,
	dataSource: Flow<Model>,
	transform: (Model) -> Boolean,
	//dependency: UntisPreferenceDataStore<*>? = null,
	//dataStore: UntisPreferenceDataStore<Boolean>
) {
	Preference<Model, Boolean>(
		title = title,
		supportingContent = supportingContent,
		leadingContent = leadingContent,
		trailingContent = { value, enabled ->
			Switch(
				checked = value,
				onCheckedChange = onCheckedChange,
				enabled = enabled
			)
		},
		//dependency = dependency,
		//dataStore = dataStore,
		dataSource = dataSource,
		transform = transform,
		onClick = { value -> onCheckedChange?.invoke(!value)}
	)
}
