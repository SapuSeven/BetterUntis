package com.sapuseven.untis.ui.pages.infocenter

import androidx.lifecycle.viewModelScope
import com.sapuseven.untis.api.model.untis.MessageOfDay
import com.sapuseven.untis.api.model.untis.classreg.Exam
import com.sapuseven.untis.api.model.untis.classreg.HomeWork
import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import com.sapuseven.untis.data.database.entities.User
import com.sapuseven.untis.data.repository.InfoCenterRepository
import com.sapuseven.untis.data.repository.MasterDataRepository
import com.sapuseven.untis.scope.UserScopeManager
import com.sapuseven.untis.ui.navigation.AppNavigator
import com.sapuseven.untis.ui.pages.ActivityViewModel
import crocodile8.universal_cache.FromCache
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class InfoCenterViewModel @Inject constructor(
	internal val masterDataRepository: MasterDataRepository,
	private val infoCenterRepository: InfoCenterRepository,
	private val userScopeManager: UserScopeManager,
	private val navigator: AppNavigator,
) : ActivityViewModel() {
	private val currentUser: User = userScopeManager.user
	private val currentSchoolYearEndDate by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
		masterDataRepository.currentSchoolYear()?.endDate ?: LocalDate.now()
	}

	private val _messages = MutableStateFlow<Result<List<MessageOfDay>>?>(null)
	val messages: StateFlow<Result<List<MessageOfDay>>?> = _messages

	private val _exams = MutableStateFlow<Result<List<Exam>>?>(null)
	val exams: StateFlow<Result<List<Exam>>?> = _exams

	private val _homework = MutableStateFlow<Result<List<HomeWork>>?>(null)
	val homework: StateFlow<Result<List<HomeWork>>?> = _homework

	val shouldShowAbsences: Boolean = true

	val shouldShowOfficeHours: Boolean = true

	fun goBack() {
		navigator.popBackStack()
	}

	init {
		viewModelScope.launch {
			listOf(
				async { loadMessages() },
				async { loadExams() },
				async { loadHomeworks() }
			).awaitAll()
		}
	}

	private fun loadMessages() = viewModelScope.launch {
		infoCenterRepository.messagesOfDaySource()
			.get(LocalDate.now(), FromCache.CACHED_THEN_LOAD, maxAge = 60 * 60 * 1000 /* 1h */)
			.collectToStateResult(_messages)
	}

	private suspend fun loadExams() {
		infoCenterRepository.examsSource()
			.get(
				InfoCenterRepository.ClassRegParams(
					currentUser.userData.elemId,
					currentUser.userData.elemType ?: ElementType.STUDENT,
					LocalDate.now(),
					currentSchoolYearEndDate
				),
				FromCache.CACHED_THEN_LOAD,
				maxAge = 60 * 60 * 1000 /* 1h */
			)
			.collectToStateResult(_exams)
	}

	private suspend fun loadHomeworks() {
		infoCenterRepository.homeworksSource()
			.get(
				InfoCenterRepository.ClassRegParams(
					currentUser.userData.elemId,
					currentUser.userData.elemType ?: ElementType.STUDENT,
					LocalDate.now(),
					currentSchoolYearEndDate
				),
				FromCache.CACHED_THEN_LOAD,
				maxAge = 60 * 60 * 1000 /* 1h */
			)
			.collectToStateResult(_homework)
	}
}

// Maybe this can be reused elsewhere?
private suspend fun <T> Flow<T>.collectToStateResult(state: MutableStateFlow<Result<T>?>) {
	catch { throwable ->
		state.value = Result.failure(throwable)
	}.collect { value ->
		state.value = Result.success(value)
	}
}
