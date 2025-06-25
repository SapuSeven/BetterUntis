package com.sapuseven.untis.ui.pages.infocenter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sapuseven.untis.api.model.untis.enumeration.Right
import com.sapuseven.untis.data.repository.MasterDataRepository
import com.sapuseven.untis.data.repository.UserRepository
import com.sapuseven.untis.data.repository.UserSettingsRepository
import com.sapuseven.untis.domain.GetAbsencesUseCase
import com.sapuseven.untis.domain.GetExamsUseCase
import com.sapuseven.untis.domain.GetHomeworkUseCase
import com.sapuseven.untis.domain.GetMessagesOfDayUseCase
import com.sapuseven.untis.domain.GetMessagesUseCase
import com.sapuseven.untis.domain.GetOfficeHoursUseCase
import com.sapuseven.untis.ui.navigation.AppNavigator
import com.sapuseven.untis.ui.pages.infocenter.fragments.AbsencesUiState
import com.sapuseven.untis.ui.pages.infocenter.fragments.EventsUiState
import com.sapuseven.untis.ui.pages.infocenter.fragments.MessagesUiState
import com.sapuseven.untis.ui.pages.infocenter.fragments.OfficeHoursUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class InfoCenterViewModel @Inject constructor(
	private val userRepository: UserRepository,
	internal val masterDataRepository: MasterDataRepository,
	private val navigator: AppNavigator,
	internal val userSettingsRepository: UserSettingsRepository,
	getMessages: GetMessagesUseCase,
	getMessagesOfDay: GetMessagesOfDayUseCase,
	getExams: GetExamsUseCase,
	getHomework: GetHomeworkUseCase,
	getAbsences: GetAbsencesUseCase,
	getOfficeHours: GetOfficeHoursUseCase,
) : ViewModel() {
	private val excuseStatuses = masterDataRepository.userData?.excuseStatuses ?: emptyList()

	val shouldShowAbsences: Boolean
		get() = Right.R_MY_ABSENCES in userRepository.currentUser!!.userData.rights
	val shouldShowAbsencesAdd: Boolean
		get() = Right.W_OWN_ABSENCE in userRepository.currentUser!!.userData.rights
	val shouldShowAbsencesAddReason: Boolean
		get() = Right.W_OWN_ABSENCEREASON in userRepository.currentUser!!.userData.rights

	val shouldShowOfficeHours: Boolean
		get() = Right.R_OFFICEHOURS in userRepository.currentUser!!.userData.rights

	val messagesState: StateFlow<MessagesUiState> = combine(
		getMessagesOfDay(),
		getMessages(),
		MessagesUiState::Success
	).stateIn(
		scope = viewModelScope,
		started = SharingStarted.WhileSubscribed(5_000),
		initialValue = MessagesUiState.Loading
	)

	val eventsState: StateFlow<EventsUiState> = combine(
		getExams(),
		getHomework(),
		EventsUiState::Success
	).stateIn(
		scope = viewModelScope,
		started = SharingStarted.WhileSubscribed(5_000),
		initialValue = EventsUiState.Loading
	)

	val absencesState: StateFlow<AbsencesUiState> =
		(if (!shouldShowAbsences) emptyFlow() else getAbsences())
			.map { AbsencesUiState.Success(it, excuseStatuses) }
			.stateIn(
				scope = viewModelScope,
				started = SharingStarted.WhileSubscribed(5_000),
				initialValue = AbsencesUiState.Loading
			)

	val officeHoursState: StateFlow<OfficeHoursUiState> =
		(if (!shouldShowAbsences) emptyFlow() else getOfficeHours())
			.map(OfficeHoursUiState::Success)
			.stateIn(
				scope = viewModelScope,
				started = SharingStarted.WhileSubscribed(5_000),
				initialValue = OfficeHoursUiState.Loading
			)

	fun goBack() {
		navigator.popBackStack()
	}
}
