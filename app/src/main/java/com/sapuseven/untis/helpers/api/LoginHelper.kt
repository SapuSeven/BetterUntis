package com.sapuseven.untis.helpers.api

import com.sapuseven.untis.R
import com.sapuseven.untis.data.connectivity.UntisApiConstants
import com.sapuseven.untis.data.connectivity.UntisAuthentication
import com.sapuseven.untis.data.connectivity.UntisRequest
import com.sapuseven.untis.helpers.ErrorMessageDictionary
import com.sapuseven.untis.models.UntisSchoolInfo
import com.sapuseven.untis.models.untis.params.AppSharedSecretParams
import com.sapuseven.untis.models.untis.params.SchoolSearchParams
import com.sapuseven.untis.models.untis.params.UserDataParams
import com.sapuseven.untis.models.untis.response.AppSharedSecretResponse
import com.sapuseven.untis.models.untis.response.SchoolSearchResponse
import com.sapuseven.untis.models.untis.response.UserDataResponse
import com.sapuseven.untis.models.untis.response.UserDataResult
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import java.net.UnknownHostException

class LoginHelper(
	val loginData: LoginDataInfo,
	val proxyHost: String? = null,
	val onStatusUpdate: (statusStringRes: Int) -> Unit,
	val onError: (error: LoginErrorInfo) -> Unit
) {
	private val api: UntisRequest = UntisRequest()

	init {
		onStatusUpdate(R.string.logindatainput_connecting)
	}

	@ExperimentalSerializationApi
	suspend fun loadSchoolInfo(school: String): UntisSchoolInfo? {
		onStatusUpdate(R.string.logindatainput_aquiring_schoolid)

		val schoolId = school.toIntOrNull()
		val query = UntisRequest.UntisRequestQuery()

		query.data.method = UntisApiConstants.METHOD_SEARCH_SCHOOLS
		query.url = UntisApiConstants.SCHOOL_SEARCH_URL
		query.proxyHost = proxyHost
		query.data.params =
			if (schoolId != null) listOf(SchoolSearchParams(schoolid = schoolId))
			else listOf(SchoolSearchParams(search = school))

		api.request<SchoolSearchResponse>(query).fold({ untisResponse ->
			try {
				untisResponse.result?.let {
					if (it.schools.isNotEmpty()) {
						val schoolResult =
							if (it.schools.size == 1)
								it.schools.first()
							else
							// TODO: Show manual selection dialog when more than one results are returned
								it.schools.find { schoolInfoResult ->
									schoolInfoResult.schoolId == schoolId || schoolInfoResult.loginName.equals(
										school,
										true
									)
								}

						if (schoolResult != null)
							return schoolResult
					}
					onError(LoginErrorInfo(errorMessageStringRes = R.string.logindatainput_error_invalid_school))
				} ?: run {
					onError(
						LoginErrorInfo(
							errorCode = untisResponse.error?.code,
							errorMessage = untisResponse.error?.message
						)
					)
				}
			} catch (e: SerializationException) {
				onError(
					LoginErrorInfo(
						errorMessageStringRes = R.string.all_error_details,
						errorMessage = e.message
					)
				)
			}
		}, { error ->
			onError(
				LoginErrorInfo(
					errorMessageStringRes = R.string.all_error_details,
					errorMessage = error.message
				)
			)
		})

		return null
	}

	suspend fun loadAppSharedSecret(apiUrl: String): String? {
		onStatusUpdate(R.string.logindatainput_aquiring_app_secret)

		val query = UntisRequest.UntisRequestQuery()

		query.url = apiUrl
		query.proxyHost = proxyHost
		query.data.method = UntisApiConstants.METHOD_GET_APP_SHARED_SECRET
		query.data.params = listOf(AppSharedSecretParams(loginData.user, loginData.password))

		api.request<AppSharedSecretResponse>(query).fold({ untisResponse ->
			try {
				if (untisResponse.error?.code == ErrorMessageDictionary.ERROR_CODE_INVALID_CREDENTIALS)
					return loginData.password
				if (untisResponse.result.isNullOrEmpty())
					onError(
						LoginErrorInfo(
							errorCode = untisResponse.error?.code,
							errorMessage = untisResponse.error?.message
						)
					)
				else
					return untisResponse.result
			} catch (e: SerializationException) {
				onError(
					LoginErrorInfo(
						errorMessageStringRes = R.string.all_error_details,
						errorMessage = e.message
					)
				)
			}
		}, { error ->
			when (error.exception) {
				is UnknownHostException -> onError(LoginErrorInfo(errorCode = ErrorMessageDictionary.ERROR_CODE_NO_SERVER_FOUND))
				else -> onError(
					LoginErrorInfo(
						errorMessageStringRes = R.string.all_error_details,
						errorMessage = error.message
					)
				)
			}
		})

		return null
	}

	suspend fun loadUserData(apiUrl: String, key: String?): UserDataResult? {
		onStatusUpdate(R.string.logindatainput_loading_user_data)

		val query = UntisRequest.UntisRequestQuery()

		query.url = apiUrl
		query.proxyHost = proxyHost
		query.data.method = UntisApiConstants.METHOD_GET_USER_DATA

		if (loginData.anonymous)
			query.data.params =
				listOf(UserDataParams(auth = UntisAuthentication.createAuthObject()))
		else {
			if (key == null) return null
			query.data.params = listOf(
				UserDataParams(
					auth = UntisAuthentication.createAuthObject(
						loginData.user,
						key
					)
				)
			)
		}

		api.request<UserDataResponse>(query).fold({ untisResponse ->
			try {
				if (untisResponse.result != null) {
					return untisResponse.result
				} else {
					onError(
						LoginErrorInfo(
							errorCode = untisResponse.error?.code,
							errorMessage = untisResponse.error?.message
						)
					)
				}
			} catch (e: SerializationException) {
				onError(
					LoginErrorInfo(
						errorMessageStringRes = R.string.all_error_details,
						errorMessage = e.message
					)
				)
			}
		}, { error ->
			onError(
				LoginErrorInfo(
					errorMessageStringRes = R.string.all_error_details,
					errorMessage = error.message
				)
			)
		})

		return null
	}
}
