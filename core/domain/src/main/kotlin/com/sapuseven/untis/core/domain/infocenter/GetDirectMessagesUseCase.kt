package com.sapuseven.untis.core.domain.infocenter

import com.sapuseven.untis.core.domain.repository.DirectMessageRepository
import com.sapuseven.untis.core.model.messages.DirectMessage
import com.sapuseven.untis.core.model.user.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetDirectMessagesUseCase @Inject constructor(
	private val directMessageRepository: DirectMessageRepository,
) {
	operator fun invoke(user: User): Flow<Result<List<DirectMessage>>> =
		directMessageRepository.getMessages(user)
			.map(Result.Companion::success)
			.catch { emit(Result.failure(it)) }

	operator fun invoke(user: User, messageId: Long): Flow<Result<DirectMessage>> =
		directMessageRepository.getMessage(user, messageId)
			.map(Result.Companion::success)
			.catch { emit(Result.failure(it)) }
}
