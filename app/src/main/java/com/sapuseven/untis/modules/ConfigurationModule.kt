package com.sapuseven.untis.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
}
