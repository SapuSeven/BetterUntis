package com.sapuseven.untis.ui.activities.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.sapuseven.untis.activities.LoginActivity
import com.sapuseven.untis.activities.LoginDataInputActivity
import com.sapuseven.untis.data.databases.entities.User
import com.sapuseven.untis.data.databases.entities.UserDao
import com.sapuseven.untis.modules.ThemeManager
import com.sapuseven.untis.modules.UserManager
import com.sapuseven.untis.ui.activities.ActivityEvents
import com.sapuseven.untis.ui.activities.ActivityViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
	val userManager: UserManager,
	val themeManager: ThemeManager,
	private val userDao: UserDao
) : ActivityViewModel() {
	var profileManagementDialog by mutableStateOf(false)

	var userList = mutableStateListOf<User>()
		private set

	var displayedName by mutableStateOf("")
		private set

	var activeUser = userManager.activeUser

	init {
	    viewModelScope.launch {
			userList.addAll(getUserList())
		}
	}

	fun editUsers() {
		profileManagementDialog = true
	}

	fun editUser(
		user: User?
	) = viewModelScope.launch {
		activityEvents.send(ActivityEvents.Launch(
			user?.let { LoginDataInputActivity::class.java } ?: LoginActivity::class.java,
			Bundle().apply {
				//user?.id?.let { contextActivity.putUserIdExtra(this, it) }
				putBoolean(LoginActivity.EXTRA_BOOLEAN_SHOW_BACK_BUTTON, true)
			}
		))
		/*loginLauncher.launch(
			Intent(
				contextActivity,
				user?.let { LoginDataInputActivity::class.java } ?: LoginActivity::class.java
			).apply {
				user?.id?.let { contextActivity.putUserIdExtra(this, it) }
				contextActivity.putBackgroundColorExtra(this)
				putExtra(LoginActivity.EXTRA_BOOLEAN_SHOW_BACK_BUTTON, true)
			})*/
	}

	suspend fun deleteUser(user: User) = withContext(Dispatchers.IO) {
		userDao.delete(user)
		//contextActivity.deleteProfile(user.id)
		//if (userDatabase.userDao().getAll().isEmpty())
			//contextActivity.recreate()
	}

	fun switchUser(user: User) {
		userManager.setActiveUser(user)
		viewModelScope.launch {
			//loadAllEvents()
		}
	}

	suspend fun getUserList(): List<User> = withContext(Dispatchers.IO) {
		return@withContext userDao.getAll()
	}

	fun toggleTheme() {
		activeUser.value?.id?.let {
			themeManager.toggleTheme(it)
		}
	}
}
