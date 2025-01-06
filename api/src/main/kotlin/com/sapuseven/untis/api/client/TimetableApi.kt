package com.sapuseven.untis.api.client

import com.sapuseven.untis.api.exception.UntisApiException
import com.sapuseven.untis.api.model.request.CreateImmediateAbsenceParams
import com.sapuseven.untis.api.model.request.DeleteAbsenceParams
import com.sapuseven.untis.api.model.request.PeriodDataParams
import com.sapuseven.untis.api.model.request.RequestData
import com.sapuseven.untis.api.model.request.SubmitAbsencesCheckedParams
import com.sapuseven.untis.api.model.request.SubmitLessonTopicParams
import com.sapuseven.untis.api.model.request.TimetableParams
import com.sapuseven.untis.api.model.response.BaseResponse
import com.sapuseven.untis.api.model.response.CreateImmediateAbsenceResponse
import com.sapuseven.untis.api.model.response.DeleteAbsenceResponse
import com.sapuseven.untis.api.model.response.PeriodDataResponse
import com.sapuseven.untis.api.model.response.PeriodDataResult
import com.sapuseven.untis.api.model.response.SubmitLessonTopicResponse
import com.sapuseven.untis.api.model.response.TimetableResponse
import com.sapuseven.untis.api.model.response.TimetableResult
import com.sapuseven.untis.api.model.untis.Auth
import com.sapuseven.untis.api.model.untis.absence.StudentAbsence
import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import com.sapuseven.untis.api.serializer.Time
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngineFactory
import kotlinx.serialization.json.Json
import java.time.LocalDate

open class TimetableApi(
	engineFactory: HttpClientEngineFactory<*>,
	config: ((HttpClientConfig<*>) -> Unit)? = null,
	jsonBlock: Json = DEFAULT_JSON
) : ApiClient(
	httpClientEngineFactory = engineFactory,
	httpClientConfig = config,
	jsonBlock = jsonBlock
) {
	open suspend fun getTimetable(
		apiUrl: String,
		id: Long,
		type: ElementType,
		startDate: LocalDate,
		endDate: LocalDate,
		masterDataTimestamp: Long,
		timetableTimestamp: Long = 0,
		timetableTimestamps: List<Long> = emptyList(),
		user: String?,
		key: String?
	): TimetableResult {
		val body = RequestData(
			method = METHOD_GET_TIMETABLE,
			params = listOf(
				TimetableParams(
					id = id,
					type = type.name,
					startDate = startDate,
					endDate = endDate,
					masterDataTimestamp = masterDataTimestamp,
					timetableTimestamp = timetableTimestamp,
					timetableTimestamps = timetableTimestamps,
					auth = Auth(user, key)
				)
			)
		)

		val response: TimetableResponse = request(apiUrl, body).body()

		return response.result ?: throw UntisApiException(response.error)
	}

	open suspend fun getPeriodData(
		apiUrl: String,
		periodIds: Set<Long>,
		user: String?,
		key: String?
	): PeriodDataResult {
		val body = RequestData(
			method = METHOD_GET_PERIOD_DATA,
			params = listOf(
				PeriodDataParams(
					ttIds = periodIds,
					auth = Auth(user, key)
				)
			)
		)

		val response: PeriodDataResponse = request(apiUrl, body).body()

		return response.result ?: throw UntisApiException(response.error)
	}

	/**
	 * This method is marked as "legacy" by Untis and might stop working in the future.
	 * Further investigation regarding a replacement method is needed.
	 */
	@Deprecated(message = "This is a \"legacy\" method and might stop working in the future")
	open suspend fun postLessonTopic(
		apiUrl: String,
		periodId: Long,
		lessonTopic: String,
		user: String?,
		key: String?
	): Boolean {
		val body = RequestData(
			method = METHOD_SUBMIT_LESSON_TOPIC,
			params = listOf(
				SubmitLessonTopicParams(
					ttId = periodId,
					lessonTopic = lessonTopic,
					auth = Auth(user, key)
				)
			)
		)

		val response: SubmitLessonTopicResponse = request(apiUrl, body).body()

		return response.result?.success ?: throw UntisApiException(response.error)
	}

	open suspend fun postAbsence(
		apiUrl: String,
		periodId: Long,
		studentId: Long,
		startTime: Time,
		endTime: Time,
		user: String?,
		key: String?
	): List<StudentAbsence> {
		val body = RequestData(
			method = METHOD_CREATE_IMMEDIATE_ABSENCE,
			params = listOf(
				CreateImmediateAbsenceParams(
					periodId = periodId,
					studentId = studentId,
					startTime = startTime,
					endTime = endTime,
					auth = Auth(user, key)
				)
			)
		)

		val response: CreateImmediateAbsenceResponse = request(apiUrl, body).body()

		return response.result?.absences ?: throw UntisApiException(response.error)
	}

	open suspend fun deleteAbsence(
		apiUrl: String,
		absenceId: Long,
		user: String?,
		key: String?
	): Boolean {
		val body = RequestData(
			method = METHOD_DELETE_ABSENCE,
			params = listOf(
				DeleteAbsenceParams(
					absenceId = absenceId,
					auth = Auth(user, key)
				)
			)
		)

		val response: DeleteAbsenceResponse = request(apiUrl, body).body()

		return response.result?.success ?: throw UntisApiException(response.error)
	}

	open suspend fun postAbsencesChecked(
		apiUrl: String,
		periodIds: Set<Long>,
		user: String?,
		key: String?
	) {
		val body = RequestData(
			method = METHOD_SUBMIT_ABSENCES_CHECKED,
			params = listOf(
				SubmitAbsencesCheckedParams(
					periodIds = periodIds,
					auth = Auth(user, key)
				)
			)
		)

		// Nothing is returned in the response
		val response: BaseResponse = request(apiUrl, body).body()

		response.error?.let { throw UntisApiException(it) }
	}
}
