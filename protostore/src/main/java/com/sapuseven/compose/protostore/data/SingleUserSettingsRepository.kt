package com.sapuseven.compose.protostore.data

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import com.google.protobuf.MessageLite
import kotlinx.coroutines.flow.Flow

abstract class SingleUserSettingsRepository <
	SettingsType : MessageLite,
	SettingsBuilderType : MessageLite.Builder
	> (
	private val _dataStore: DataStore<SettingsType>
) : SettingsRepository<SettingsType, SettingsBuilderType>, ViewModel() {
	override fun getUserSettings(): Flow<SettingsType> {
		return _dataStore.data
	}

	@Suppress("UNCHECKED_CAST")
	override suspend fun updateUserSettings(update: SettingsBuilderType.() -> Unit) {
		_dataStore.updateData { currentData ->
			val settingsBuilder = currentData.toBuilder() as SettingsBuilderType
			settingsBuilder.apply(update).build() as SettingsType
		}
	}
}
