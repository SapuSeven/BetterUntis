package com.sapuseven.untis.data.repository

import com.sapuseven.untis.api.client.TimetableApi
import com.sapuseven.untis.api.model.response.PeriodDataResult
import com.sapuseven.untis.api.model.untis.absence.StudentAbsence
import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import com.sapuseven.untis.api.model.untis.timetable.Period
import com.sapuseven.untis.data.cache.DiskCache
import com.sapuseven.untis.data.database.entities.User
import com.sapuseven.untis.scope.UserScopeManager
import crocodile8.universal_cache.CachedSource
import crocodile8.universal_cache.time.TimeProvider
import kotlinx.serialization.serializer
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Named

interface TimetableRepository {
	fun timetableSource(): CachedSource<TimetableParams, List<Period>>
	fun periodDataSource(): CachedSource<Set<Period>, PeriodDataResult>

	suspend fun postLessonTopic(periodId: Long, lessonTopic: String): Result<Boolean>

	suspend fun postAbsence(
		periodId: Long, studentId: Long, startTime: LocalTime, endTime: LocalTime
	): Result<List<StudentAbsence>>

	suspend fun deleteAbsence(absenceId: Long): Result<Boolean>

	suspend fun postAbsencesChecked(periodIds: Set<Long>): Result<Unit>

	data class TimetableParams(
		val elementId: Long,
		val elementType: ElementType,
		val startDate: LocalDate,
		val endDate: LocalDate = startDate.plusDays(5 /*TODO*/)
	)
}

class UntisTimetableRepository @Inject constructor(
	private val api: TimetableApi,
	@Named("cacheDir") private val cacheDir: File,
	private val timeProvider: TimeProvider,
	userScopeManager: UserScopeManager
) : TimetableRepository {
	private val user: User = userScopeManager.user

	override fun timetableSource(): CachedSource<TimetableRepository.TimetableParams, List<Period>> {
		return CachedSource(
			source = { params ->
				api.getTimetable(
					id = params.elementId,
					type = params.elementType,
					startDate = params.startDate,
					endDate = params.endDate,
					masterDataTimestamp = user.masterDataTimestamp,
					apiUrl = user.apiUrl,
					user = user.user,
					key = user.key
				).timetable.periods
			},
			cache = DiskCache(File(cacheDir, "timetable"), serializer()),
			timeProvider = timeProvider
		)
	}

	override fun periodDataSource(): CachedSource<Set<Period>, PeriodDataResult> {
		return CachedSource(
			source = { params ->
				api.getPeriodData(
					periodIds = params.map { it.id }.toSet(),
					apiUrl = user.apiUrl,
					user = user.user,
					key = user.key
				)
			},
			cache = DiskCache(File(cacheDir, "periodData"), serializer()),
			timeProvider = timeProvider
		)
	}

	override suspend fun postLessonTopic(periodId: Long, lessonTopic: String) = runCatching {
		api.postLessonTopic(
			periodId = periodId,
			lessonTopic = lessonTopic,
			apiUrl = user.apiUrl,
			user = user.user,
			key = user.key
		)
	}

	override suspend fun postAbsence(periodId: Long, studentId: Long, startTime: LocalTime, endTime: LocalTime) =
		runCatching {
			api.postAbsence(
				periodId = periodId,
				studentId = studentId,
				startTime = startTime,
				endTime = endTime,
				apiUrl = user.apiUrl,
				user = user.user,
				key = user.key
			)
		}

	override suspend fun deleteAbsence(absenceId: Long) = runCatching {
		api.deleteAbsence(
			absenceId = absenceId, apiUrl = user.apiUrl, user = user.user, key = user.key
		)
	}

	override suspend fun postAbsencesChecked(periodIds: Set<Long>) = runCatching {
		api.postAbsencesChecked(
			periodIds = periodIds, apiUrl = user.apiUrl, user = user.user, key = user.key
		)
	}
}
