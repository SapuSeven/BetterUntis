package com.sapuseven.untis.ui.pages.infocenter

import androidx.compose.material3.darkColorScheme
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sapuseven.untis.api.model.untis.enumeration.Right
import com.sapuseven.untis.data.database.entities.User
import com.sapuseven.untis.data.repository.MasterDataRepository
import com.sapuseven.untis.domain.GetAbsencesUseCase
import com.sapuseven.untis.domain.GetExamsUseCase
import com.sapuseven.untis.domain.GetHomeworkUseCase
import com.sapuseven.untis.domain.GetMessagesUseCase
import com.sapuseven.untis.domain.GetOfficeHoursUseCase
import com.sapuseven.untis.scope.UserScopeManager
import com.sapuseven.untis.ui.navigation.AppNavigator
import com.sapuseven.untis.ui.pages.infocenter.fragments.AbsencesUiState
import com.sapuseven.untis.ui.pages.infocenter.fragments.EventsUiState
import com.sapuseven.untis.ui.pages.infocenter.fragments.MessagesUiState
import com.sapuseven.untis.ui.pages.infocenter.fragments.OfficeHoursUiState
import com.sapuseven.untis.ui.pages.settings.UserSettingsRepository
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
	internal val masterDataRepository: MasterDataRepository,
	private val navigator: AppNavigator,
	userSettingsRepositoryFactory: UserSettingsRepository.Factory,
	getMessages: GetMessagesUseCase,
	getExams: GetExamsUseCase,
	getHomework: GetHomeworkUseCase,
	getAbsences: GetAbsencesUseCase,
	getOfficeHours: GetOfficeHoursUseCase,
	userScopeManager: UserScopeManager,
) : ViewModel() {
	private val currentUser: User = userScopeManager.user

	private val excuseStatuses = masterDataRepository.currentUserData?.excuseStatuses ?: emptyList()

	val userSettingsRepository = userSettingsRepositoryFactory.create()

	val shouldShowAbsences: Boolean = Right.R_MY_ABSENCES in currentUser.userData.rights
	val shouldShowAbsencesAdd: Boolean = Right.W_OWN_ABSENCE in currentUser.userData.rights
	val shouldShowAbsencesAddReason: Boolean = Right.W_OWN_ABSENCEREASON in currentUser.userData.rights

	val shouldShowOfficeHours: Boolean = Right.R_OFFICEHOURS in currentUser.userData.rights

	val messagesState: StateFlow<MessagesUiState> = getMessages()
		.map(MessagesUiState::Success)
		.stateIn(
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
