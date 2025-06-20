package com.sapuseven.untis.api.client

import com.sapuseven.untis.api.exception.UntisApiException
import com.sapuseven.untis.api.model.request.OfficeHoursParams
import com.sapuseven.untis.api.model.request.RequestData
import com.sapuseven.untis.api.model.request.StudentAbsencesParams
import com.sapuseven.untis.api.model.response.OfficeHoursResponse
import com.sapuseven.untis.api.model.response.OfficeHoursResult
import com.sapuseven.untis.api.model.response.StudentAbsencesResponse
import com.sapuseven.untis.api.model.response.StudentAbsencesResult
import com.sapuseven.untis.api.model.untis.Auth
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngineFactory
import kotlinx.serialization.json.Json
import java.time.LocalDate

open class OfficeHoursApi(
	engineFactory: HttpClientEngineFactory<*>,
	config: ((HttpClientConfig<*>) -> Unit)? = null,
	jsonBlock: Json = DEFAULT_JSON
) : ApiClient(
	httpClientEngineFactory = engineFactory,
	httpClientConfig = config,
	jsonBlock = jsonBlock
) {
	open suspend fun getOfficeHours(
		apiUrl: String,
		klasseId: Long,
		startDate: LocalDate,
		user: String?,
		key: String?
	): OfficeHoursResult {
		val body = RequestData(
			method = METHOD_GET_OFFICEHOURS,
			params = listOf(
				OfficeHoursParams(
					klasseId = klasseId,
					startDate = startDate,
					auth = Auth(user, key)
				)
			)
		)

		val response: OfficeHoursResponse = request(apiUrl, body).body()

		return response.result ?: throw UntisApiException(response.error)
	}
}
