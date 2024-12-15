package com.sapuseven.untis.ui.activities.timetable

import android.content.Intent
import android.util.Log
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.datastore.core.DataStore
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.sapuseven.untis.R
import com.sapuseven.untis.activities.SettingsActivity
import com.sapuseven.untis.activities.SettingsActivity.Companion.EXTRA_STRING_PREFERENCE_HIGHLIGHT
import com.sapuseven.untis.activities.SettingsActivity.Companion.EXTRA_STRING_PREFERENCE_ROUTE
import com.sapuseven.untis.components.ElementPicker
import com.sapuseven.untis.components.UserManager
import com.sapuseven.untis.data.databases.entities.User
import com.sapuseven.untis.data.databases.entities.UserDao
import com.sapuseven.untis.data.settings.model.Settings
import com.sapuseven.untis.data.settings.model.UserSettings
import com.sapuseven.untis.modules.ThemeManager
import com.sapuseven.untis.scope.UserScopeManager
import com.sapuseven.untis.ui.activities.settings.SettingsRepository
import com.sapuseven.untis.ui.navigation.AppNavigator
import com.sapuseven.untis.ui.navigation.AppRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimetableViewModel @Inject constructor(
	private val navigator: AppNavigator,
	private val themeManager: ThemeManager,
	internal val userManager: UserManager,
	private val userScopeManager: UserScopeManager,
	private val userDao: UserDao,
	private val repository: SettingsRepository,
	savedStateHandle: SavedStateHandle,
) : ViewModel() {
	val args = savedStateHandle.toRoute<AppRoutes.Timetable>()

	private val _currentUserSettings = MutableStateFlow<UserSettings>(repository.getSettingsDefaults())
	val currentUserSettings: StateFlow<UserSettings> = _currentUserSettings

	var profileManagementDialog by mutableStateOf(false)
	var feedbackDialog by mutableStateOf(false)

	var loading by mutableStateOf(true)

	private val _needsPersonalTimetable = MutableStateFlow(false)
	val needsPersonalTimetable: StateFlow<Boolean> = _needsPersonalTimetable

	val currentUser: User = userScopeManager.user

	val allUsersState: StateFlow<List<User>> = userManager.allUsersState

	val elementPicker: ElementPicker
		get() = ElementPicker(userScopeManager.user, userDao)

	init {
		viewModelScope.launch {
			repository.getSettings().collect { userSettings ->
				_currentUserSettings.value = userSettings
				_needsPersonalTimetable.value = userSettings.timetablePersonalTimetable.isBlank()
			}
		}
	}

	fun setColorScheme(colorScheme: ColorScheme) {
		repository.colorScheme = colorScheme
	}

	fun switchUser(user: User) {
		userManager.switchUser(user)
		/*navigator.navigate(AppRoutes.Timetable(user.id)) {
			popUpTo(0) // Pop all previous routes
		}*/
	}

	fun editUser(user: User?) {
		navigator.navigate(AppRoutes.LoginDataInput(userId = user?.id ?: -1))
	}

	fun editUsers() {
		profileManagementDialog = true
	}

	fun toggleTheme() {
		/*user.value?.id?.let {
			themeManager.toggleTheme(it)
		}*/
	}

	val onAnonymousSettingsClick = {
		navigator.navigate(AppRoutes.Settings.Timetable(highlightTitle = R.string.preference_timetable_personal_timetable))
	}

	// TODO
	@Composable
	fun lastRefreshText() = stringResource(
		id = R.string.main_last_refreshed,
		/*if (weekViewRefreshTimestamps[weekViewPage] ?: 0L > 0L) formatTimeDiff(Instant.now().millis - weekViewRefreshTimestamps[weekViewPage]!!)
		else*/ stringResource(id = R.string.main_last_refreshed_never)
	)

	fun showFeedback() {
		feedbackDialog = true
	}
}
