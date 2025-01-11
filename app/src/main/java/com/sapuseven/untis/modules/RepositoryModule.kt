package com.sapuseven.untis.modules

import com.sapuseven.untis.data.repository.ElementRepository
import com.sapuseven.untis.data.repository.InfoCenterRepository
import com.sapuseven.untis.data.repository.TimetableRepository
import com.sapuseven.untis.data.repository.UntisElementRepository
import com.sapuseven.untis.data.repository.UntisInfoCenterRepository
import com.sapuseven.untis.data.repository.UntisTimetableRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {
	@Binds
	fun bindElementRepository(
		implementation: UntisElementRepository
	): ElementRepository

	@Binds
	fun bindTimetableRepository(
		implementation: UntisTimetableRepository
	): TimetableRepository

	@Binds
	fun bindInfoCenterRepository(
		implementation: UntisInfoCenterRepository
	): InfoCenterRepository
}
