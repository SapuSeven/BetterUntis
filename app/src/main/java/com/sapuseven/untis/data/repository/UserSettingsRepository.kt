package com.sapuseven.untis.data.repository

import android.util.Log
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.datastore.core.DataStore
import com.sapuseven.compose.protostore.data.MultiUserSettingsRepository
import com.sapuseven.untis.data.settings.model.Settings
import com.sapuseven.untis.data.settings.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserSettingsRepository @Inject constructor(
	private val userRepository: UserRepository,
	dataStore: DataStore<Settings>
) : MultiUserSettingsRepository<Settings, Settings.Builder, UserSettings, UserSettings.Builder>(
	dataStore
) {
	companion object {
		@Composable
		fun getDefaultColors() = getDefaultColors(MaterialTheme.colorScheme)

		fun getDefaultColors(colorScheme: ColorScheme) = DefaultColors(
			themeColor = colorScheme.primary,
			backgroundRegular = colorScheme.primary,
			backgroundRegularPast = colorScheme.primary.copy(alpha = .7f),
			backgroundExam = colorScheme.error,
			backgroundExamPast = colorScheme.error.copy(alpha = .7f),
			backgroundIrregular = colorScheme.tertiary,
			backgroundIrregularPast = colorScheme.tertiary.copy(alpha = .7f),
			backgroundCancelled = colorScheme.secondary,
			backgroundCancelledPast = colorScheme.secondary.copy(alpha = .7f),
		)
	}

	fun getSettings(userId: Long): Flow<UserSettings> {
		return getAllSettings().map { userSettings -> userSettings.userSettingsMap.getOrDefault(userId, UserSettings.getDefaultInstance()) }
	}

	private fun getUserSettings(dataStore: Settings, userId: Long): UserSettings {
		Log.d("SettingsRepository", "DataStore getUserSettings #$userId")

		return dataStore.userSettingsMap.getOrDefault(userId, UserSettings.getDefaultInstance())
	}

	override fun getUserSettings(dataStore: Settings): UserSettings {
		return getUserSettings(dataStore, userRepository.currentUser!!.id)
	}

	override fun updateUserSettings(currentData: Settings, userSettings: UserSettings): Settings {
		return currentData.toBuilder()
			.apply {
				putUserSettings(userRepository.currentUser!!.id, userSettings)
			}
			.build()
	}

	data class DefaultColors(
		val themeColor: Color,
		val backgroundRegular: Color,
		val backgroundRegularPast: Color,
		val backgroundExam: Color,
		val backgroundExamPast: Color,
		val backgroundIrregular: Color,
		val backgroundIrregularPast: Color,
		val backgroundCancelled: Color,
		val backgroundCancelledPast: Color,
	)
}

fun <T> T.withDefault(isPresent: Boolean, defaultValue: T): T = if (isPresent) this else defaultValue
