package com.sapuseven.untis.ui.activities.timetable

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.sapuseven.untis.api.model.untis.masterdata.Klasse
import com.sapuseven.untis.api.model.untis.masterdata.Room
import com.sapuseven.untis.api.model.untis.masterdata.Subject
import com.sapuseven.untis.api.model.untis.masterdata.Teacher
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.data.databases.entities.User
import com.sapuseven.untis.data.databases.entities.UserDao
import com.sapuseven.untis.data.databases.entities.UserWithData
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.modules.ThemeManager
import com.sapuseven.untis.ui.activities.ActivityViewModel
import com.sapuseven.untis.ui.dialogs.ProfileManagementDialogViewModel
import com.sapuseven.untis.ui.navigation.AppNavigator
import com.sapuseven.untis.ui.navigation.AppRoutes
import com.sapuseven.untis.viewmodels.CloseableCoroutineScope
import com.sapuseven.untis.viewmodels.ElementPickerDelegate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class TimetableViewModel @Inject constructor(
	savedStateHandle: SavedStateHandle,
	val elementPickerDelegate: ElementPickerDelegate,
	override val delegateScope: CoroutineScope,
	private val navigator: AppNavigator,
	private val themeManager: ThemeManager,
	private val userDao: UserDao,
) : ViewModel(),
	ProfileManagementDialogViewModel,
	ElementPickerDelegate by elementPickerDelegate
{
	private val args: AppRoutes.Timetable = savedStateHandle.toRoute<AppRoutes.Timetable>()

	var currentUser = mutableStateOf<User?>(null)
		private set

	var allUsersLiveData = userDao.getAllLive().distinctUntilChanged()
		private set

	var displayedName by mutableStateOf("")
		private set

	var profileManagementDialog by mutableStateOf(false)

	//fun getTimetableDatabaseInterface() = TimetableDatabaseInterface(userDatabase, currentUser.value!!.id)

	init {
		viewModelScope.launch(Dispatchers.IO) {
			currentUser.value = userDao.getById(args.userId)
			// Observe user list and handle changes in that list
			allUsersLiveData.asFlow().collect { users ->
				if (users.isEmpty()) {
					navigator.navigate(AppRoutes.Login)
				} else if (currentUser.value?.id?.let { currentUserId -> users.find { it.id == currentUserId } == null } == true) {
					// Potential improvement: With this approach, the user gets kicked out of the profile management dialog when the current user is deleted.
					switchUser(users.get(0))
				}
			}
		}

		elementPickerDelegate.loadAllElements(args.userId)
	}

	fun switchUser(user: User) {
		navigator.navigate(AppRoutes.Timetable(user.id)){
			popUpTo(0) // Pop all previous routes
		}
	}

	fun editUsers() {
		profileManagementDialog = true
	}

	override fun editUser(
		user: User?
	) {
		viewModelScope.launch {
			navigator.navigate(AppRoutes.LoginDataInput(userId = user?.id ?: -1))
		}
	}

	override suspend fun deleteUser(user: User) {
		withContext(Dispatchers.IO) {
			userDao.delete(user)
		}
	}

	override fun getAllUsers(): LiveData<List<User>> {
		return allUsersLiveData
	}

	fun toggleTheme() {
		currentUser.value?.id?.let {
			themeManager.toggleTheme(it)
		}
	}
}
