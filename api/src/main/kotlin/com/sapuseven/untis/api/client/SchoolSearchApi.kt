package com.sapuseven.untis.api.client

import com.sapuseven.untis.api.model.request.SchoolSearchParams
import com.sapuseven.untis.api.model.request.UntisRequestData
import com.sapuseven.untis.api.model.response.SchoolSearchResponse
import com.sapuseven.untis.api.model.response.SchoolSearchResult
import com.sapuseven.untis.api.model.response.UntisResult
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.http.HttpMethod
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

/*
HttpClient(engine) {
		install(ContentNegotiation)
		defaultRequest {
			url(DEFAULT_SCHOOLSEARCH_URL)
			header("Content-Type", "application/json; charset=UTF-8")
		}
	}
	*/
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
		val query = mutableMapOf<String, List<String>>()
		val headers = mutableMapOf<String, String>()
		val config = RequestConfig<Any?>(
			HttpMethod.Post,
			DEFAULT_SCHOOLSEARCH_URL,
			query = query,
			headers = headers,
			requiresAuthentication = false
		)
		val body = UntisRequestData(
			id = "untis-mobile-android",
			method = ApiClient.METHOD_SEARCH_SCHOOLS,
			params = listOf(SchoolSearchParams(search, schoolid, schoolname))
		)

		val response: SchoolSearchResponse = request(
			config,
			body
		).body()

		return UntisResult.of(response.result) { response.error!! }
	}
}
