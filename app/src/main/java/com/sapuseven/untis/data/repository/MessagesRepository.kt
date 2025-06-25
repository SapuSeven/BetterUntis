package com.sapuseven.untis.data.repository

import com.sapuseven.untis.data.cache.DiskCache
import com.sapuseven.untis.model.rest.Message
import com.sapuseven.untis.model.rest.MessagesResponse
import com.sapuseven.untis.modules.MessagesApiFactory
import crocodile8.universal_cache.CachedSource
import crocodile8.universal_cache.time.TimeProvider
import kotlinx.serialization.serializer
import java.io.File
import javax.inject.Inject
import javax.inject.Named

interface MessagesRepository {
	fun messagesSource(): CachedSource<Unit, MessagesResponse>

	fun messagesSentSource(): CachedSource<Unit, List<Message>>

	fun messagesDraftsSource(): CachedSource<Unit, List<Message>>
}

class UntisMessagesRepository @Inject constructor(
	private val userRepository: UserRepository,
	private val messagesApiFactory: MessagesApiFactory,
	@Named("cacheDir") private val cacheDir: File,
	private val timeProvider: TimeProvider,
) : MessagesRepository {
	override fun messagesSource(): CachedSource<Unit, MessagesResponse> {
		val user = userRepository.currentUser!!
		return CachedSource(
			source = { params ->
				messagesApiFactory.create(user.restApiUrl).getMessages().body()
			},
			cache = DiskCache(File(cacheDir, "messenger/messages"), serializer()),
			timeProvider = timeProvider
		)
	}

	override fun messagesSentSource(): CachedSource<Unit, List<Message>> {
		val user = userRepository.currentUser!!
		return CachedSource(
			source = { params ->
				messagesApiFactory.create(user.restApiUrl).getMessagesSent().body().sentMessages ?: emptyList()
			},
			cache = DiskCache(File(cacheDir, "messenger/messagesSent"), serializer()),
			timeProvider = timeProvider
		)
	}

	override fun messagesDraftsSource(): CachedSource<Unit, List<Message>> {
		val user = userRepository.currentUser!!
		return CachedSource(
			source = { params ->
				messagesApiFactory.create(user.restApiUrl).getMessagesDrafts().body().draftMessages ?: emptyList()
			},
			cache = DiskCache(File(cacheDir, "messenger/messagesDrafts"), serializer()),
			timeProvider = timeProvider
		)
	}
}
