package com.sapuseven.untis.ui.activities.settings

import androidx.lifecycle.ViewModel
import com.sapuseven.untis.components.UserManager
import com.sapuseven.untis.data.databases.entities.UserDao
import com.sapuseven.untis.modules.ThemeManager
import com.sapuseven.untis.scope.UserScopeManager
import com.sapuseven.untis.ui.navigation.AppNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
	private val navigator: AppNavigator,
) : ViewModel() {}
