package com.sapuseven.untis.ui.activities.settings

import android.util.Log
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import com.sapuseven.compose.protostore.data.MultiUserSettingsRepository
import com.sapuseven.compose.protostore.ui.preferences.materialColors
import com.sapuseven.untis.data.settings.model.Settings
import com.sapuseven.untis.data.settings.model.UserSettings
import com.sapuseven.untis.scope.UserScopeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class SettingsRepository @Inject constructor(
	private val userScopeManager: UserScopeManager,
	dataStore: DataStore<Settings>
) : MultiUserSettingsRepository<Settings, Settings.Builder, UserSettings, UserSettings.Builder>(
	dataStore
) {
	lateinit var colorScheme: ColorScheme
	private val defaultColor = Color.Magenta.toArgb()

	private val userId = userScopeManager.user.id

	override fun getUserSettings(dataStore: Settings): UserSettings {
		Log.d("SettingsRepository", "DataStore getUserSettings")

		return dataStore.usersMap.getOrDefault(userId, getSettingsDefaults())
	}

	override fun getSettingsDefaults() = UserSettings.newBuilder().apply {
		automuteCancelledLessons = true
		automuteMutePriority = true
		automuteMinimumBreakLength = 5.0f
		// todo detailed errors

		backgroundFuture = Color.Transparent.toArgb()
		backgroundPast = Color(0x40808080).toArgb()
		marker = Color.White.toArgb()
		backgroundRegular = if (::colorScheme.isInitialized) colorScheme.primary.toArgb() else defaultColor
		backgroundRegularPast = if (::colorScheme.isInitialized) colorScheme.primary.copy(alpha = .7f).toArgb() else defaultColor
		backgroundExam = if (::colorScheme.isInitialized) colorScheme.error.toArgb() else defaultColor
		backgroundExamPast = if (::colorScheme.isInitialized) colorScheme.error.copy(alpha = .7f).toArgb() else defaultColor
		backgroundIrregular = if (::colorScheme.isInitialized) colorScheme.tertiary.toArgb() else defaultColor
		backgroundIrregularPast = if (::colorScheme.isInitialized) colorScheme.tertiary.copy(alpha = .7f).toArgb() else defaultColor
		backgroundCancelled = if (::colorScheme.isInitialized) colorScheme.secondary.toArgb() else defaultColor
		backgroundCancelledPast = if (::colorScheme.isInitialized) colorScheme.secondary.copy(alpha = .7f).toArgb() else defaultColor
		themeColor = /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
				with(LocalContext.current) {
					resources.getColor(android.R.color.system_accent1_500, theme)
				}
			else*/
			materialColors[0].toArgb()
		darkTheme = "auto"

		timetableSubstitutionsIrregular = true
		timetableItemPaddingOverlap = 4
		timetableItemPadding = 4
		timetableItemCornerRadius = 4
		timetableBoldLessonName = true
		timetableLessonNameFontSize = 14 // TODO Use Material typography values?
		timetableLessonInfoFontSize = 10 // TODO Use Material typography values?

		notificationsInMultiple = false
		notificationsBeforeFirstTime = 30

		notificationsVisibilitySubjects = "long"
		notificationsVisibilityRooms = "short"
		notificationsVisibilityTeachers = "short"
		notificationsVisibilityClasses = "short"

		connectivityRefreshInBackground = true

		infocenterAbsencesTimeRange = "current_schoolyear"
	}.build()

	override fun updateUserSettings(currentData: Settings, userSettings: UserSettings): Settings {
		return currentData.toBuilder()
			.putUsers(userId, userSettings)
			.build()
	}
}
