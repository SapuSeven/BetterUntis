package com.sapuseven.untis.modules

import com.sapuseven.untis.domain.GetMessagesUseCase
import com.sapuseven.untis.domain.GetMessagesUseCaseImpl
import com.sapuseven.untis.domain.GetRoomFinderItemsUseCase
import com.sapuseven.untis.domain.GetRoomFinderItemsUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface UseCaseModule {
	@Binds
	fun bindGetMessagesUseCase(
		implementation: GetMessagesUseCaseImpl
	): GetMessagesUseCase

	@Binds
	fun bindGetRoomFinderItemsUseCase(
		implementation: GetRoomFinderItemsUseCaseImpl
	): GetRoomFinderItemsUseCase
}
