package com.sapuseven.untis.modules

import com.sapuseven.untis.api.client.AbsenceApi
import com.sapuseven.untis.api.client.ApiClient.Companion.DEFAULT_JSON
import com.sapuseven.untis.api.client.ClassRegApi
import com.sapuseven.untis.api.client.MessagesApi
import com.sapuseven.untis.api.client.OfficeHoursApi
import com.sapuseven.untis.api.client.SchoolSearchApi
import com.sapuseven.untis.api.client.TimetableApi
import com.sapuseven.untis.api.client.UserDataApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiClientModule {
	@Provides
	@Singleton
	fun provideHttpClientEngineFactory(): HttpClientEngineFactory<*> = CIO

	@Provides
	@Singleton
	@Named("json")
	fun provideJsonHttpClient(httpClientEngineFactory: HttpClientEngineFactory<*>): HttpClient =
		HttpClient(httpClientEngineFactory.create()) {
			install(ContentNegotiation) {
				json(DEFAULT_JSON, contentType = ContentType.Application.Json)
			}
		}

	@Provides
	@Singleton
	fun provideSchoolSearchApi(engineFactory: HttpClientEngineFactory<*>): SchoolSearchApi = SchoolSearchApi(engineFactory)

	@Provides
	@Singleton
	fun provideUserDataApi(engineFactory: HttpClientEngineFactory<*>): UserDataApi = UserDataApi(engineFactory)

	@Provides
	@Singleton
	fun provideTimetableApi(engineFactory: HttpClientEngineFactory<*>): TimetableApi = TimetableApi(engineFactory)

	@Provides
	@Singleton
	fun provideMessagesApi(engineFactory: HttpClientEngineFactory<*>): MessagesApi = MessagesApi(engineFactory)

	@Provides
	@Singleton
	fun provideClassRegApi(engineFactory: HttpClientEngineFactory<*>): ClassRegApi = ClassRegApi(engineFactory)

	@Provides
	@Singleton
	fun provideAbsenceApi(engineFactory: HttpClientEngineFactory<*>): AbsenceApi = AbsenceApi(engineFactory)

	@Provides
	@Singleton
	fun provideOfficeHoursApi(engineFactory: HttpClientEngineFactory<*>): OfficeHoursApi = OfficeHoursApi(engineFactory)
}
