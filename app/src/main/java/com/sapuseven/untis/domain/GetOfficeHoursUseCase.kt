package com.sapuseven.untis.domain

import com.sapuseven.untis.api.model.untis.timetable.OfficeHour
import com.sapuseven.untis.data.database.entities.User
import com.sapuseven.untis.data.repository.InfoCenterRepository
import com.sapuseven.untis.scope.UserScopeManager
import crocodile8.universal_cache.FromCache
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class GetOfficeHoursUseCase @Inject constructor(
	private val infoCenterRepository: InfoCenterRepository,
	userScopeManager: UserScopeManager
) {
	private val user: User = userScopeManager.user

	companion object {
		private const val ONE_HOUR: Long = 60 * 60 * 1000
	}

	operator fun invoke(): Flow<Result<List<OfficeHour>>> = infoCenterRepository.officeHoursSource()
		.get(
			InfoCenterRepository.OfficeHoursParams(-1, LocalDate.now()),
			FromCache.CACHED_THEN_LOAD,
			maxAge = ONE_HOUR,
			additionalKey = user.id
		)
		.map(Result.Companion::success)
		.catch { emit(Result.failure(it)) }
}
