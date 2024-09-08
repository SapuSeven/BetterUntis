package com.sapuseven.untis.ui.activities.settings

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
	val dataStore: DataStore<UserSettings>
) : ViewModel() {
	private val userId = userScopeManager.user.id

	// Get the preferences of the selected profile
	fun getUserSettings(): Flow<Settings> {
		return dataStore.data
			.map { userSettings ->
				userSettings.usersMap.getOrDefault(userId, Settings.getDefaultInstance()) // TODO: Get an instance with default values instead
			}
	}

	fun updateUserSettings(update: (Settings.Builder) -> Unit) {
		viewModelScope.launch {
			dataStore.updateData { currentData ->
				currentData.toBuilder()
					.putUsers(
						userId,
						currentData.usersMap[userId]?.toBuilder()?.apply {
							update(this)
						}?.build() ?: Settings.newBuilder().apply(update).build()
					)
					.build()
			}
		}
	}


	fun getUserId(): String {
		return userScopeManager.user.id.toString()
	}
}
