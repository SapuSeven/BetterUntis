package com.sapuseven.untis.ui.pages.settings

import android.util.Log
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.core.DataStore
import com.sapuseven.compose.protostore.data.MultiUserSettingsRepository
import com.sapuseven.untis.data.settings.model.Settings
import com.sapuseven.untis.data.settings.model.UserSettings
import com.sapuseven.untis.scope.UserScopeManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class UserSettingsRepository @AssistedInject constructor(
	@Assisted private val colorScheme: ColorScheme,
	userScopeManager: UserScopeManager,
	dataStore: DataStore<Settings>
) : MultiUserSettingsRepository<Settings, Settings.Builder, UserSettings, UserSettings.Builder>(
	dataStore
) {
	@AssistedFactory
	interface Factory {
		fun create(colorScheme: ColorScheme = lightColorScheme()): UserSettingsRepository
	}

	private val userId = userScopeManager.userOptional?.id

	override fun getUserSettings(dataStore: Settings): UserSettings {
		Log.d("SettingsRepository", "DataStore getUserSettings")

		return dataStore.userSettingsMap.getOrDefault(userId, getSettingsDefaults())
	}

	override fun updateUserSettings(currentData: Settings, userSettings: UserSettings): Settings {
		return currentData.toBuilder()
			.apply {
				userId?.let {
					putUserSettings(userId, userSettings)
				}
			}
			.build()
	}

	override fun getSettingsDefaults(): UserSettings = UserSettings.newBuilder().apply {
		automuteCancelledLessons = true
		automuteMinimumBreakLength = 5.0f
		// todo detailed errors

		backgroundFuture = Color.Transparent.toArgb()
		backgroundPast = Color(0x40808080).toArgb()
		marker = Color.White.toArgb()
		backgroundRegular = colorScheme.primary.toArgb()
		backgroundRegularPast = colorScheme.primary.copy(alpha = .7f).toArgb()
		backgroundExam = colorScheme.error.toArgb()
		backgroundExamPast = colorScheme.error.copy(alpha = .7f).toArgb()
		backgroundIrregular = colorScheme.tertiary.toArgb()
		backgroundIrregularPast = colorScheme.tertiary.copy(alpha = .7f).toArgb()
		backgroundCancelled = colorScheme.secondary.toArgb()
		backgroundCancelledPast = colorScheme.secondary.copy(alpha = .7f).toArgb()
		themeColor =
			colorScheme.primary.toArgb() // TODO: This should always be the system theme color, not the current theme primary color
		darkTheme = "auto"

		timetableSubstitutionsIrregular = true
		timetableItemPadding = 2
		timetableItemCornerRadius = 4
		timetableCenteredLessonInfo = false
		timetableBoldLessonName = true
		timetableLessonNameFontSize = 16 // TODO Use Material typography values?
		timetableLessonInfoFontSize = 12 // TODO Use Material typography values?
		timetableZoomEnabled = true
		timetableZoomLevel = 1.0f

		notificationsInMultiple = false
		notificationsBeforeFirstTime = 30

		notificationsVisibilitySubjects = "long"
		notificationsVisibilityRooms = "short"
		notificationsVisibilityTeachers = "short"
		notificationsVisibilityClasses = "short"

		connectivityRefreshInBackground = true

		infocenterAbsencesTimeRange = "current_schoolyear"
	}.build()
}
