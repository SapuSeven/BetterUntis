package com.sapuseven.untis.modules

import com.sapuseven.untis.api.client.AbsenceApi
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
import io.ktor.client.engine.cio.CIO
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiClientModule {
	@Provides
	@Singleton
	fun provideSchoolSearchApi(): SchoolSearchApi = SchoolSearchApi(CIO)
	@Provides
	@Singleton
	fun provideUserDataApi(): UserDataApi = UserDataApi(CIO)
	@Provides
	@Singleton
	fun provideTimetableApi(): TimetableApi = TimetableApi(CIO)
	@Provides
	@Singleton
	fun provideMessagesApi(): MessagesApi = MessagesApi(CIO)
	@Provides
	@Singleton
	fun provideClassRegApi(): ClassRegApi = ClassRegApi(CIO)
	@Provides
	@Singleton
	fun provideAbsenceApi(): AbsenceApi = AbsenceApi(CIO)
	@Provides
	@Singleton
	fun provideOfficeHoursApi(): OfficeHoursApi = OfficeHoursApi(CIO)
}
