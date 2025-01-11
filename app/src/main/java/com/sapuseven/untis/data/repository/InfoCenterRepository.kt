package com.sapuseven.untis.data.repository

import com.sapuseven.untis.api.client.MessagesApi
import com.sapuseven.untis.api.model.untis.MessageOfDay
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
}

class UntisInfoCenterRepository @Inject constructor(
	private val api: MessagesApi,
	@Named("cacheDir") private val cacheDir: File,
	private val timeProvider: TimeProvider,
	userScopeManager: UserScopeManager
) : InfoCenterRepository {
	private val user: User = userScopeManager.user

	override fun messagesOfDaySource(): CachedSource<LocalDate, List<MessageOfDay>> {
		return CachedSource(
			source = { params ->
				api.getMessagesOfDay(
					date = params,
					apiUrl = user.apiUrl,
					user = user.user,
					key = user.key
				).messages
			},
			cache = DiskCache(File(cacheDir, "timetable"), serializer()),
			timeProvider = timeProvider
		)
	}
}
