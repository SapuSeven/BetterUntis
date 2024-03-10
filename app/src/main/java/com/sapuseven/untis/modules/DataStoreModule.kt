package com.sapuseven.untis.modules

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
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
	val globalDataStore = context.globalDataStore

	companion object {
		private val Context.globalDataStore: DataStore<Preferences> by preferencesDataStore("global")
		private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("settings")

		val USER_ID_KEY = longPreferencesKey("active_user_id")

		fun getDarkModeKey(userId: Long): Preferences.Key<Boolean> = booleanPreferencesKey("${userId}_dark_mode")
	}
}

data class ThemeState(val isDarkMode: Boolean = false)

@Singleton
class ThemeManager @Inject constructor(
	private val dataStoreUtil: DataStoreUtil
) {
	val _themeState = MutableStateFlow(ThemeState())
	val themeState: StateFlow<ThemeState> = _themeState

	private val scope = CoroutineScope(Dispatchers.IO)

	init {
		scope.launch(Dispatchers.IO) {
			dataStoreUtil.globalDataStore.data
				.map { preferences -> preferences[DataStoreUtil.USER_ID_KEY] }
				.distinctUntilChanged()
				.collect { userId ->
					userId?.let { loadThemeState(it) }
				}
		}
	}

	private fun loadThemeState(userId: Long) {
		scope.launch(Dispatchers.IO) {
			dataStoreUtil.dataStore.data
				.map { preferences -> preferences[DataStoreUtil.getDarkModeKey(userId)] ?: false }
				.collect { isDarkMode ->
					_themeState.value = ThemeState(isDarkMode)
				}
		}
	}

	fun toggleTheme(userId: Long) {
		scope.launch(Dispatchers.IO) {
			dataStoreUtil.dataStore.edit { preferences ->
				preferences[DataStoreUtil.getDarkModeKey(userId)] = !(preferences[DataStoreUtil.getDarkModeKey(userId)] ?: false)
			}
		}
	}
}
