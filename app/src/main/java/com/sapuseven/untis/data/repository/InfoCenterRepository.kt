package com.sapuseven.untis.data.repository

import com.sapuseven.untis.api.client.ClassRegApi
import com.sapuseven.untis.api.client.MessagesApi
import com.sapuseven.untis.api.model.untis.MessageOfDay
import com.sapuseven.untis.api.model.untis.classreg.Exam
import com.sapuseven.untis.api.model.untis.classreg.HomeWork
import com.sapuseven.untis.api.model.untis.enumeration.ElementType
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

	fun examsSource(): CachedSource<ClassRegParams, List<Exam>>

	fun homeworksSource(): CachedSource<ClassRegParams, List<HomeWork>>

	data class ClassRegParams(
		val elementId: Long,
		val elementType: ElementType,
		val startDate: LocalDate,
		val endDate: LocalDate = startDate.plusDays(5 /*TODO*/)
	)
}

class UntisInfoCenterRepository @Inject constructor(
	private val messagesApi: MessagesApi,
	private val classRegApi: ClassRegApi,
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
			cache = DiskCache(File(cacheDir, "infocenter"), serializer()),
			timeProvider = timeProvider
		)
	}

	override fun examsSource(): CachedSource<InfoCenterRepository.ClassRegParams, List<Exam>> {
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
			cache = DiskCache(File(cacheDir, "infocenter"), serializer()),
			timeProvider = timeProvider
		)
	}

	override fun homeworksSource(): CachedSource<InfoCenterRepository.ClassRegParams, List<HomeWork>> {
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
			cache = DiskCache(File(cacheDir, "infocenter"), serializer()),
			timeProvider = timeProvider
		)
	}
}
