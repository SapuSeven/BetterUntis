package com.sapuseven.untis.domain

import com.sapuseven.untis.api.model.untis.absence.StudentAbsence
import com.sapuseven.untis.persistence.entity.SchoolYearEntity
import com.sapuseven.untis.data.repository.InfoCenterRepository
import com.sapuseven.untis.data.repository.UserRepository
import com.sapuseven.untis.data.repository.UserSettingsRepository
import com.sapuseven.untis.data.settings.model.AbsencesTimeRange
import crocodile8.universal_cache.FromCache
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class GetAbsencesUseCase @Inject constructor(
	private val userRepository: UserRepository,
	private val infoCenterRepository: InfoCenterRepository,
	private val userSettingsRepository: UserSettingsRepository,
	getCurrentSchoolYear: GetCurrentSchoolYearUseCase
) {
	private val currentSchoolYear =
		getCurrentSchoolYear() ?: SchoolYearEntity(startDate = LocalDate.now(), endDate = LocalDate.now())

	companion object {
		private const val ONE_HOUR: Long = 60 * 60 * 1000
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	operator fun invoke(): Flow<Result<List<StudentAbsence>>> =
		userSettingsRepository.getSettings().flatMapLatest { settings ->
			val daysAgo: Long = when (settings.infocenterAbsencesTimeRange) {
				AbsencesTimeRange.SEVEN_DAYS -> 7
				AbsencesTimeRange.FOURTEEN_DAYS -> 14
				AbsencesTimeRange.THIRTY_DAYS -> 30
				AbsencesTimeRange.NINETY_DAYS -> 90
				else -> 0
			}

			val timeRange = if (daysAgo > 0) {
				LocalDate.now().minusDays(daysAgo) to LocalDate.now()
			} else {
				currentSchoolYear.startDate to currentSchoolYear.endDate
			}

			infoCenterRepository.absencesSource()
				.get(
					InfoCenterRepository.AbsencesParams(
						timeRange.first,
						timeRange.second,
						includeExcused = !settings.infocenterAbsencesOnlyUnexcused,
					),
					FromCache.CACHED_THEN_LOAD, maxAge = ONE_HOUR, additionalKey = userRepository.currentUser!!.id
				)
				.map {
					if (settings.infocenterAbsencesSortReverse)
						it.sortedBy { absence -> absence.startDateTime } // oldest first
					else
						it.sortedByDescending { absence -> absence.startDateTime } // newest first
				}
				.map(Result.Companion::success)
				.catch { emit(Result.failure(it)) }
		}
}
