package com.sapuseven.untis.feature.infocenter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sapuseven.untis.core.domain.infocenter.GetAbsencesUseCase
import com.sapuseven.untis.core.domain.infocenter.GetDirectMessagesUseCase
import com.sapuseven.untis.core.domain.infocenter.GetExamsUseCase
import com.sapuseven.untis.core.domain.infocenter.GetExcusesUseCase
import com.sapuseven.untis.core.domain.infocenter.GetHomeworkUseCase
import com.sapuseven.untis.core.domain.infocenter.GetMessagesOfDayUseCase
import com.sapuseven.untis.core.domain.infocenter.GetOfficeHoursUseCase
import com.sapuseven.untis.core.domain.repository.UserRepository
import com.sapuseven.untis.core.model.user.UserRight
import com.sapuseven.untis.feature.infocenter.pages.AbsencesUiState
import com.sapuseven.untis.feature.infocenter.pages.EventsUiState
import com.sapuseven.untis.feature.infocenter.pages.OfficeHoursUiState
import com.sapuseven.untis.feature.infocenter.pages.messages.Message
import com.sapuseven.untis.feature.infocenter.pages.messages.MessageDetailsState
import com.sapuseven.untis.feature.infocenter.pages.messages.MessagesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class InfoCenterViewModel @Inject constructor(
	userRepository: UserRepository,
	private val getDirectMessages: GetDirectMessagesUseCase,
	getMessagesOfDay: GetMessagesOfDayUseCase,
	getExams: GetExamsUseCase,
	getHomework: GetHomeworkUseCase,
	getAbsences: GetAbsencesUseCase,
	getExcuses: GetExcusesUseCase,
	getOfficeHours: GetOfficeHoursUseCase,
) : ViewModel() {
	private val currentUser = userRepository.observeActiveUser()

	private fun hasRight(right: UserRight): StateFlow<Boolean> = currentUser
		.map { it?.hasRight(right) ?: false }
		.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

	val shouldShowAbsences = hasRight(UserRight.R_MY_ABSENCES)
	val shouldShowAbsencesAdd = hasRight(UserRight.W_OWN_ABSENCE)
	val shouldShowAbsencesAddReason = hasRight(UserRight.W_OWN_ABSENCEREASON)
	val shouldShowOfficeHours = hasRight(UserRight.R_OFFICEHOURS)

	private val _selectedMessage = MutableStateFlow<Message?>(null)

	private val _messageListState = currentUser
		.filterNotNull()
		.flatMapLatest { user ->
			combine(getMessagesOfDay(user), getDirectMessages(user)) { day, direct ->
				MessagesUiState.Success(
					errors = listOfNotNull(day.exceptionOrNull(), direct.exceptionOrNull()),
					messages = day.getOrDefault(emptyList()).map(Message::Day) +
						direct.getOrDefault(emptyList()).map(Message::Direct)
				)
			}
		}.stateIn(
			scope = viewModelScope,
			started = SharingStarted.WhileSubscribed(5_000),
			initialValue = MessagesUiState.Loading
		)

	private val _messageDetailsState = _selectedMessage.flatMapLatest { message ->
		if (message == null) return@flatMapLatest flowOf(null)

		if (message is Message.Day) {
			flowOf(MessageDetailsState.Success(message, message.content))
		} else
			currentUser.filterNotNull().flatMapLatest { user ->
				getDirectMessages(user, message.id.removePrefix("direct-").toLong())
					.map { result ->
						result.fold(
							onSuccess = { MessageDetailsState.Success(message, it.body) },
							onFailure = {
								MessageDetailsState.Error(
									message,
									it.message ?: "Unknown error"
								)
							}
						)
					}
					.onStart { emit(MessageDetailsState.Loading(message)) }
			}
	}

	val messagesState =
		combine(_messageListState, _messageDetailsState) { listState, detailsState ->
			if (detailsState != null) {
				MessagesUiState.Details(detailsState)
			} else {
				listState
			}
		}.stateIn(
			scope = viewModelScope,
			started = SharingStarted.WhileSubscribed(5_000),
			initialValue = MessagesUiState.Loading
		)

	val eventsState = currentUser
		.filterNotNull()
		.flatMapLatest { user ->
			combine(getExams(user), getHomework(user), EventsUiState::Success)
		}.stateIn(
			scope = viewModelScope,
			started = SharingStarted.WhileSubscribed(5_000),
			initialValue = EventsUiState.Loading
		)

	val absencesState = shouldShowAbsences
		.flatMapLatest { hasRight ->
			if (!hasRight) flowOf(AbsencesUiState.Hidden)
			else currentUser.filterNotNull().flatMapLatest { user ->
				combine(getAbsences(user), getExcuses(user), AbsencesUiState::Success)
			}
		}.stateIn(
			scope = viewModelScope,
			started = SharingStarted.WhileSubscribed(5_000),
			initialValue = AbsencesUiState.Loading
		)

	val officeHoursState = shouldShowOfficeHours
		.flatMapLatest { hasRight ->
			if (!hasRight) flowOf(OfficeHoursUiState.Hidden)
			else currentUser.filterNotNull().flatMapLatest { user ->
				getOfficeHours(user).map { OfficeHoursUiState.Success(it) }
			}
		}.stateIn(
			scope = viewModelScope,
			started = SharingStarted.WhileSubscribed(5_000),
			initialValue = OfficeHoursUiState.Loading
		)

	fun onMessageClicked(message: Message) {
		_selectedMessage.value = message
	}

	fun onMessageDismiss() {
		_selectedMessage.value = null
	}

	fun onMessageReply() {
		_selectedMessage.value = null
	}

	fun onMessageDelete() {
		_selectedMessage.value = null
	}
}
