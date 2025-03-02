package com.sapuseven.untis.domain

import com.sapuseven.untis.api.model.untis.classreg.Exam
import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import com.sapuseven.untis.data.database.entities.SchoolYearEntity
import com.sapuseven.untis.data.database.entities.User
import com.sapuseven.untis.data.repository.InfoCenterRepository
import com.sapuseven.untis.scope.UserScopeManager
import crocodile8.universal_cache.FromCache
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject


class GetExamsUseCase @Inject constructor(
	private val infoCenterRepository: InfoCenterRepository,
	getCurrentSchoolYear: GetCurrentSchoolYearUseCase,
	userScopeManager: UserScopeManager
) {
	private val user: User = userScopeManager.user
	private val currentSchoolYear =
		getCurrentSchoolYear() ?: SchoolYearEntity(startDate = LocalDate.now(), endDate = LocalDate.now())

	companion object {
		private const val ONE_HOUR: Long = 60 * 60 * 1000
	}

	operator fun invoke(): Flow<Result<List<Exam>>> = infoCenterRepository.examsSource()
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
