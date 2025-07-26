package com.sapuseven.untis.ui.pages.settings.automute

import android.annotation.SuppressLint
import android.app.NotificationManager.EXTRA_AUTOMATIC_RULE_ID
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sapuseven.untis.persistence.entity.User
import com.sapuseven.untis.persistence.entity.UserDao
import com.sapuseven.untis.data.repository.UserSettingsRepository
import com.sapuseven.untis.services.AutoMuteService
import com.sapuseven.untis.services.AutoMuteServiceZenRuleImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("NewApi")
@HiltViewModel
class AutoMuteSettingsViewModel @Inject constructor(
	userDao: UserDao,
	val userSettingsRepository: UserSettingsRepository,
	val autoMuteService: AutoMuteService,
	private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
	private val _user = MutableStateFlow<User?>(null)
	val user: StateFlow<User?> = _user

	companion object {
		const val EXTRA_USER_ID = "com.sapuseven.untis.ui.pages.settings.automute.EXTRA_USER_ID"
	}

	init {
		if (autoMuteService is AutoMuteServiceZenRuleImpl) {
			viewModelScope.launch {
				(savedStateHandle.get<Long>(EXTRA_USER_ID) ?: let {
					val ruleId: String? = savedStateHandle[EXTRA_AUTOMATIC_RULE_ID]
					val rule = autoMuteService.getRule(ruleId)
					rule?.conditionId?.getQueryParameter("userId")?.toLongOrNull()
				})?.let { userId ->
					userDao.getByIdAsync(userId)?.let {
						_user.value = it
						autoMuteService.setUser(it)
					}
				} ?: run {
					// TODO: Handle case where userId can't be determined since autoMuteService requires a user to be set
				}
			}
		}
	}
}
