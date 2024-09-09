package com.sapuseven.compose.protostore.data

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import com.google.protobuf.MessageLite
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

abstract class MultiUserSettingsRepository <
	SettingsType : MessageLite,
	SettingsBuilderType : MessageLite.Builder,
	UserSettingsType : MessageLite,
	UserSettingsBuilderType : MessageLite.Builder,
> (
	private val _dataStore: DataStore<SettingsType>
) : SettingsRepository<UserSettingsType, UserSettingsBuilderType>, ViewModel() {
	abstract fun getUserSettings(dataStore: SettingsType) : UserSettingsType

	abstract fun updateUserSettings(currentData : SettingsType, userSettings: UserSettingsType) : SettingsType

	override fun getSettings(): Flow<UserSettingsType> {
		return _dataStore.data.map { userSettings -> getUserSettings(userSettings) }
	}

	@Suppress("UNCHECKED_CAST")
	override suspend fun updateSettings(update: UserSettingsBuilderType.() -> Unit) {
		_dataStore.updateData { currentData ->
			val settingsBuilder = getUserSettings(currentData).toBuilder() as UserSettingsBuilderType
			settingsBuilder.apply(update)
			updateUserSettings(currentData, settingsBuilder.build() as UserSettingsType)
		}
	}
}
