package com.sapuseven.untis.ui.activities.settings

import androidx.compose.material3.ColorScheme
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.sapuseven.untis.components.ElementPicker
import com.sapuseven.untis.data.database.entities.UserDao
import com.sapuseven.untis.scope.UserScopeManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel(assistedFactory = SettingsScreenViewModel.Factory::class)
class SettingsScreenViewModel @AssistedInject constructor(
	private val userSettingsRepositoryFactory: UserSettingsRepository.Factory,
	private val globalSettingsRepository: GlobalSettingsRepository,
	private val userScopeManager: UserScopeManager,
	private val userDao: UserDao,
	val savedStateHandle: SavedStateHandle,
	@Assisted val colorScheme: ColorScheme,
) : ViewModel() {
	@AssistedFactory
	interface Factory {
		fun create(colorScheme: ColorScheme): SettingsScreenViewModel
	}

	val repository = userSettingsRepositoryFactory.create(colorScheme)
	val globalRepository = globalSettingsRepository

	val elementPicker: ElementPicker
		get() = ElementPicker(userScopeManager.user, userDao)
}
