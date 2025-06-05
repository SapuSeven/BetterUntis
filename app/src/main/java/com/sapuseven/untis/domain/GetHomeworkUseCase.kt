package com.sapuseven.untis.domain

import com.sapuseven.untis.api.model.untis.classreg.HomeWork
import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import com.sapuseven.untis.data.repository.UserRepository
import com.sapuseven.untis.data.database.entities.SchoolYearEntity
import com.sapuseven.untis.data.repository.InfoCenterRepository
import crocodile8.universal_cache.FromCache
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject


class GetHomeworkUseCase @Inject constructor(
	private val userRepository: UserRepository,
	private val infoCenterRepository: InfoCenterRepository,
	getCurrentSchoolYear: GetCurrentSchoolYearUseCase,
) {
	private val currentSchoolYear =
		getCurrentSchoolYear() ?: SchoolYearEntity(startDate = LocalDate.now(), endDate = LocalDate.now())

	companion object {
		private const val ONE_HOUR: Long = 60 * 60 * 1000
	}

	operator fun invoke(): Flow<Result<List<HomeWork>>> = userRepository.currentUser!!.let { user ->
		infoCenterRepository.homeworkSource()
			.get(
				InfoCenterRepository.EventsParams(
					user.userData.elemId,
					user.userData.elemType ?: ElementType.STUDENT,
					LocalDate.now(),
					currentSchoolYear.endDate
				),
				FromCache.CACHED_THEN_LOAD, maxAge = ONE_HOUR, additionalKey = user.id
			)
			.map(Result.Companion::success)
			.catch { emit(Result.failure(it)) }
	}
}
