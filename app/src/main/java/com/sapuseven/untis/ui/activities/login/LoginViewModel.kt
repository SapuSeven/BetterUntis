package com.sapuseven.untis.ui.activities.login

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
import com.sapuseven.untis.api.exceptions.UntisApiException
import com.sapuseven.untis.api.model.untis.SchoolInfo
import com.sapuseven.untis.helpers.ErrorMessageDictionary
import com.sapuseven.untis.helpers.SerializationUtils.getJSON
import com.sapuseven.untis.services.CodeScanService
import com.sapuseven.untis.ui.activities.ActivityEvents
import com.sapuseven.untis.ui.activities.ActivityViewModel
import com.sapuseven.untis.ui.navigation.AppNavigator
import com.sapuseven.untis.ui.navigation.AppRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
	val schoolSearchApi: SchoolSearchApi,
	val codeScanService: CodeScanService,
	private val navigator: AppNavigator,
	savedStateHandle: SavedStateHandle
) : ActivityViewModel() {
	val debounceMillis: Long = 300

	var searchMode by mutableStateOf(false)
		private set

	var shouldShowBackButton = derivedStateOf {
		searchMode || savedStateHandle.get<Boolean>(EXTRA_BOOLEAN_SHOW_BACK_BUTTON) ?: false
	}

	var schoolSearchText by mutableStateOf("")
		private set

	var schoolSearchItems by mutableStateOf<List<SchoolInfo>>(emptyList())
		private set

	var schoolSearchError: Int? by mutableStateOf(null)
		private set

	var schoolSearchErrorRaw: String? by mutableStateOf(null)
		private set

	var schoolSearchLoading by mutableStateOf(false)
		private set

	var schoolSearchJob by mutableStateOf<Job?>(null)
		private set

	val events = MutableSharedFlow<LoginEvents>()

	fun onSchoolSearchFocusChanged(focused: Boolean) {
		if (focused) searchMode = true
	}

	private fun updateSearchMode(enabled: Boolean) {
		searchMode = enabled
		if (!enabled) {
			viewModelScope.launch {
				events.emit(LoginEvents.ClearFocus)
				schoolSearchText = ""
			}
		}
	}

	fun goBack(): Boolean {
		if (searchMode) {
			updateSearchMode(false)
			return false
		} else {
			return true
		}
	}

	fun updateSchoolSearchText(text: String) {
		schoolSearchText = text
	}

	fun startSchoolSearch() {
		schoolSearchError = null
		schoolSearchErrorRaw = null
		schoolSearchItems = emptyList()

		schoolSearchJob?.cancel()
		schoolSearchJob = viewModelScope.launch {
			if (schoolSearchText.isEmpty()) return@launch

			schoolSearchLoading = true
			delay(debounceMillis)
			try {
				schoolSearchItems = schoolSearchApi.searchSchools(schoolSearchText).schools
			} catch (e: UntisApiException) {
				schoolSearchError =
					ErrorMessageDictionary.getErrorMessageResource(e.error?.code, false)
				schoolSearchErrorRaw = e.message.orEmpty()
			} catch (e: Exception) {
				schoolSearchError = null
				schoolSearchErrorRaw = e.message.orEmpty()
			} finally {
				schoolSearchLoading = false
			}
		}
	}

	fun stopSchoolSearch() {
		schoolSearchJob?.cancel()
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
