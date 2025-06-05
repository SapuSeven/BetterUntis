package com.sapuseven.untis.modules

import android.content.Context
import com.sapuseven.untis.services.CodeScanService
import com.sapuseven.untis.services.CodeScanServiceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object CodeScanModule {
	@Provides
	fun provideCodeScanService(
		@ApplicationContext context: Context
	): CodeScanService = CodeScanServiceImpl(context)
}
