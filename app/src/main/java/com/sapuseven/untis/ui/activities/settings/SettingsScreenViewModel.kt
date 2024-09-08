package com.sapuseven.untis.ui.activities.settings

import androidx.datastore.core.DataStore
import com.sapuseven.compose.protostore.data.MultiUserSettingsRepository
import com.sapuseven.untis.data.settings.model.UserSettings
import com.sapuseven.untis.data.settings.model.Settings
import com.sapuseven.untis.scope.UserScopeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsScreenViewModel @Inject constructor(
	private val userScopeManager: UserScopeManager,
	dataStore: DataStore<Settings>
) : MultiUserSettingsRepository<Settings, Settings.Builder, UserSettings, UserSettings.Builder>(dataStore) {
	private val userId = userScopeManager.user.id

	override fun getUserSettings(dataStore: Settings) : UserSettings {
		return dataStore.usersMap.getOrDefault(userId, UserSettings.getDefaultInstance()) // TODO: Get an instance with default values instead
	}

	override fun updateUserSettings(currentData : Settings, userSettings: UserSettings) : Settings {
		return currentData.toBuilder()
			.putUsers(userId, userSettings)
			.build()
	}
}
