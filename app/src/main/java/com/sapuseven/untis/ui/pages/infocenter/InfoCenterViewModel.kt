package com.sapuseven.untis.ui.pages.infocenter

import androidx.compose.material3.darkColorScheme
import androidx.lifecycle.viewModelScope
import com.sapuseven.untis.api.model.untis.MessageOfDay
import com.sapuseven.untis.api.model.untis.absence.StudentAbsence
import com.sapuseven.untis.api.model.untis.classreg.Exam
import com.sapuseven.untis.api.model.untis.classreg.HomeWork
import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import com.sapuseven.untis.api.model.untis.enumeration.Right
import com.sapuseven.untis.api.model.untis.timetable.OfficeHour
import com.sapuseven.untis.data.database.entities.User
import com.sapuseven.untis.data.repository.InfoCenterRepository
import com.sapuseven.untis.data.repository.MasterDataRepository
import com.sapuseven.untis.scope.UserScopeManager
import com.sapuseven.untis.ui.navigation.AppNavigator
import com.sapuseven.untis.ui.pages.ActivityViewModel
import com.sapuseven.untis.ui.pages.settings.UserSettingsRepository
import crocodile8.universal_cache.FromCache
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class InfoCenterViewModel @Inject constructor(
	internal val masterDataRepository: MasterDataRepository,
	private val infoCenterRepository: InfoCenterRepository,
	private val navigator: AppNavigator,
	userSettingsRepositoryFactory: UserSettingsRepository.Factory,
	userScopeManager: UserScopeManager,
) : ActivityViewModel() {
	val userSettingsRepository = userSettingsRepositoryFactory.create(darkColorScheme()) // Color scheme doesn't matter here

	private val currentUser: User = userScopeManager.user
	private val currentSchoolYear by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
		masterDataRepository.currentSchoolYear()
	}

	val excuseStatuses = masterDataRepository.currentUserData?.excuseStatuses ?: emptyList()

	private val _messages = MutableStateFlow<Result<List<MessageOfDay>>?>(null)
	val messages: StateFlow<Result<List<MessageOfDay>>?> = _messages

	private val _exams = MutableStateFlow<Result<List<Exam>>?>(null)
	val exams: StateFlow<Result<List<Exam>>?> = _exams

	private val _homework = MutableStateFlow<Result<List<HomeWork>>?>(null)
	val homework: StateFlow<Result<List<HomeWork>>?> = _homework

	private val _absences = MutableStateFlow<Result<List<StudentAbsence>>?>(null)
	val absences: StateFlow<Result<List<StudentAbsence>>?> = _absences

	private val _officeHours = MutableStateFlow<Result<List<OfficeHour>>?>(null)
	val officeHours: StateFlow<Result<List<OfficeHour>>?> = _officeHours

	private val _showAbsenceFilter = MutableStateFlow(false)
	val showAbsenceFilter: StateFlow<Boolean> = _showAbsenceFilter

	val shouldShowAbsences: Boolean = Right.R_MY_ABSENCES in currentUser.userData.rights
	val shouldShowAbsencesAdd: Boolean = Right.W_OWN_ABSENCE in currentUser.userData.rights
	val shouldShowAbsencesAddReason: Boolean = Right.W_OWN_ABSENCEREASON in currentUser.userData.rights

	val shouldShowOfficeHours: Boolean = Right.R_OFFICEHOURS in currentUser.userData.rights

	fun goBack() {
		navigator.popBackStack()
	}

	init {
		viewModelScope.launch {
			listOf(
				async { loadMessages() },
				async { loadExams() },
				async { loadHomework() },
				async { loadAbsences() },
				async { loadOfficeHours() },
			).awaitAll()
		}
	}

	private fun loadMessages() = viewModelScope.launch {
		infoCenterRepository.messagesOfDaySource()
			.get(
				LocalDate.now(),
				FromCache.CACHED_THEN_LOAD,
				maxAge = 60 * 60 * 1000 /* 1h */,
				additionalKey = currentUser
			)
			.collectToStateResult(_messages)
	}

	private suspend fun loadExams() {
		infoCenterRepository.examsSource()
			.get(
				InfoCenterRepository.EventsParams(
					currentUser.userData.elemId,
					currentUser.userData.elemType ?: ElementType.STUDENT,
					LocalDate.now(),
					currentSchoolYear?.endDate ?: LocalDate.now()
				),
				FromCache.CACHED_THEN_LOAD,
				maxAge = 60 * 60 * 1000, /* 1h */
				additionalKey = currentUser
			)
			.collectToStateResult(_exams)
	}

	private suspend fun loadHomework() {
		infoCenterRepository.homeworkSource()
			.get(
				InfoCenterRepository.EventsParams(
					currentUser.userData.elemId,
					currentUser.userData.elemType ?: ElementType.STUDENT,
					LocalDate.now(),
					currentSchoolYear?.endDate ?: LocalDate.now()
				),
				FromCache.CACHED_THEN_LOAD,
				maxAge = 60 * 60 * 1000, /* 1h */
				additionalKey = currentUser
			)
			.collectToStateResult(_homework)
	}

	private suspend fun loadAbsences() {
		if (!shouldShowAbsences) return

		val settings = userSettingsRepository.getSettings().first()

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

		val params = InfoCenterRepository.AbsencesParams(
			timeRange.first,
			timeRange.second,
			includeExcused = !settings.infocenterAbsencesOnlyUnexcused,
		)

		infoCenterRepository.absencesSource()
			.get(
				params,
				FromCache.CACHED_THEN_LOAD,
				maxAge = 60 * 60 * 1000, /* 1h */
				additionalKey = currentUser
			)
			.collectToStateResult(_absences) {
				if (settings.infocenterAbsencesSortReverse)
					it.sortedBy { absence -> absence.startDateTime } // oldest first
				else
					it.sortedByDescending { absence -> absence.startDateTime } // newest first
			}
	}

	private suspend fun loadOfficeHours() {
		if (!shouldShowOfficeHours) return

		infoCenterRepository.officeHoursSource()
			.get(
				InfoCenterRepository.OfficeHoursParams(-1, LocalDate.now()),
				FromCache.CACHED_THEN_LOAD,
				maxAge = 60 * 60 * 1000, /* 1h */
				additionalKey = currentUser
			)
			.collectToStateResult(_officeHours)
	}

	fun onAbsenceFilterDismiss() {
		_showAbsenceFilter.value = false
	}

	fun onAbsenceFilterShow() {
		_showAbsenceFilter.value = true
	}
}

// Maybe this can be reused elsewhere?
private suspend fun <T> Flow<T>.collectToStateResult(
	state: MutableStateFlow<Result<T>?>,
	transform: (T) -> T = { it }
) {
	catch { throwable ->
		state.value = Result.failure(throwable)
	}.collect { value ->
		state.value = Result.success(transform(value))
	}
}
