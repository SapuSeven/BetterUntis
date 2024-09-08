package com.sapuseven.compose.protostore.data

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import com.google.protobuf.GeneratedMessageLite
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

abstract class MultiUserPreferenceRepository <
	RootMessageType : GeneratedMessageLite<RootMessageType, RootBuilderType>,
	RootBuilderType : GeneratedMessageLite.Builder<RootMessageType, RootBuilderType>,
	MessageType : GeneratedMessageLite<MessageType, BuilderType>,
	BuilderType : GeneratedMessageLite.Builder<MessageType, BuilderType>,
> (
	private val _dataStore: DataStore<RootMessageType>
) : ViewModel() {
	abstract fun getUserSettings(dataStore: RootMessageType) : MessageType

	abstract fun updateUserSettings(currentData : RootMessageType, settings: MessageType) : RootMessageType

	fun getUserSettings(): Flow<MessageType> {
		return _dataStore.data.map { userSettings -> getUserSettings(userSettings) }
	}

	suspend fun updateUserSettings(update: BuilderType.() -> Unit) {
		_dataStore.updateData { currentData ->
			val settingsBuilder = getUserSettings(currentData).toBuilder()
			settingsBuilder.apply(update)
			updateUserSettings(currentData, settingsBuilder.build())
		}
	}
}
