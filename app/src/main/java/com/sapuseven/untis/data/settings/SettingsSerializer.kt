package com.sapuseven.untis.data.settings

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import androidx.datastore.preferences.protobuf.InvalidProtocolBufferException
import com.sapuseven.untis.data.settings.model.Settings
import com.sapuseven.untis.data.settings.model.UserSettings
import java.io.InputStream
import java.io.OutputStream

object UserSettingsSerializer : Serializer<UserSettings> {
	override val defaultValue: UserSettings = UserSettings.getDefaultInstance()

	override suspend fun readFrom(input: InputStream): UserSettings {
		try {
			return UserSettings.parseFrom(input)
		} catch (exception: InvalidProtocolBufferException) {
			throw CorruptionException("Cannot read proto.", exception)
		}
	}

	override suspend fun writeTo(
		t: UserSettings,
		output: OutputStream
	) = t.writeTo(output)
}

val Context.settingsDataStore: DataStore<UserSettings> by dataStore(
	fileName = "settings.pb",
	serializer = UserSettingsSerializer
)

