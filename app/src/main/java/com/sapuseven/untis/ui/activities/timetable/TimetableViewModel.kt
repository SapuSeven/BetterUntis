package com.sapuseven.untis.ui.activities.timetable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.sapuseven.untis.data.databases.entities.User
import com.sapuseven.untis.data.databases.entities.UserDao
import com.sapuseven.untis.modules.ThemeManager
import com.sapuseven.untis.ui.navigation.AppNavigator
import com.sapuseven.untis.ui.navigation.AppRoutes
import com.sapuseven.untis.viewmodels.ElementPickerDelegate
import com.sapuseven.untis.viewmodels.UserManagerDelegate
import com.sapuseven.untis.viewmodels.UserManagerDelegateImpl
import com.sapuseven.untis.viewmodels.ViewModelDelegateFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
	UserManagerDelegate by userManagerDelegate
{
	var profileManagementDialog by mutableStateOf(false)

	private val args: AppRoutes.Timetable = savedStateHandle.toRoute<AppRoutes.Timetable>()

	init {
		elementPickerDelegate.init(viewModelScope)
		userManagerDelegate.init(viewModelScope, args.userId)

		viewModelScope.launch {
			allUsers.collect { users ->
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
}
