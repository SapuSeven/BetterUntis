package com.sapuseven.untis.feature.automute.ui

import android.annotation.SuppressLint
import android.app.NotificationManager.EXTRA_AUTOMATIC_RULE_ID
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sapuseven.untis.core.datastore.UserSettingsDataSource
import com.sapuseven.untis.core.domain.repository.UserRepository
import com.sapuseven.untis.core.domain.service.AutoMuteService
import com.sapuseven.untis.core.model.user.User
import com.sapuseven.untis.feature.automute.service.AutoMuteServiceZenRuleImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("NewApi")
@HiltViewModel
class AutoMuteSettingsViewModel @Inject constructor(
	userRepository: UserRepository,
	val userSettingsDataSource: UserSettingsDataSource,
	val autoMuteService: AutoMuteService,
	private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
	fun enableAutoMute() {
		if (!autoMuteService.isPermissionGranted()) return

		viewModelScope.launch {
			userSettingsDataSource.updateSettings {
				automuteEnable = true
			}
			autoMuteService.autoMuteEnable()
		}
	}

	private val _user = MutableStateFlow<User?>(null)
	val user: StateFlow<User?> = _user

	init {
		viewModelScope.launch {
			val userId = savedStateHandle.get<Long>("userId")
				?: (autoMuteService as? AutoMuteServiceZenRuleImpl)?.let {
					val ruleId: String? = savedStateHandle[EXTRA_AUTOMATIC_RULE_ID]
					val rule = it.getRule(ruleId)
					rule?.conditionId?.getQueryParameter("userId")?.toLongOrNull()
				}

			val user = userId?.let { userRepository.getUserById(it) }
				?: userRepository.observeActiveUser().firstOrNull()

			user?.let {
				_user.value = it
				autoMuteService.setUser(it)
			} ?: run {
				// TODO: Handle case where userId can't be determined since autoMuteService requires a user to be set
			}
		}
	}
}
