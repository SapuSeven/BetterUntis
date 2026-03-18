package com.sapuseven.untis.di

import android.content.Context
import com.sapuseven.untis.core.domain.worker.TimetableActionService
import com.sapuseven.untis.util.ApplicationBuildConfigFieldsProvider
import com.sapuseven.untis.util.BuildConfigFieldsProvider
import com.sapuseven.untis.worker.TimetableActionServiceImpl
import crocodile8.universal_cache.time.SystemTimeProvider
import crocodile8.universal_cache.time.TimeProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import java.io.File
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object ConfigurationModule {
	@Provides
	@Named("cacheDir")
	fun provideCacheDir(
		@ApplicationContext appContext: Context,
	): File = appContext.cacheDir

	@Provides
	fun provideClock(): Clock = Clock.System

	@Provides
	fun provideTimeZone(): TimeZone = TimeZone.currentSystemDefault()

	@Provides
	fun provideTimeProvider(): TimeProvider = SystemTimeProvider

	@Provides
	fun provideBuildConfigFieldsProvider(): BuildConfigFieldsProvider = ApplicationBuildConfigFieldsProvider()

	@Provides
	fun provideTimetableActionService(
		impl: TimetableActionServiceImpl
	): TimetableActionService = impl
}
