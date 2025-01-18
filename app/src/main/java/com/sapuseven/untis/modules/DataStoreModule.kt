package com.sapuseven.untis.modules

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import androidx.datastore.preferences.protobuf.InvalidProtocolBufferException
import com.sapuseven.untis.data.settings.model.Settings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Singleton

private const val DATA_STORE_FILE_NAME = "settings.pb"

@InstallIn(SingletonComponent::class)
@Module
object DataStoreModule {
	@Provides
	@Singleton
	fun provideProtoDataStore(@ApplicationContext appContext: Context): DataStore<Settings> {
		return DataStoreFactory.create(
			serializer = UserSettingsSerializer,
			produceFile = { appContext.dataStoreFile(DATA_STORE_FILE_NAME) },
			corruptionHandler = ReplaceFileCorruptionHandler {
				Settings.getDefaultInstance()
			},
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

internal object UserSettingsSerializer : Serializer<Settings> {
	override val defaultValue: Settings = Settings.getDefaultInstance()

	override suspend fun readFrom(input: InputStream): Settings {
		try {
			return Settings.parseFrom(input)
		} catch (exception: InvalidProtocolBufferException) {
			throw CorruptionException("Cannot read proto", exception)
		}
	}

	override suspend fun writeTo(
		t: Settings,
		output: OutputStream
	) = t.writeTo(output)
}
