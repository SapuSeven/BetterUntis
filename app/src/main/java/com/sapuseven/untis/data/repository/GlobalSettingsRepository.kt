package com.sapuseven.untis.data.repository

import androidx.datastore.core.DataStore
import com.sapuseven.compose.protostore.data.MultiUserSettingsRepository
import com.sapuseven.untis.data.settings.model.GlobalSettings
import com.sapuseven.untis.data.settings.model.Settings
import javax.inject.Inject

class GlobalSettingsRepository @Inject constructor(
	dataStore: DataStore<Settings>
) : MultiUserSettingsRepository<Settings, Settings.Builder, GlobalSettings, GlobalSettings.Builder>(
	dataStore
) {
	override fun getUserSettings(dataStore: Settings): GlobalSettings =
		if (dataStore.globalSettings.initialized)
			dataStore.globalSettings
		else
			GlobalSettings.getDefaultInstance()

	override fun updateUserSettings(currentData: Settings, userSettings: GlobalSettings): Settings =
		currentData.toBuilder().setGlobalSettings(userSettings.toBuilder().setInitialized(true).build()).build()
}
