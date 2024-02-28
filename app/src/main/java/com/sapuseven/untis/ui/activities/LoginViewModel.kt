package com.sapuseven.untis.ui.activities

import android.net.Uri
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sapuseven.untis.activities.LoginActivity.Companion.EXTRA_BOOLEAN_SHOW_BACK_BUTTON
import com.sapuseven.untis.api.client.SchoolSearchApi
import com.sapuseven.untis.api.model.SchoolInfo
import com.sapuseven.untis.helpers.ErrorMessageDictionary
import com.sapuseven.untis.helpers.SerializationUtils.getJSON
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
	val schoolSearchApi: SchoolSearchApi,
	savedStateHandle: SavedStateHandle
) : ViewModel() {
	val debounceMillis: Long = 500

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

	val clearFocusEvent = MutableSharedFlow<Unit>()

	fun onSchoolSearchFocusChanged(focused: Boolean) {
		if (focused) searchMode = true
	}

	private fun updateSearchMode(enabled: Boolean) {
		searchMode = enabled
		if (!enabled) {
			viewModelScope.launch {
				clearFocusEvent.emit(Unit)
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

	fun startSchoolSearch(scope: CoroutineScope) {
		schoolSearchError = null
		schoolSearchErrorRaw = null
		schoolSearchItems = emptyList()

		schoolSearchJob?.cancel()
		schoolSearchJob = scope.launch {
			if (schoolSearchText.isEmpty()) return@launch

			schoolSearchLoading = true
			delay(debounceMillis)
			val schoolSearchResult = schoolSearchApi.searchSchools(schoolSearchText)
			schoolSearchLoading = false

			schoolSearchResult.fold({
				schoolSearchItems = it.schools
			}, {
				schoolSearchError = ErrorMessageDictionary.getErrorMessageResource(it.code, false)
				schoolSearchErrorRaw = it.message.orEmpty()
			})
		}
	}

	fun stopSchoolSearch() {
		schoolSearchJob?.cancel()
	}

	// onClick listeners
	fun onSchoolSelected(school: SchoolInfo) {
		val builder = Uri.Builder()
			.appendQueryParameter("schoolInfo", getJSON().encodeToString(school))

		/*loginLauncher.launch(
			Intent(
			this@LoginActivity, LoginDataInputActivity::class.java
		).apply {
			data = builder.build()
		})*/
	}

	fun onCodeScanClick() {
		/*codeScanService.scanCode {
						loginLauncher.launch(
							Intent(
								this@LoginActivity,
								LoginDataInputActivity::class.java
							).apply {
								data = it
							})
					}*/
	}

	fun onDemoClick() {
		/*loginLauncher.launch(
							Intent(
							this@LoginActivity,
							LoginDataInputActivity::class.java
						).apply {
							putBackgroundColorExtra(this)
							putExtra(EXTRA_BOOLEAN_DEMO_LOGIN, true)
						})*/
	}

	fun onManualDataInputClick() {
		/*loginLauncher.launch(
							Intent(
							this@LoginActivity,
							LoginDataInputActivity::class.java
						).apply {
							putBackgroundColorExtra(this)
						})*/
	}
}
