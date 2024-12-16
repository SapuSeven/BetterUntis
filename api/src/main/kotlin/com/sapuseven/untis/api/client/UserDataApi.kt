package com.sapuseven.untis.api.client

import com.sapuseven.untis.api.exception.UntisApiException
import com.sapuseven.untis.api.model.request.AppSharedSecretParams
import com.sapuseven.untis.api.model.request.RequestData
import com.sapuseven.untis.api.model.request.UserDataParams
import com.sapuseven.untis.api.model.response.AppSharedSecretResponse
import com.sapuseven.untis.api.model.response.UserDataResponse
import com.sapuseven.untis.api.model.response.UserDataResult
import com.sapuseven.untis.api.model.untis.Auth
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngineFactory
import kotlinx.serialization.json.Json

open class UserDataApi(
	engineFactory: HttpClientEngineFactory<*>,
	config: ((HttpClientConfig<*>) -> Unit)? = null,
	jsonBlock: Json = ApiClient.DEFAULT_JSON
) : ApiClient(
	httpClientEngineFactory = engineFactory,
	httpClientConfig = config,
	jsonBlock = jsonBlock
) {
	open suspend fun loadAppSharedSecret(
		apiUrl: String,
		user: String,
		password: String
	): String {
		val body = RequestData(
			method = ApiClient.METHOD_GET_APP_SHARED_SECRET,
			params = listOf(AppSharedSecretParams(user, password))
		)

		val response: AppSharedSecretResponse = request(apiUrl, body).body()

		return response.result ?: throw UntisApiException(response.error)
	}

	open suspend fun loadUserData(
		apiUrl: String,
		user: String?,
		key: String?
	): UserDataResult {
		val body = RequestData(
			method = ApiClient.METHOD_GET_USER_DATA,
			params = listOf(UserDataParams(auth = Auth(user, key)))
		)

		val response: UserDataResponse = request(apiUrl, body).body()

		return response.result ?: throw UntisApiException(response.error)
	}
}
