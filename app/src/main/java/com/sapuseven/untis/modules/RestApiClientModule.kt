package com.sapuseven.untis.modules

import com.sapuseven.untis.api.client.UserDataApi
import com.sapuseven.untis.api.rest.MessagesApi
import com.sapuseven.untis.data.repository.UserRepository
import com.sapuseven.untis.exceptions.UntisRestApiException
import com.sapuseven.untis.model.rest.ErrorResponse
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import javax.inject.Inject
import kotlin.reflect.KProperty

@Module
@InstallIn(SingletonComponent::class)
object RestApiClientModule {
	@Provides
	fun provideMessagesApiFactory(
		userRepository: UserRepository,
		userDataApi: UserDataApi,
		httpClientEngineFactory: HttpClientEngineFactory<*>
	): MessagesApiFactory =
		DefaultMessagesApiFactory(userRepository, userDataApi, httpClientEngineFactory)
}

interface MessagesApiFactory {
	suspend fun create(): MessagesApi
}

class DefaultMessagesApiFactory @Inject constructor(
	private val userRepository: UserRepository,
	private val userDataApi: UserDataApi,
	private val httpClientEngineFactory: HttpClientEngineFactory<*>
) : MessagesApiFactory {
	val messagesApi by keyedLazy({ userRepository.currentUser!!.id }) {
		MessagesApi(
			baseUrl = userRepository.currentUser!!.restApiUrl.toString(),
			httpClientEngine = httpClientEngineFactory.create()
		) {
			it.expectSuccess = true
			it.install(ContentNegotiation) {
				json()
			}
			it.install(HttpRequestRetry) {
				retryOnServerErrors(maxRetries = 3)
				retryOnException(maxRetries = 3)
				exponentialDelay()
			}
			it.HttpResponseValidator {
				handleResponseExceptionWithRequest { exception, request ->
					val clientException = exception as? ClientRequestException ?: return@handleResponseExceptionWithRequest
					val exceptionResponse = clientException.response
					val errorResponse = exceptionResponse.body<ErrorResponse>()
					throw UntisRestApiException(errorResponse)
				}
			}
		}
	}

	override suspend fun create(): MessagesApi = messagesApi.apply {
		val user = userRepository.currentUser!!
		// TODO: Only fetch token if missing or expired
		val token = userDataApi.getAuthToken(user.jsonRpcApiUrl.toString(), user.user, user.key)
		setBearerToken(token)
	}
}

class KeyedLazy<K, V>(
	private val keyProvider: () -> K,
	private val initializer: (K) -> V
) {
	private var cachedKey: K? = null
	private var cachedValue: V? = null

	operator fun getValue(thisRef: Any?, property: KProperty<*>): V = synchronized(this) {
		val key = keyProvider()
		if (cachedKey != key) {
			cachedValue = initializer(key)
			cachedKey = key
		}
		return cachedValue!!
	}
}

fun <K, V> keyedLazy(keyProvider: () -> K, initializer: (K) -> V): KeyedLazy<K, V> =
	KeyedLazy(keyProvider, initializer)
