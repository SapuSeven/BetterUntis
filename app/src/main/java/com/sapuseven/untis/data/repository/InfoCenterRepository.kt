package com.sapuseven.untis.data.repository

import com.sapuseven.untis.api.client.AbsenceApi
import com.sapuseven.untis.api.client.ClassRegApi
import com.sapuseven.untis.api.client.MessagesApi
import com.sapuseven.untis.api.client.OfficeHoursApi
import com.sapuseven.untis.api.model.untis.MessageOfDay
import com.sapuseven.untis.api.model.untis.absence.StudentAbsence
import com.sapuseven.untis.api.model.untis.classreg.Exam
import com.sapuseven.untis.api.model.untis.classreg.HomeWork
import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import com.sapuseven.untis.api.model.untis.timetable.OfficeHour
import com.sapuseven.untis.data.cache.DiskCache
import com.sapuseven.untis.data.database.entities.User
import com.sapuseven.untis.scope.UserScopeManager
import crocodile8.universal_cache.CachedSource
import crocodile8.universal_cache.time.TimeProvider
import kotlinx.serialization.serializer
import java.io.File
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Named

interface InfoCenterRepository {
	fun messagesOfDaySource(): CachedSource<LocalDate, List<MessageOfDay>>

	fun examsSource(): CachedSource<EventsParams, List<Exam>>

	fun homeworkSource(): CachedSource<EventsParams, List<HomeWork>>

	fun absencesSource(): CachedSource<AbsencesParams, List<StudentAbsence>>

	fun officeHoursSource(): CachedSource<OfficeHoursParams, List<OfficeHour>>

	data class EventsParams(
		val elementId: Long,
		val elementType: ElementType,
		val startDate: LocalDate,
		val endDate: LocalDate
	)

	data class AbsencesParams(
		val startDate: LocalDate,
		val endDate: LocalDate,
		val includeExcused: Boolean = true,
		val includeUnExcused: Boolean = true
	)

	data class OfficeHoursParams(
		val klasseId: Long,
		val startDate: LocalDate
	)
}

class UntisInfoCenterRepository @Inject constructor(
	private val messagesApi: MessagesApi,
	private val classRegApi: ClassRegApi,
	private val absenceApi: AbsenceApi,
	private val officeHoursApi: OfficeHoursApi,
	@Named("cacheDir") private val cacheDir: File,
	private val timeProvider: TimeProvider,
	userScopeManager: UserScopeManager
) : InfoCenterRepository {
	private val user: User = userScopeManager.user

	override fun messagesOfDaySource(): CachedSource<LocalDate, List<MessageOfDay>> {
		return CachedSource(
			source = { params ->
				messagesApi.getMessagesOfDay(
					date = params,
					apiUrl = user.apiUrl,
					user = user.user,
					key = user.key
				).messages
			},
			cache = DiskCache(File(cacheDir, "infocenter/messages"), serializer()),
			timeProvider = timeProvider
		)
	}

	override fun examsSource(): CachedSource<InfoCenterRepository.EventsParams, List<Exam>> {
		return CachedSource(
			source = { params ->
				classRegApi.getExams(
					id = params.elementId,
					type = params.elementType,
					startDate = params.startDate,
					endDate = params.endDate,
					apiUrl = user.apiUrl,
					user = user.user,
					key = user.key
				).exams
			},
			cache = DiskCache(File(cacheDir, "infocenter/exams"), serializer()),
			timeProvider = timeProvider
		)
	}

	override fun homeworkSource(): CachedSource<InfoCenterRepository.EventsParams, List<HomeWork>> {
		return CachedSource(
			source = { params ->
				classRegApi.getHomework(
					id = params.elementId,
					type = params.elementType,
					startDate = params.startDate,
					endDate = params.endDate,
					apiUrl = user.apiUrl,
					user = user.user,
					key = user.key
				).homeWorks
			},
			cache = DiskCache(File(cacheDir, "infocenter/homework"), serializer()),
			timeProvider = timeProvider
		)
	}

	override fun absencesSource(): CachedSource<InfoCenterRepository.AbsencesParams, List<StudentAbsence>> {
		return CachedSource(
			source = { params ->
				absenceApi.getStudentAbsences(
					startDate = params.startDate,
					endDate = params.endDate,
					includeExcused = params.includeExcused,
					includeUnExcused = params.includeUnExcused,
					apiUrl = user.apiUrl,
					user = user.user,
					key = user.key
				).absences
			},
			cache = DiskCache(File(cacheDir, "infocenter/homework"), serializer()),
			timeProvider = timeProvider
		)
	}

	override fun officeHoursSource(): CachedSource<InfoCenterRepository.OfficeHoursParams, List<OfficeHour>> {
		return CachedSource(
			source = { params ->
				officeHoursApi.getOfficeHours(
					klasseId = params.klasseId,
					startDate = params.startDate,
					apiUrl = user.apiUrl,
					user = user.user,
					key = user.key
				).officeHours
			},
			cache = DiskCache(File(cacheDir, "infocenter/officehours"), serializer()),
			timeProvider = timeProvider
		)
	}
}
