package com.sapuseven.untis.components

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.compositionLocalOf
import com.sapuseven.untis.ui.activities.settings.UserSettingsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


class ThemeManager @AssistedInject constructor(
	private val userSettingsRepositoryFactory: UserSettingsRepository.Factory,
	@Assisted val scope: CoroutineScope,
	@Assisted val colorScheme: ColorScheme,
) {
	@AssistedFactory
	interface Factory {
		fun create(scope: CoroutineScope, colorScheme: ColorScheme): ThemeManager
	}

	private val settingsRepository = userSettingsRepositoryFactory.create(colorScheme)

	val _themeState = MutableStateFlow(ThemeState())
	val themeState: StateFlow<ThemeState> = _themeState

	init {
		scope.launch(Dispatchers.IO) {
			loadThemeState()
		}
		/*scope.launch(Dispatchers.IO) {
			dataStoreUtil.globalDataStore.data
				.map { preferences -> preferences[DataStoreUtil.USER_ID_KEY] }
				.distinctUntilChanged()
				.collect { userId ->
					userId?.let { loadThemeState(it) }
				}
		}*/
	}

	private suspend fun loadThemeState() {
		waitForSettings().let { settings ->
			val forceDarkTheme = when (settings.darkTheme) {
				"on" -> true
				"off" -> false
				else -> null
			}
			_themeState.value = ThemeState(forceDarkTheme)
		}


		/*scope.launch(Dispatchers.IO) {
			dataStoreUtil.dataStore.data
				.map { preferences -> preferences[DataStoreUtil.getDarkModeKey(userId)] ?: false }
				.collect { isDarkMode ->
					_themeState.value = ThemeState(isDarkMode)
				}
		}*/
	}

	private suspend fun waitForSettings() = settingsRepository.getSettings().filterNotNull().first()
}

data class ThemeState(val forceDarkTheme: Boolean? = null)

data class DarkTheme(val isDark: Boolean = false)

val LocalTheme = compositionLocalOf { DarkTheme() }
