package com.sapuseven.untis.api.client

import com.sapuseven.untis.api.model.request.SchoolSearchParams
import com.sapuseven.untis.api.model.request.UntisRequestData
import com.sapuseven.untis.api.model.response.SchoolSearchResponse
import com.sapuseven.untis.api.model.response.SchoolSearchResult
import com.sapuseven.untis.api.model.response.UntisResult
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.utils.EmptyContent.headers
import io.ktor.http.HttpMethod
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

open class SchoolSearchApi(
	engineFactory: HttpClientEngineFactory<*>,
	config: ((HttpClientConfig<*>) -> Unit)? = null,
	jsonBlock: Json = ApiClient.DEFAULT_JSON
) : ApiClient(
	httpClientEngineFactory = engineFactory,
	httpClientConfig = config,
	jsonBlock = jsonBlock
) {
	@OptIn(ExperimentalSerializationApi::class)
	open suspend fun searchSchools(
		search: String? = null,
		schoolid: Int = 0,
		schoolname: String = ""
	): UntisResult<SchoolSearchResult> {
		val config = RequestConfig<Any?>(
			HttpMethod.Post,
			DEFAULT_SCHOOLSEARCH_URL,
			requiresAuthentication = false
		)
		val body = UntisRequestData(
			method = ApiClient.METHOD_SEARCH_SCHOOLS,
			params = listOf(SchoolSearchParams(search, schoolid, schoolname))
		)

		val response: SchoolSearchResponse = request(config, body).body()

		return UntisResult.of(response.result) { response.error!! }
	}
}
