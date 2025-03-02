package com.sapuseven.untis.api.client

import com.sapuseven.untis.api.exception.UntisApiException
import com.sapuseven.untis.api.model.request.ExamsParams
import com.sapuseven.untis.api.model.request.HomeworkParams
import com.sapuseven.untis.api.model.request.RequestData
import com.sapuseven.untis.api.model.response.ExamsResponse
import com.sapuseven.untis.api.model.response.ExamsResult
import com.sapuseven.untis.api.model.response.HomeworkResponse
import com.sapuseven.untis.api.model.response.HomeworkResult
import com.sapuseven.untis.api.model.untis.Auth
import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngineFactory
import kotlinx.serialization.json.Json
import java.time.LocalDate

open class ClassRegApi(
	engineFactory: HttpClientEngineFactory<*>,
	config: ((HttpClientConfig<*>) -> Unit)? = null,
	jsonBlock: Json = DEFAULT_JSON
) : ApiClient(
	httpClientEngineFactory = engineFactory,
	httpClientConfig = config,
	jsonBlock = jsonBlock
) {
	open suspend fun getExams(
		apiUrl: String,
		id: Long,
		type: ElementType,
		startDate: LocalDate,
		endDate: LocalDate,
		user: String?,
		key: String?
	): ExamsResult {
		val body = RequestData(
			method = METHOD_GET_EXAMS,
			params = listOf(
				ExamsParams(
					id = id,
					type = type,
					startDate = startDate,
					endDate = endDate,
					auth = Auth(user, key)
				)
			)
		)

		val response: ExamsResponse = request(apiUrl, body).body()

		return response.result ?: throw UntisApiException(response.error)
	}

	open suspend fun getHomework(
		apiUrl: String,
		id: Long,
		type: ElementType,
		startDate: LocalDate,
		endDate: LocalDate,
		user: String?,
		key: String?
	): HomeworkResult {
		val body = RequestData(
			method = METHOD_GET_HOMEWORK,
			params = listOf(
				HomeworkParams(
					id = id,
					type = type,
					startDate = startDate,
					endDate = endDate,
					auth = Auth(user, key)
				)
			)
		)

		val response: HomeworkResponse = request(apiUrl, body).body()

		return response.result ?: throw UntisApiException(response.error)
	}
}
