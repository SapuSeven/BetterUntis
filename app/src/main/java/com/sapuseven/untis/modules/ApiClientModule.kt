package com.sapuseven.untis.modules

import com.sapuseven.untis.api.client.SchoolSearchApi
import com.sapuseven.untis.api.client.TimetableApi
import com.sapuseven.untis.api.client.UserDataApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.engine.cio.CIO

@Module
@InstallIn(SingletonComponent::class)
object ApiClientModule {
	@Provides
	fun provideSchoolSearchApi(): SchoolSearchApi = SchoolSearchApi(CIO)
	@Provides
	fun provideUserDataApi(): UserDataApi = UserDataApi(CIO)
	@Provides
	fun provideTimetableApi(): TimetableApi = TimetableApi(CIO)
}
