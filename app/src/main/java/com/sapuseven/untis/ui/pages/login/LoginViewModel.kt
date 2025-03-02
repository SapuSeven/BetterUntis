package com.sapuseven.untis.ui.pages.login

import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import com.sapuseven.untis.api.model.untis.SchoolInfo
import com.sapuseven.untis.helpers.SerializationUtils.getJSON
import com.sapuseven.untis.services.CodeScanService
import com.sapuseven.untis.ui.navigation.AppNavigator
import com.sapuseven.untis.ui.navigation.AppRoutes
import com.sapuseven.untis.ui.pages.ActivityViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
	private val codeScanService: CodeScanService,
	private val navigator: AppNavigator,
) : ActivityViewModel() {
	var searchMode by mutableStateOf(false)
		private set

	var shouldShowBackButton = derivedStateOf {
		searchMode //|| savedStateHandle.get<Boolean>(EXTRA_BOOLEAN_SHOW_BACK_BUTTON) ?: false
	}

	private val _schoolSearchText = MutableStateFlow<String>("")
	val schoolSearchText: StateFlow<String> = _schoolSearchText

	val events = MutableSharedFlow<LoginEvents>()

	val codeScanResultHandler: (String?) -> Unit = {
		navigator.navigate(AppRoutes.LoginDataInput(autoLoginData = it))
	}

	fun setCodeScanLauncher(launcher: ManagedActivityResultLauncher<ScanOptions, ScanIntentResult>) {
		codeScanService.setLauncher(launcher)
	}

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

	fun onSchoolSelected(school: SchoolInfo) {
		navigator.navigate(
			AppRoutes.LoginDataInput(schoolInfoSerialized = getJSON().encodeToString<SchoolInfo>(school))
		)
	}

	fun onCodeScanClick() {
		codeScanService.scanCode(codeScanResultHandler)
	}

	fun onDemoClick() {
		navigator.navigate(AppRoutes.LoginDataInput(demoLogin = true))
	}

	fun onManualDataInputClick() {
		navigator.navigate(AppRoutes.LoginDataInput())
	}
}
