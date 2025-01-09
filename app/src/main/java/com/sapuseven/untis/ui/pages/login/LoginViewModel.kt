package com.sapuseven.untis.ui.pages.login

import android.app.Activity
import androidx.activity.result.ActivityResult
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.sapuseven.untis.activities.LoginActivity.Companion.EXTRA_BOOLEAN_SHOW_BACK_BUTTON
import com.sapuseven.untis.api.client.SchoolSearchApi
import com.sapuseven.untis.api.model.untis.SchoolInfo
import com.sapuseven.untis.helpers.SerializationUtils.getJSON
import com.sapuseven.untis.services.CodeScanService
import com.sapuseven.untis.ui.pages.ActivityEvents
import com.sapuseven.untis.ui.pages.ActivityViewModel
import com.sapuseven.untis.ui.navigation.AppNavigator
import com.sapuseven.untis.ui.navigation.AppRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class LoginViewModel @Inject constructor(
	val schoolSearchApi: SchoolSearchApi,
	val codeScanService: CodeScanService,
	private val navigator: AppNavigator,
	savedStateHandle: SavedStateHandle
) : ActivityViewModel() {
	var searchMode by mutableStateOf(false)
		private set

	var shouldShowBackButton = derivedStateOf {
		searchMode || savedStateHandle.get<Boolean>(EXTRA_BOOLEAN_SHOW_BACK_BUTTON) ?: false
	}

	private val _schoolSearchText = MutableStateFlow<String>("")
	val schoolSearchText: StateFlow<String> = _schoolSearchText

	val events = MutableSharedFlow<LoginEvents>()

	fun onSchoolSearchFocusChanged(focused: Boolean) {
		if (focused) searchMode = true
	}

	private fun updateSearchMode(enabled: Boolean) {
		searchMode = enabled
		if (!enabled) {
			viewModelScope.launch {
				events.emit(LoginEvents.ClearFocus)
				_schoolSearchText.value = ""
			}
		}
	}

	fun goBack() {
		if (searchMode) {
			updateSearchMode(false)
		} else {
			navigator.popBackStack()
		}
	}

	fun updateSchoolSearchText(text: String) {
		_schoolSearchText.value = text
	}

	fun onLoginResult(result: ActivityResult) {
		if (result.resultCode == Activity.RESULT_OK) {
			viewModelScope.launch {
				activityEvents.send(ActivityEvents.Finish(Activity.RESULT_OK, result.data))
			}
		}
	}

	// onClick listeners
	fun onSchoolSelected(school: SchoolInfo) {
		navigator.navigate(
			AppRoutes.LoginDataInput(schoolInfoSerialized = getJSON().encodeToString<SchoolInfo>(school))
		)
	}

	fun onCodeScanClick() {
		/* TODO codeScanService.scanCode {
			viewModelScope.launch {
				events.emit(LoginEvents.StartLoginActivity(data = it))
			}
		}*/
	}

	fun onDemoClick() {
		navigator.navigate(AppRoutes.LoginDataInput(demoLogin = true))
	}

	fun onManualDataInputClick() {
		navigator.navigate(AppRoutes.LoginDataInput())
	}
}
