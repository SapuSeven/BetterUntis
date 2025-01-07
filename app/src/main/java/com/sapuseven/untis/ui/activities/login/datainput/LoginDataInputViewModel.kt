package com.sapuseven.untis.ui.activities.login.datainput

import android.net.Uri
import android.util.Log
import android.util.Patterns
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.sapuseven.untis.R
import com.sapuseven.untis.activities.LoginDataInputActivity.Companion.DEMO_API_URL
import com.sapuseven.untis.activities.SAVED_STATE_INTENT_DATA
import com.sapuseven.untis.api.client.SchoolSearchApi
import com.sapuseven.untis.api.client.UserDataApi
import com.sapuseven.untis.api.exception.UntisApiException
import com.sapuseven.untis.api.model.response.UserDataResult
import com.sapuseven.untis.api.model.untis.MasterData
import com.sapuseven.untis.api.model.untis.SchoolInfo
import com.sapuseven.untis.api.model.untis.masterdata.TimeGrid
import com.sapuseven.untis.components.UserManager
import com.sapuseven.untis.data.database.entities.User
import com.sapuseven.untis.data.database.entities.UserDao
import com.sapuseven.untis.helpers.ErrorMessageDictionary
import com.sapuseven.untis.helpers.ErrorMessageDictionary.ERROR_CODE_TOO_MANY_RESULTS
import com.sapuseven.untis.helpers.SerializationUtils.getJSON
import com.sapuseven.untis.ui.activities.ActivityViewModel
import com.sapuseven.untis.ui.navigation.AppNavigator
import com.sapuseven.untis.ui.navigation.AppRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

