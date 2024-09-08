package com.sapuseven.untis.ui.activities.settings

import androidx.datastore.core.DataStore
import androidx.lifecycle.viewModelScope
import com.sapuseven.compose.protostore.data.MultiUserPreferenceRepository
import com.sapuseven.untis.data.settings.model.Settings
import com.sapuseven.untis.data.settings.model.UserSettings
import com.sapuseven.untis.scope.UserScopeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsScreenViewModel @Inject constructor(
	private val userScopeManager: UserScopeManager,
	dataStore: DataStore<UserSettings>
) : MultiUserPreferenceRepository<UserSettings, UserSettings.Builder, Settings, Settings.Builder>(dataStore) {
	private val userId = userScopeManager.user.id

	override fun getUserSettings(dataStore: UserSettings) : Settings {
		return dataStore.usersMap.getOrDefault(userId, Settings.getDefaultInstance()) // TODO: Get an instance with default values instead
	}

	override fun updateUserSettings(currentData : UserSettings, settings: Settings) : UserSettings {
		return currentData.toBuilder()
			.putUsers(userId, settings)
			.build()
	}

	fun getUserId(): String {
		return userScopeManager.user.id.toString()
	}
}
