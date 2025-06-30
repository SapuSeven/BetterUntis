package com.sapuseven.untis.domain

import com.sapuseven.untis.data.repository.MessagesRepository
import com.sapuseven.untis.data.repository.UserRepository
import com.sapuseven.untis.model.rest.Message
import crocodile8.universal_cache.FromCache
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface GetMessagesUseCase {
	operator fun invoke(): Flow<Result<List<Message>>>

	operator fun invoke(id: Long): Flow<Result<Message>>
}

class GetMessagesUseCaseImpl @Inject constructor(
	private val userRepository: UserRepository,
	private val messagesRepository: MessagesRepository,
) : GetMessagesUseCase {
	companion object {
		private const val ONE_HOUR: Long = 60 * 60 * 1000
	}

	override operator fun invoke(): Flow<Result<List<Message>>> = messagesRepository.messagesSource()
		.get(Unit, FromCache.CACHED_THEN_LOAD, maxAge = ONE_HOUR, additionalKey = userRepository.currentUser!!.id)
		.map { (it.incomingMessages ?: emptyList()) + (it.readConfirmationMessages ?: emptyList()) }
		.map(Result.Companion::success)
		.catch { emit(Result.failure(it)) }

	override operator fun invoke(id: Long): Flow<Result<Message>> = messagesRepository.messageSource()
		.get(id, FromCache.IF_HAVE, maxAge = ONE_HOUR, additionalKey = userRepository.currentUser!!.id)
		.map(Result.Companion::success)
		.catch { emit(Result.failure(it)) }
}
