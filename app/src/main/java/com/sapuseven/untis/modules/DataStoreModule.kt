package com.sapuseven.untis.modules

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sapuseven.untis.data.settings.UserSettingsSerializer
import com.sapuseven.untis.data.settings.model.Settings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val DATA_STORE_FILE_NAME = "settings.pb"

@InstallIn(SingletonComponent::class)
@Module
object DataStoreModule {
	@Singleton
	@Provides
	fun provideProtoDataStore(@ApplicationContext appContext: Context): DataStore<Settings> {
		return DataStoreFactory.create(
			serializer = UserSettingsSerializer,
			produceFile = { appContext.dataStoreFile(DATA_STORE_FILE_NAME) },
			corruptionHandler = null,
			/*TODO migrations = listOf(
				SharedPreferencesMigration(
					appContext,
					USER_PREFERENCES_NAME
				)
			),*/
			scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
		)
	}

	@Provides
	fun provideThemeManager(): ThemeManager = ThemeManager()
}

data class ThemeState(val isDarkMode: Boolean = false)

@Singleton
class ThemeManager @Inject constructor(
	//private val dataStoreUtil: DataStoreUtil
) {
	val _themeState = MutableStateFlow(ThemeState())
	val themeState: StateFlow<ThemeState> = _themeState

	private val scope = CoroutineScope(Dispatchers.IO)

	/*init {
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
	}*/
}
