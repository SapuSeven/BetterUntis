package com.sapuseven.untis.ui.pages

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

abstract class ActivityViewModel() : ViewModel() {
	protected val activityEvents = Channel<ActivityEvents>()
	fun activityEvents() = activityEvents.receiveAsFlow().asLiveData(Dispatchers.Main)
}

sealed class ActivityEvents {
	data class Finish(
		val resultCode: Int? = null,
		val data: Intent? = null
	) : ActivityEvents()

	data class Launch(
		val clazz: Class<*>,
		val data: Bundle? = null
	) : ActivityEvents()
}
