package com.sapuseven.untis.ui.activities.main

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.material3.DrawerState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sapuseven.untis.activities.LoginActivity
import com.sapuseven.untis.activities.LoginDataInputActivity
import com.sapuseven.untis.data.databases.entities.User
import com.sapuseven.untis.data.databases.entities.UserDao
import com.sapuseven.untis.modules.DataStoreUtil
import com.sapuseven.untis.modules.ThemeManager
import com.sapuseven.untis.modules.UserManager
import com.sapuseven.untis.ui.activities.ActivityEvents
import com.sapuseven.untis.ui.activities.ActivityViewModel
import com.sapuseven.untis.ui.navigation.AppNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@Deprecated(
	message = " There is no \"Main\" anymore.",
	replaceWith = ReplaceWith("TimetableViewModel()", "com.sapuseven.untis.ui.activities.timetable"),
	level = DeprecationLevel.ERROR
)
@HiltViewModel
class MainViewModel @Inject constructor(
	val navigator: AppNavigator,
	private val userManager: UserManager,
	private val themeManager: ThemeManager,
	private val userDao: UserDao
) : ActivityViewModel() {
	var activeUser = userManager.activeUser
}
