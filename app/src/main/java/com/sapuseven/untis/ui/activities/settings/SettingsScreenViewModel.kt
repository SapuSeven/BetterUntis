package com.sapuseven.untis.ui.activities.settings

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.core.DataStore
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.sapuseven.compose.protostore.data.MultiUserSettingsRepository
import com.sapuseven.compose.protostore.ui.preferences.materialColors
import com.sapuseven.untis.components.ElementPicker
import com.sapuseven.untis.data.databases.entities.UserDao
import com.sapuseven.untis.data.settings.model.Settings
import com.sapuseven.untis.data.settings.model.UserSettings
import com.sapuseven.untis.scope.UserScopeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsScreenViewModel @Inject constructor(
	private val settingsViewModel: SettingsRepository,
	private val userScopeManager: UserScopeManager,
	private val userDao: UserDao,
	val savedStateHandle: SavedStateHandle,
	val repository: SettingsRepository,
) : ViewModel() {
	val elementPicker: ElementPicker
		get() = ElementPicker(userScopeManager.user, userDao)

	fun setColorScheme(colorScheme: ColorScheme) {
		repository.colorScheme = colorScheme
	}
}
