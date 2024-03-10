package com.sapuseven.untis.modules

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sapuseven.untis.modules.DataStoreUtil.Companion.IS_DARK_MODE_KEY
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
	@Provides
	fun provideDataStoreUtil(@ApplicationContext context: Context): DataStoreUtil = DataStoreUtil(context)

	@Provides
	fun provideThemeManager(dataStoreUtil: DataStoreUtil): ThemeManager = ThemeManager(dataStoreUtil)
}

// TODO Move the following stuff elsewhere
class DataStoreUtil @Inject constructor(context: Context) {
	val dataStore = context.dataStore

	companion object {
		private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("settings")
		val IS_DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
	}
}

data class ThemeState(val isDarkMode: Boolean = false)

@Singleton
class ThemeManager @Inject constructor(
	private val dataStoreUtil: DataStoreUtil
) {
	private val _themeState = MutableStateFlow(ThemeState())
	val themeState: StateFlow<ThemeState> = _themeState

	private val scope = CoroutineScope(Dispatchers.IO)

	init {
		scope.launch(Dispatchers.IO) {
			dataStoreUtil.dataStore.data.map { preferences ->
				ThemeState(preferences[IS_DARK_MODE_KEY] ?: false)
			}.collect {
				_themeState.value = it
			}
		}

	}

	fun toggleTheme() {
		scope.launch(Dispatchers.IO) {
			dataStoreUtil.dataStore.edit { preferences ->
				preferences[IS_DARK_MODE_KEY] = !(preferences[IS_DARK_MODE_KEY] ?: false)
			}
		}
	}
}
