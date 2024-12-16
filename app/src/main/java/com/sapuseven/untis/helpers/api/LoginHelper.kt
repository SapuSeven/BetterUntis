package com.sapuseven.untis.helpers.api

import com.sapuseven.untis.R
import com.sapuseven.untis.api.client.SchoolSearchApi
import com.sapuseven.untis.data.connectivity.UntisApiConstants
import com.sapuseven.untis.data.connectivity.UntisRequest
import com.sapuseven.untis.helpers.ErrorMessageDictionary
import com.sapuseven.untis.models.untis.params.AppSharedSecretParams
import com.sapuseven.untis.models.untis.response.AppSharedSecretResponse
import io.ktor.client.engine.cio.CIO
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
	private val schoolSearchApi = SchoolSearchApi(CIO)

	init {
		onStatusUpdate(R.string.logindatainput_connecting)
	}

	@ExperimentalSerializationApi
	/*suspend fun loadSchoolInfo(school: String): SchoolInfo? {
		onStatusUpdate(R.string.logindatainput_aquiring_schoolid)

		val schoolId = school.toIntOrNull()
		val schoolSearchResult = (
			if (schoolId != null) schoolSearchApi.searchSchools(schoolid = schoolId)
			else schoolSearchApi.searchSchools(search = school)
		)

		try {
			schoolSearchResult.fold({
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
			}, {
				onError(
					LoginErrorInfo(
						errorCode = it.code,
						errorMessage = it.message
					)
				)
			})
		} catch (e: SerializationException) {
			onError(
				LoginErrorInfo(
					errorMessageStringRes = R.string.all_error_details,
					errorMessage = e.message
				)
			)
		}
		/*}, { error ->
			onError(
				LoginErrorInfo(
					errorMessageStringRes = R.string.all_error_details,
					errorMessage = error.message
				)
			)
		})*/

		return null
	}*/

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

	/*suspend fun loadUserData(apiUrl: String, key: String?): UserDataResult? {
		onStatusUpdate(R.string.logindatainput_loading_user_data)

		val query = UntisRequest.UntisRequestQuery()

		query.url = apiUrl
		query.proxyHost = proxyHost
		query.data.method = UntisApiConstants.METHOD_GET_USER_DATA

		if (loginData.anonymous)
			query.data.params =
				listOf(UserDataParams(auth = Authentication.createAuthObject()))
		else {
			if (key == null) return null
			query.data.params = listOf(
				UserDataParams(
					auth = Authentication.createAuthObject(
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
	}*/
}
