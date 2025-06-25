package com.sapuseven.untis.modules

import com.sapuseven.untis.data.repository.InfoCenterRepository
import com.sapuseven.untis.data.repository.MasterDataRepository
import com.sapuseven.untis.data.repository.MessagesRepository
import com.sapuseven.untis.data.repository.TimetableRepository
import com.sapuseven.untis.data.repository.UntisInfoCenterRepository
import com.sapuseven.untis.data.repository.UntisMasterDataRepository
import com.sapuseven.untis.data.repository.UntisMessagesRepository
import com.sapuseven.untis.data.repository.UntisTimetableRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {
	@Binds
	fun bindMasterDataRepository(
		implementation: UntisMasterDataRepository
	): MasterDataRepository

	@Binds
	fun bindTimetableRepository(
		implementation: UntisTimetableRepository
	): TimetableRepository

	@Binds
	fun bindInfoCenterRepository(
		implementation: UntisInfoCenterRepository
	): InfoCenterRepository

	@Binds
	fun bindMessagesRepository(
		implementation: UntisMessagesRepository
	): MessagesRepository
}
