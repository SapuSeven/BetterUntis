package com.sapuseven.untis.ui.pages.infocenter

import androidx.compose.material3.darkColorScheme
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import com.sapuseven.untis.api.model.untis.enumeration.Right
import com.sapuseven.untis.data.database.entities.User
import com.sapuseven.untis.data.repository.InfoCenterRepository
import com.sapuseven.untis.data.repository.MasterDataRepository
import com.sapuseven.untis.scope.UserScopeManager
import com.sapuseven.untis.ui.navigation.AppNavigator
import com.sapuseven.untis.ui.pages.infocenter.fragments.AbsencesUiState
import com.sapuseven.untis.ui.pages.infocenter.fragments.EventsUiState
import com.sapuseven.untis.ui.pages.infocenter.fragments.MessagesUiState
import com.sapuseven.untis.ui.pages.infocenter.fragments.OfficeHoursUiState
import com.sapuseven.untis.ui.pages.settings.UserSettingsRepository
import crocodile8.universal_cache.FromCache
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class InfoCenterViewModel @Inject constructor(
	internal val masterDataRepository: MasterDataRepository,
	private val infoCenterRepository: InfoCenterRepository,
	private val navigator: AppNavigator,
	userSettingsRepositoryFactory: UserSettingsRepository.Factory,
	userScopeManager: UserScopeManager,
) : ViewModel() {
	companion object {
		private const val ONE_HOUR: Long = 60 * 60 * 1000
	}

	val userSettingsRepository =
		userSettingsRepositoryFactory.create(darkColorScheme()) // Color scheme doesn't matter here

	private val currentUser: User = userScopeManager.user
	private val currentSchoolYear by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
		masterDataRepository.currentSchoolYear()
	}

	private val excuseStatuses = masterDataRepository.currentUserData?.excuseStatuses ?: emptyList()

	val shouldShowAbsences: Boolean = Right.R_MY_ABSENCES in currentUser.userData.rights
	val shouldShowAbsencesAdd: Boolean = Right.W_OWN_ABSENCE in currentUser.userData.rights
	val shouldShowAbsencesAddReason: Boolean = Right.W_OWN_ABSENCEREASON in currentUser.userData.rights

	val shouldShowOfficeHours: Boolean = Right.R_OFFICEHOURS in currentUser.userData.rights

	val messagesState: StateFlow<MessagesUiState> = infoCenterRepository.messagesOfDaySource()
		.get(LocalDate.now(), FromCache.CACHED_THEN_LOAD, maxAge = ONE_HOUR, additionalKey = currentUser)
		.map(MessagesUiState::Success)
		.catch { MessagesUiState.Success(Result.failure(it)) }
		.stateIn(
			scope = viewModelScope,
			started = SharingStarted.WhileSubscribed(5_000),
			initialValue = MessagesUiState.Loading
		)

	val eventsState: StateFlow<EventsUiState> =
		infoCenterRepository.examsSource()
			.get(
				InfoCenterRepository.EventsParams(
					currentUser.userData.elemId,
					currentUser.userData.elemType ?: ElementType.STUDENT,
					LocalDate.now(),
					currentSchoolYear?.endDate ?: LocalDate.now()
				),
				FromCache.CACHED_THEN_LOAD, maxAge = ONE_HOUR, additionalKey = currentUser
			)
			.map { Result.success(it) }
			.catch { emit(Result.failure(it)) }
			.combine(
				infoCenterRepository.homeworkSource()
					.get(
						InfoCenterRepository.EventsParams(
							currentUser.userData.elemId,
							currentUser.userData.elemType ?: ElementType.STUDENT,
							LocalDate.now(),
							currentSchoolYear?.endDate ?: LocalDate.now()
						),
						FromCache.CACHED_THEN_LOAD,
						maxAge = ONE_HOUR,
						additionalKey = currentUser
					)
					.map { Result.success(it) }
					.catch { emit(Result.failure(it)) },
				transform = EventsUiState::Success
			)
			.stateIn(
				scope = viewModelScope,
				started = SharingStarted.WhileSubscribed(5_000),
				initialValue = EventsUiState.Loading
			)

	@OptIn(ExperimentalCoroutinesApi::class)
	val absencesState: StateFlow<AbsencesUiState> =
		(if (!shouldShowAbsences) emptyFlow() else userSettingsRepository.getSettings().flatMapLatest { settings ->
			val daysAgo: Long = when (settings.infocenterAbsencesTimeRange) {
				"seven_days" -> 7
				"fourteen_days" -> 14
				"thirty_days" -> 30
				"ninety_days" -> 90
				else -> 0
			}

			val timeRange = if (daysAgo > 0) {
				LocalDate.now().minusDays(daysAgo) to LocalDate.now()
			} else {
				(currentSchoolYear?.startDate ?: LocalDate.now()) to (currentSchoolYear?.endDate ?: LocalDate.now())
			}

			infoCenterRepository.absencesSource()
				.get(
					InfoCenterRepository.AbsencesParams(
						timeRange.first,
						timeRange.second,
						includeExcused = !settings.infocenterAbsencesOnlyUnexcused,
					),
					FromCache.CACHED_THEN_LOAD, maxAge = ONE_HOUR, additionalKey = currentUser
				)
				.map {
					if (settings.infocenterAbsencesSortReverse)
						it.sortedBy { absence -> absence.startDateTime } // oldest first
					else
						it.sortedByDescending { absence -> absence.startDateTime } // newest first
				}
				.map { AbsencesUiState.Success(Result.success(it), excuseStatuses) }
				.catch { emit(AbsencesUiState.Success(Result.failure(it), excuseStatuses)) }
		}).stateIn(
			scope = viewModelScope,
			started = SharingStarted.WhileSubscribed(5_000),
			initialValue = AbsencesUiState.Loading
		)

	val officeHoursState: StateFlow<OfficeHoursUiState> =
		(if (!shouldShowAbsences) emptyFlow() else infoCenterRepository.officeHoursSource()
			.get(
				InfoCenterRepository.OfficeHoursParams(-1, LocalDate.now()),
				FromCache.CACHED_THEN_LOAD,
				maxAge = ONE_HOUR,
				additionalKey = currentUser
			)
			.map(OfficeHoursUiState::Success)
			.catch { emit(OfficeHoursUiState.Success(Result.failure(it))) }
			).stateIn(
				scope = viewModelScope,
				started = SharingStarted.WhileSubscribed(5_000),
				initialValue = OfficeHoursUiState.Loading
			)

	fun goBack() {
		navigator.popBackStack()
	}
}
