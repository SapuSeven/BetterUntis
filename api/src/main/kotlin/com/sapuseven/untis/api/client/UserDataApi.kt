package com.sapuseven.untis.api.client

import com.sapuseven.untis.api.model.request.RequestData
import com.sapuseven.untis.api.model.request.UserDataParams
import com.sapuseven.untis.api.model.response.UntisResult
import com.sapuseven.untis.api.model.response.UserDataResponse
import com.sapuseven.untis.api.model.response.UserDataResult
import com.sapuseven.untis.api.model.untis.Auth
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.http.HttpMethod
import kotlinx.serialization.ExperimentalSerializationApi
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
	open suspend fun loadUserData(
		apiUrl: String,
		user: String?,
		key: String?
	): UntisResult<UserDataResult> {
		val config = RequestConfig<Any?>(
			HttpMethod.Post,
			apiUrl,
			Auth(user, key)
		)
		val body = RequestData(
			method = ApiClient.METHOD_GET_USER_DATA,
			params = listOf(UserDataParams())
		)

		val response: UserDataResponse = request(config, body).body()

		return UntisResult.of(response.result) { response.error!! }
	}
}