// TODO: Things to check:
//       - respect proxy host
//       - bookmarks
@HiltViewModel
class LoginDataInputViewModel @Inject constructor(
	val schoolSearchApi: SchoolSearchApi,
	val userDataApi: UserDataApi,
	val userDao: UserDao,
	private val navigator: AppNavigator,
	private val userManager: UserManager,
	savedStateHandle: SavedStateHandle
) : ActivityViewModel() {
	val args = savedStateHandle.toRoute<AppRoutes.LoginDataInput>()

	private val existingUserId = if (args.userId == -1L) null else args.userId

	val isExistingUser = existingUserId != null

	val useStoredPassword
		get() = isExistingUser && loginData.password.value.isNullOrEmpty() && loginData.storedPassword != null

	val loginData = LoginData()

	var advanced by mutableStateOf(
		loginData.proxyUrl.value?.isNotEmpty() == true || loginData.apiUrl.value?.isNotEmpty() == true
	)

	var searchMode by mutableStateOf(false)

	var validate by mutableStateOf(false)
		private set

	var loading by mutableStateOf(false)
		private set

	var errorText: Int? by mutableStateOf(null)
		private set

	var errorTextRaw: String? by mutableStateOf(null)
		private set

	var showQrCodeErrorDialog by mutableStateOf(false)
		private set

	val showProfileUpdate = args.profileUpdate == true

	var schoolIdLocked by mutableStateOf(false)

	val schoolIdValid = derivedStateOf {
		loginData.schoolId.value?.let {
			it.isNotEmpty()
		} ?: false
	}

	val usernameValid = derivedStateOf {
		loginData.username.value?.let {
			it.isNotEmpty()
		} ?: false || (loginData.anonymous.value == true)
	}

	val proxyUrlValid = derivedStateOf {
		loginData.proxyUrl.value?.let {
			it.isEmpty() || Patterns.WEB_URL.matcher(it).matches()
		} ?: true
	}

	val apiUrlValid = derivedStateOf {
		loginData.apiUrl.value?.let {
			it.isEmpty() || Patterns.WEB_URL.matcher(it).matches()
		} ?: true
	}

	val schoolInfoFromSearch = args.schoolInfoSerialized?.let {
		getJSON().decodeFromString<SchoolInfo>(it)
	}

	init {
		viewModelScope.launch(Dispatchers.IO) {
			existingUserId?.let { userDao.getById(it) }?.let { loginData.loadFromUser(it) }
		}

		if (args.demoLogin == true) {
			loginData.anonymous.value = true
			loginData.schoolId.value = "demo"
			advanced = true
			loginData.apiUrl.value = DEMO_API_URL

			loadData()
		}

		schoolInfoFromSearch?.let {
			loginData.schoolId.value = schoolInfoFromSearch.schoolId.toString()
			schoolIdLocked = true
		}

		if (showProfileUpdate) {
			loadData()
		}

		try {
			loadFromAppLinkData(savedStateHandle.get<String>(SAVED_STATE_INTENT_DATA))
		} catch (e: Exception) {
			// TODO handle correctly
			showQrCodeErrorDialog = true
		}
	}

	fun onLoginClick() {
		validate = true
		if (schoolIdValid.value && usernameValid.value && proxyUrlValid.value && apiUrlValid.value) {
			errorText = null
			errorTextRaw = null
			loadData()
		}
	}

	private fun loadFromAppLinkData(appLinkDataString: String?) {
		if (appLinkDataString == null) return;

		val appLinkData = Uri.parse(appLinkDataString)

		if (appLinkData?.isHierarchical == true && appLinkData.scheme == "untis" && appLinkData.host == "setschool") {
			// Untis-native values
			loginData.schoolId.value = appLinkData.getQueryParameter("school")
			loginData.username.value = appLinkData.getQueryParameter("user")
			loginData.password.value = appLinkData.getQueryParameter("key")

			// Custom values
			loginData.anonymous.value = appLinkData.getBooleanQueryParameter("anonymous", false)
			loginData.proxyUrl.value = appLinkData.getQueryParameter("proxyUrl") ?: ""
			loginData.apiUrl.value = appLinkData.getQueryParameter("apiUrl")
			loginData.skipAppSecret.value =
				appLinkData.getBooleanQueryParameter("skipAppSecret", false)

			advanced =
				loginData.proxyUrl.value?.isNotEmpty() == true || loginData.apiUrl.value?.isNotEmpty() == true
		} else {
			showQrCodeErrorDialog = true
		}
	}

	private fun loadData() = viewModelScope.launch {
		loading = true

		try {
			val schoolInfo = loadSchoolInfo() ?: run {
				errorText = R.string.logindatainput_error_invalid_school
				return@launch
			}

			val untisApiUrl = buildUntisApiUrl(schoolInfo)

			val appSharedSecret = loadAppSharedSecret(untisApiUrl)

			val userData = loadUserData(untisApiUrl, appSharedSecret)
			val user = buildUser(untisApiUrl, appSharedSecret, schoolInfo, userData)

			withContext(Dispatchers.IO) {
				val userId = saveUser(user, userData.masterData)
				userManager.switchUser(userId)
				navigator.navigate(AppRoutes.Timetable) {
					popUpTo(0) // Pop all previous routes
				}
			}
		} catch (e: UntisApiException) {
			Log.e(LoginDataInputViewModel::class.simpleName, "loadData Untis error", e)
			val errorTextRes = ErrorMessageDictionary.getErrorMessageResource(e.error?.code, false)
			errorText = errorTextRes ?: R.string.errormessagedictionary_generic
			errorTextRaw = when (e.error?.code) {
				ERROR_CODE_TOO_MANY_RESULTS -> "Check the school id" // TODO: This is an exampe. Add detailed descriptions to errormessagedictionary
				else -> if (errorTextRes == null) e.error?.message else null
			}
		} catch (e: Exception) {
			Log.e(LoginDataInputViewModel::class.simpleName, "loadData error", e)
			errorText = R.string.errormessagedictionary_generic
			errorTextRaw = e.message
		} finally {
			loading = false
		}
	}

	private fun buildUser(
		untisApiUrl: String,
		appSharedSecret: String,
		schoolInfo: SchoolInfo,
		userData: UserDataResult
	): User {
		val user = User(
			existingUserId ?: 0,
			loginData.profileName.value ?: "",
			untisApiUrl,
			schoolInfo.schoolId.toString(),
			if (loginData.anonymous.value != true) loginData.username.value else null,
			if (loginData.anonymous.value != true) appSharedSecret else null,
			loginData.anonymous.value == true,
			userData.masterData.timeGrid ?: TimeGrid.generateDefault(),
			userData.masterData.timeStamp,
			userData.userData,
			userData.settings,
			bookmarks = emptySet()//bookmarks
		)
		return user
	}

	private suspend fun saveUser(user: User, masterData: MasterData): Long {
		val userId = existingUserId?.also {
			userDao.update(user)
		} ?: run {
			userDao.insert(user)
		}

		userDao.deleteUserData(userId)
		userDao.insertUserData(userId, masterData)
		return userId
	}

	private fun buildUntisApiUrl(schoolInfo: SchoolInfo): String {
		return if (advanced && !loginData.apiUrl.value.isNullOrBlank()) loginData.apiUrl.value ?: ""
		else if (schoolInfo.useMobileServiceUrlAndroid && !schoolInfo.mobileServiceUrl.isNullOrBlank()) schoolInfo.mobileServiceUrl!!
		else Uri.parse(schoolInfo.serverUrl).buildUpon().appendEncodedPath("jsonrpc_intern.do")
			.build().toString()
	}

	private suspend fun loadSchoolInfo(): SchoolInfo? {
		return schoolInfoFromSearch ?: run {
			if (advanced && !loginData.apiUrl.value.isNullOrBlank()) SchoolInfo(
				server = "",
				useMobileServiceUrlAndroid = true,
				useMobileServiceUrlIos = true,
				address = "",
				displayName = loginData.schoolId.value ?: "",
				loginName = loginData.schoolId.value ?: "",
				schoolId = loginData.schoolId.value?.toLongOrNull() ?: 0,
				serverUrl = loginData.apiUrl.value ?: "",
				mobileServiceUrl = loginData.apiUrl.value
			)
			else {
				val school = loginData.schoolId.value ?: ""
				val schoolId = school.toLongOrNull()

				val schoolSearchResult = schoolId?.let {
					schoolSearchApi.searchSchools(schoolId = it)
				} ?: schoolSearchApi.searchSchools(search = school)

				if (schoolSearchResult.size == 1) schoolSearchResult.schools.first()
				else
				// TODO: Show manual selection dialog when more than one results are returned.
				//       This workaround tries to find a matching school regardless.
					schoolSearchResult.schools.find { schoolInfoResult ->
						schoolInfoResult.schoolId == schoolId || schoolInfoResult.loginName.equals(
							school,
							true
						)
					}
			}
		}
	}

	/**
	 * This method tries to get the app secret from the supplied password.
	 * If the call fails, the password is assumed to be the app secret already and is returned directly.
	 */
	private suspend fun loadAppSharedSecret(untisApiUrl: String): String {
		if (loginData.anonymous.value == true) return ""

		if (useStoredPassword) return loginData.storedPassword!!

		return try {
			userDataApi.getAppSharedSecret(
				untisApiUrl, loginData.username.value ?: "", loginData.password.value ?: ""
			)
		} catch (e: UntisApiException) {
			// If we want to filter for some exceptions and throw others, we can implement it here
			/*if (e.error?.code != SOME_ERROR_CODE) throw e
			else */loginData.password.value ?: ""
		}
	}

	private suspend fun loadUserData(untisApiUrl: String, appSharedSecret: String): UserDataResult {
		val user = if (loginData.anonymous.value == true) null else loginData.username.value
		return userDataApi.getUserData(untisApiUrl, user, appSharedSecret)
	}

	fun goBack() {
		if (searchMode) searchMode = false
		else navigator.popBackStack()
	}

	fun onQrCodeErrorDialogDismiss() {
		showQrCodeErrorDialog = false
	}

	fun selectSchool(it: SchoolInfo) {
		loginData.schoolId.value = it.schoolId.toString()
		searchMode = false
	}

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
		var storedPassword: String? = null

		fun loadFromUser(user: User) {
			profileName.value = user.profileName
			schoolId.value = user.schoolId
			anonymous.value = user.anonymous
			username.value = user.user
			apiUrl.value = user.apiUrl
			storedPassword = user.key
		}
	}
}
