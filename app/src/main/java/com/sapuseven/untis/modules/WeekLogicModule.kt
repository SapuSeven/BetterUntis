package com.sapuseven.untis.modules

import com.sapuseven.untis.services.WeekLogicService
import com.sapuseven.untis.services.WeekLogicServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
interface WeekLogicModule {
	@Binds
	fun bindWeekLogicService(
		implementation: WeekLogicServiceImpl
	): WeekLogicService
}
