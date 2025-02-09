package com.sapuseven.untis.domain

import com.sapuseven.untis.api.model.untis.MessageOfDay
import com.sapuseven.untis.data.database.entities.User
import com.sapuseven.untis.data.repository.InfoCenterRepository
import com.sapuseven.untis.scope.UserScopeManager
import crocodile8.universal_cache.FromCache
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

interface GetMessagesUseCase {
	operator fun invoke(): Flow<Result<List<MessageOfDay>>>
}

class GetMessagesUseCaseImpl @Inject constructor(
	private val infoCenterRepository: InfoCenterRepository,
	userScopeManager: UserScopeManager
) : GetMessagesUseCase {
	private val user: User = userScopeManager.user

	companion object {
		private const val ONE_HOUR: Long = 60 * 60 * 1000
	}

	override operator fun invoke(): Flow<Result<List<MessageOfDay>>> = infoCenterRepository.messagesOfDaySource()
		.get(LocalDate.now(), FromCache.CACHED_THEN_LOAD, maxAge = ONE_HOUR, additionalKey = user.id)
		.map(Result.Companion::success)
		.catch { emit(Result.failure(it)) }
}
