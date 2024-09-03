package com.sapuseven.untis.ui.activities.timetable

import android.os.Bundle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.sapuseven.untis.activities.LoginActivity
import com.sapuseven.untis.activities.LoginDataInputActivity
import com.sapuseven.untis.data.databases.entities.User
import com.sapuseven.untis.data.databases.entities.UserDao
import com.sapuseven.untis.modules.ThemeManager
import com.sapuseven.untis.ui.activities.ActivityEvents
import com.sapuseven.untis.ui.activities.ActivityViewModel
import com.sapuseven.untis.ui.navigation.AppNavigator
import com.sapuseven.untis.ui.navigation.AppRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class TimetableViewModel @Inject constructor(
	savedStateHandle: SavedStateHandle,
	private val navigator: AppNavigator,
	private val themeManager: ThemeManager,
	private val userDao: UserDao,
) : ActivityViewModel() {
	private val args: AppRoutes.Timetable = savedStateHandle.toRoute<AppRoutes.Timetable>()

	var currentUser = mutableStateOf<User?>(null)
		private set

	var allUsers = mutableStateListOf<User>()
		private set

	var displayedName by mutableStateOf("")
		private set

	var profileManagementDialog by mutableStateOf(false)

	init {
		viewModelScope.launch {
			loadUserDetails()
		}
	}

	private suspend fun loadUserDetails() = withContext(Dispatchers.IO) {
		currentUser.value = userDao.getById(args.userId)
		allUsers.clear()
		allUsers.addAll(userDao.getAll())
	}

	fun switchUser(user: User) {
		navigator.navigate(AppRoutes.Timetable(user.id))
	}

	fun editUsers() {
		profileManagementDialog = true
	}

	fun editUser(
		user: User?
	) = viewModelScope.launch {
		navigator.navigate(AppRoutes.LoginDataInput(userId = user?.id ?: -1))
	}

	suspend fun deleteUser(user: User) = withContext(Dispatchers.IO) {
		userDao.delete(user)
		//contextActivity.deleteProfile(user.id)
		//if (userDatabase.userDao().getAll().isEmpty())
		//contextActivity.recreate()
	}

	fun toggleTheme() {
		currentUser.value?.id?.let {
			themeManager.toggleTheme(it)
		}
	}
}
