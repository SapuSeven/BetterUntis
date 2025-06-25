package com.sapuseven.untis.modules

import android.net.Uri
import com.sapuseven.untis.api.rest.MessagesApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.engine.HttpClientEngineFactory
import javax.inject.Inject

@Module
@InstallIn(SingletonComponent::class)
object RestApiClientModule {
	@Provides
	fun provideMessagesApiFactory(httpClientEngineFactory: HttpClientEngineFactory<*>): MessagesApiFactory =
		DefaultMessagesApiFactory(httpClientEngineFactory)
}

interface MessagesApiFactory {
	fun create(baseUrl: Uri): MessagesApi
}

class DefaultMessagesApiFactory @Inject constructor(
	private val httpClientEngineFactory: HttpClientEngineFactory<*>
) : MessagesApiFactory {
	override fun create(baseUrl: Uri): MessagesApi {
		return MessagesApi(
			baseUrl = baseUrl.toString(),
			httpClientEngine = httpClientEngineFactory.create()
		)
	}
}
