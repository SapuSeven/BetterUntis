package com.sapuseven.untis.ui.pages.infocenter

import androidx.lifecycle.viewModelScope
import com.sapuseven.untis.api.model.untis.MessageOfDay
import com.sapuseven.untis.data.repository.ElementRepository
import com.sapuseven.untis.data.repository.InfoCenterRepository
import com.sapuseven.untis.ui.navigation.AppNavigator
import com.sapuseven.untis.ui.pages.ActivityViewModel
import com.sapuseven.untis.ui.pages.infocenter.fragments.EventList
import crocodile8.universal_cache.FromCache
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class InfoCenterViewModel @Inject constructor(
	internal val elementRepository: ElementRepository,
	private val infoCenterRepository: InfoCenterRepository,
	private val navigator: AppNavigator,
) : ActivityViewModel() {
	private val _messages = MutableStateFlow<List<MessageOfDay>?>(null)
	val messages: StateFlow<List<MessageOfDay>?> = _messages

	private val _events = MutableStateFlow<List<EventList>?>(null)
	val events: StateFlow<List<EventList>?> = _events

	val shouldShowAbsences: Boolean = true

	val shouldShowOfficeHours: Boolean = true

	init {
		viewModelScope.launch {
			async { loadMessages() }
		}
	}

	fun goBack() {
		navigator.popBackStack()
	}

	private suspend fun loadMessages() {
		infoCenterRepository.messagesOfDaySource()
			.get(LocalDate.now(), FromCache.CACHED_THEN_LOAD, maxAge = 60 * 60 * 1000 /* 1h */)
			.collect {
				_messages.value = it
			}
	}
}
