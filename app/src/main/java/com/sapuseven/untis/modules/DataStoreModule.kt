package com.sapuseven.untis.modules

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
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
}
