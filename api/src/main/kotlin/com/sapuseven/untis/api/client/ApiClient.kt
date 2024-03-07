package com.sapuseven.untis.api.client

import com.sapuseven.untis.api.model.request.RequestData
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.utils.EmptyContent.contentType
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

open class ApiClient() {
	private lateinit var client: HttpClient

	constructor(
		httpClientEngine: HttpClientEngine?,
		httpClientConfig: ((HttpClientConfig<*>) -> Unit)? = null,
		jsonBlock: Json,
	) : this() {
		val clientConfig: (HttpClientConfig<*>) -> Unit by lazy {
			{
				it.install(ContentNegotiation) {
					json(jsonBlock, contentType = ContentType.Application.Json)
					json(jsonBlock, contentType = ContentType.Application.JsonRpc)
				}
				it.install(HttpRequestRetry) {
					retryOnServerErrors(maxRetries = 3)
					retryOnException(maxRetries = 3)
					exponentialDelay()
				}
				httpClientConfig?.invoke(it)
			}
		}

		client = httpClientEngine?.let { HttpClient(it, clientConfig) } ?: HttpClient(clientConfig)
	}

	constructor(
		httpClientEngineFactory: HttpClientEngineFactory<*>?,
		httpClientConfig: ((HttpClientConfig<*>) -> Unit)? = null,
		jsonBlock: Json,
	) : this(
		httpClientEngineFactory?.create(), httpClientConfig, jsonBlock
	)

	constructor(
		httpClient: HttpClient
	) : this() {
		this.client = httpClient
	}

	protected suspend fun request(
		path: String, body: RequestData? = null
	): HttpResponse = withContext(Dispatchers.IO) {
		return@withContext client.post(path) {
			contentType(ContentType.Application.Json)
			setBody(body)
		}
	}

	private val ContentType.Application.JsonRpc: ContentType
		get() = ContentType("application", "json-rpc")

	companion object {
		const val DEFAULT_WEBUNTIS_HOST = "mobile.webuntis.com"
		const val DEFAULT_WEBUNTIS_PATH = "/ms/app/"
		const val DEFAULT_SCHOOLSEARCH_URL = "https://schoolsearch.webuntis.com/schoolquery2"
		val DEFAULT_JSON = Json {
			ignoreUnknownKeys = true
			isLenient = true
			encodeDefaults = true
			prettyPrint = true // TODO only for DEV
		}

		protected val UNSAFE_HEADERS = listOf(HttpHeaders.ContentType)

		const val METHOD_CREATE_IMMEDIATE_ABSENCE = "createImmediateAbsence2017"
		const val METHOD_DELETE_ABSENCE = "deleteAbsence2017"
		const val METHOD_GET_ABSENCES = "getStudentAbsences2017"
		const val METHOD_GET_APP_SHARED_SECRET = "getAppSharedSecret"
		const val METHOD_GET_EXAMS = "getExams2017"
		const val METHOD_GET_HOMEWORKS = "getHomeWork2017"
		const val METHOD_GET_MESSAGES = "getMessagesOfDay2017"
		const val METHOD_GET_OFFICEHOURS = "getOfficeHours2017"
		const val METHOD_GET_PERIOD_DATA = "getPeriodData2017"
		const val METHOD_GET_TIMETABLE = "getTimetable2017"
		const val METHOD_GET_USER_DATA = "getUserData2017"
		const val METHOD_SEARCH_SCHOOLS = "searchSchool"
		const val METHOD_SUBMIT_ABSENCES_CHECKED = "submitAbsencesChecked2017"
		const val METHOD_GET_LESSON_TOPIC = "getLessonTopic2017"
		const val METHOD_SUBMIT_LESSON_TOPIC = "submitLessonTopic"

		const val CAN_READ_STUDENT_ABSENCE = "READ_STUD_ABSENCE"
		const val CAN_WRITE_STUDENT_ABSENCE = "WRITE_STUD_ABSENCE"
		const val CAN_READ_LESSON_TOPIC = "READ_LESSONTOPIC"
		const val CAN_WRITE_LESSON_TOPIC = "WRITE_LESSONTOPIC"
		const val CAN_READ_HOMEWORK = "READ_HOMEWORK"
		const val CAN_WRITE_HOMEWORK = "WRITE_HOMEWORK"
		const val CAN_READ_CLASSREG_EVENT = "READ_CLASSREGEVENT"
		const val CAN_WRITE_CLASSREG_EVENT = "WRITE_CLASSREGEVENT"
		const val CAN_DELETE_CLASSREG_EVENT = "DELETE_CLASSREGEVENT"
		const val CAN_READ_CLASS_ROLE = "READ_CLASSROLE"
		const val CAN_READ_PERIOD_INFO = "READ_PERIODINFO"
		const val CAN_WRITE_PERIOD_INFO = "WRITE_PERIODINFO"
		const val CAN_ACTION_CHANGE_ROOM = "ACTION_CHANGE_ROOM"

		const val RIGHT_OFFICEHOURS = "R_OFFICEHOURS"
		const val RIGHT_ABSENCES = "R_MY_ABSENCES"
		const val RIGHT_CLASSREGISTER = "CLASSREGISTER"
	}
}
