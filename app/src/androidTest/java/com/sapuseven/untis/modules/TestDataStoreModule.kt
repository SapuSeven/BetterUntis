package com.sapuseven.untis.modules

import androidx.datastore.core.DataStore
import com.sapuseven.untis.data.settings.model.Settings
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Singleton

@Module
@TestInstallIn(
	components = [SingletonComponent::class],
	replaces = [DataStoreModule::class]
)
object TestDataStoreModule {
	@Provides
	@Singleton
	fun provideProtoDataStore(): DataStore<Settings> =
		object : DataStore<Settings> {
			private val _data = MutableStateFlow<Settings>(Settings.getDefaultInstance())
			private var _currentData = Settings.getDefaultInstance()

			override val data: Flow<Settings>
				get() = _data

			override suspend fun updateData(transform: suspend (Settings) -> Settings): Settings {
				return transform(_currentData).also { _data.emit(it) }
			}
		}
}
