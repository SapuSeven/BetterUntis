package com.sapuseven.untis.ui.activities

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.sapuseven.untis.api.client.SchoolSearchApi
import com.sapuseven.untis.api.client.UserDataApi
import com.sapuseven.untis.services.CodeScanService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginDataInputViewModel @Inject constructor(
	val userDataApi: UserDataApi,
	savedStateHandle: SavedStateHandle
) : ViewModel() {
	val loginData = LoginData()

	var advanced by mutableStateOf(
		loginData.proxyUrl.value?.isNotEmpty() == true ||
			loginData.apiUrl.value?.isNotEmpty() == true
	)

	var validate by mutableStateOf(false)
		private set

	var loading by mutableStateOf(false)
		private set

	val schoolIdValid = derivedStateOf { true }
	val usernameValid = derivedStateOf { true }
	val proxyUrlValid = derivedStateOf { true }
	val apiUrlValid = derivedStateOf { true }

	fun onLoginClick() {
		validate = true
		if (schoolIdValid.value && usernameValid.value && proxyUrlValid.value && apiUrlValid.value) {
			//snackbarHostState.currentSnackbarData?.dismiss()
			//loadData()
		}
	}
}

// Using `null` as the default values allows for partial data updates
class LoginData(
	initialProfileName: String? = null,
	initialSchoolId: String? = null,
	initialAnonymous: Boolean? = null,
	initialUsername: String? = null,
	initialApiUrl: String? = null,
) {
	val profileName = mutableStateOf(initialProfileName)
	val schoolId = mutableStateOf(initialSchoolId)
	val anonymous = mutableStateOf(initialAnonymous)
	val username = mutableStateOf(initialUsername)
	val password = mutableStateOf<String?>(null)
	val proxyUrl = mutableStateOf<String?>(null)
	val apiUrl = mutableStateOf(initialApiUrl)
	val skipAppSecret = mutableStateOf<Boolean?>(null)
}
