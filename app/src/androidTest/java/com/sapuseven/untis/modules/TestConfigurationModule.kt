package com.sapuseven.untis.modules

import android.content.Context
import com.sapuseven.untis.BuildConfig
import com.sapuseven.untis.helpers.BuildConfigFields
import com.sapuseven.untis.helpers.BuildConfigFieldsProvider
import crocodile8.universal_cache.time.TimeProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import java.io.File
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Named

@Module
@TestInstallIn(
	components = [SingletonComponent::class],
	replaces = [ConfigurationModule::class]
)
object TestConfigurationModule {
	@Provides
	@Named("cacheDir")
	fun provideCacheDir(
		@ApplicationContext appContext: Context,
	): File = appContext.cacheDir

	@Provides
	fun provideClock(): Clock = Clock.fixed(
		LocalDateTime.now().with(DayOfWeek.TUESDAY).with(LocalTime.of(10, 15)).atZone(ZoneId.systemDefault())
			.toInstant(),
		ZoneId.systemDefault()
	)

	@Provides
	fun provideTimeProvider(clock: Clock): TimeProvider = object : TimeProvider {
		override fun get(): Long = clock.millis()
	}

	@Provides
	fun provideBuildConfigFieldsProvider(): BuildConfigFieldsProvider = object : BuildConfigFieldsProvider {
		override fun get(): BuildConfigFields = BuildConfigFields(
			false,
			"test",
			BuildConfig.VERSION_CODE,
			BuildConfig.VERSION_NAME
		)
	}
}
