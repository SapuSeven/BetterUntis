package com.sapuseven.untis.ui.activities.timetable

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.sapuseven.untis.R
import com.sapuseven.untis.data.databases.entities.User
import com.sapuseven.untis.data.databases.entities.UserDao
import com.sapuseven.untis.modules.ThemeManager
import com.sapuseven.untis.ui.navigation.AppNavigator
import com.sapuseven.untis.ui.navigation.AppRoutes
import com.sapuseven.untis.viewmodels.ElementPickerDelegate
import com.sapuseven.untis.viewmodels.UserManagerDelegate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimetableViewModel @Inject constructor(
	savedStateHandle: SavedStateHandle,
	val userManagerDelegate: UserManagerDelegate,
	val elementPickerDelegate: ElementPickerDelegate,
	private val navigator: AppNavigator,
	private val themeManager: ThemeManager,
	private val userDao: UserDao,
) : ViewModel(),
	ElementPickerDelegate by elementPickerDelegate,
	UserManagerDelegate by userManagerDelegate {
	var profileManagementDialog by mutableStateOf(false)
	var feedbackDialog by mutableStateOf(false)

	var loading by mutableStateOf(true)

	private val args: AppRoutes.Timetable = savedStateHandle.toRoute<AppRoutes.Timetable>()

	init {
		elementPickerDelegate.init(viewModelScope)
		userManagerDelegate.init(viewModelScope, args.userId)

		viewModelScope.launch(Dispatchers.IO) {
			userManagerDelegate.allUsers.collect { users ->
				users?.let {
					if (users.isEmpty() == true) {
						navigator.navigate(AppRoutes.Login)
					} else if (user.value?.id?.let { currentUserId -> users.find { it.id == currentUserId } == null } == true) {
						// Potential improvement: With this approach, the user gets kicked out of the profile management dialog when the current user is deleted.
						switchUser(users.get(0))
					}
				}
			}
		}
	}

	fun switchUser(user: User) {
		navigator.navigate(AppRoutes.Timetable(user.id)) {
			popUpTo(0) // Pop all previous routes
		}
	}

	fun editUsers() {
		profileManagementDialog = true
	}

	fun toggleTheme() {
		user.value?.id?.let {
			themeManager.toggleTheme(it)
		}
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
