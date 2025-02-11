package com.sapuseven.untis.ui.pages.settings.automute

import android.annotation.SuppressLint
import android.app.NotificationManager.EXTRA_AUTOMATIC_RULE_ID
import androidx.compose.material3.lightColorScheme
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sapuseven.untis.data.database.entities.User
import com.sapuseven.untis.data.database.entities.UserDao
import com.sapuseven.untis.services.AutoMuteService
import com.sapuseven.untis.services.AutoMuteServiceZenRuleImpl
import com.sapuseven.untis.ui.pages.settings.UserSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("NewApi")
@HiltViewModel
class AutoMuteSettingsViewModel @Inject constructor(
	userSettingsRepositoryFactory: UserSettingsRepository.Factory,
	userDao: UserDao,
	val autoMuteService: AutoMuteService,
	private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
	private val _user = MutableStateFlow<User?>(null)
	val user: StateFlow<User?> = _user

	val repository = userSettingsRepositoryFactory.create()

	init {
		if (autoMuteService is AutoMuteServiceZenRuleImpl) {
			viewModelScope.launch {
				val ruleId: String? = savedStateHandle[EXTRA_AUTOMATIC_RULE_ID]
				val rule = autoMuteService.getRule(ruleId)
				rule?.conditionId?.getQueryParameter("userId")?.toLongOrNull()?.let { userId ->
					userDao.getByIdAsync(userId)?.let {
						_user.value = it
						autoMuteService.setUser(it)
					}
				}
			}
		}
	}
}
