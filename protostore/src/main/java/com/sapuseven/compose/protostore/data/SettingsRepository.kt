package com.sapuseven.compose.protostore.data

import com.google.protobuf.MessageLite
import kotlinx.coroutines.flow.Flow

interface SettingsRepository<SettingsType : MessageLite, SettingsBuilderType : MessageLite.Builder> {
	fun getSettings(): Flow<SettingsType>

	fun getSettingsDefaults(): SettingsType

	suspend fun updateSettings(update: SettingsBuilderType.() -> Unit)
}
