package com.sapuseven.untis.ui.pages.infocenter

import com.sapuseven.untis.api.model.untis.MessageOfDay
import com.sapuseven.untis.ui.navigation.AppNavigator
import com.sapuseven.untis.ui.pages.ActivityViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class InfoCenterViewModel @Inject constructor(
	private val navigator: AppNavigator,
) : ActivityViewModel() {
	private val _messages = MutableStateFlow<List<MessageOfDay>?>(null)
	val messages: StateFlow<List<MessageOfDay>?> = _messages

	val shouldShowAbsences: Boolean = true

	val shouldShowOfficeHours: Boolean = true

	fun goBack() {
		navigator.popBackStack()
	}
}
