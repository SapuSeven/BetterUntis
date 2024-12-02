package com.sapuseven.untis.ui.activities.settings

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.core.DataStore
import com.sapuseven.compose.protostore.data.MultiUserSettingsRepository
import com.sapuseven.compose.protostore.ui.preferences.materialColors
import com.sapuseven.untis.components.ElementPicker
import com.sapuseven.untis.data.databases.entities.UserDao
import com.sapuseven.untis.data.settings.model.Settings
import com.sapuseven.untis.data.settings.model.UserSettings
import com.sapuseven.untis.scope.UserScopeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsScreenViewModel @Inject constructor(
	private val userScopeManager: UserScopeManager,
	private val userDao: UserDao,
	dataStore: DataStore<Settings>
) : MultiUserSettingsRepository<Settings, Settings.Builder, UserSettings, UserSettings.Builder>(
	dataStore
) {
	private val userId = userScopeManager.user.id
	private lateinit var colorScheme: ColorScheme

	val elementPicker: ElementPicker
		get() = ElementPicker(userScopeManager.user, userDao)

	fun setColorScheme(colorScheme: ColorScheme) {
		this.colorScheme = colorScheme
	}

	override fun getUserSettings(dataStore: Settings): UserSettings {
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
		backgroundRegular = colorScheme.primary.toArgb()
		backgroundRegularPast = colorScheme.primary.copy(alpha = .7f).toArgb()
		backgroundExam = colorScheme.error.toArgb()
		backgroundExamPast = colorScheme.error.copy(alpha = .7f).toArgb()
		backgroundIrregular = colorScheme.tertiary.toArgb()
		backgroundIrregularPast = colorScheme.tertiary.copy(alpha = .7f).toArgb()
		backgroundCancelled = colorScheme.secondary.toArgb()
		backgroundCancelledPast = colorScheme.secondary.copy(alpha = .7f).toArgb()
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
