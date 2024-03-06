package com.sapuseven.untis.modules

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UserModule {
	@Provides
	fun provideUserId(): Int {
		// TODO: Get the user ID from storage mechanism
		return -1
	}
}
