package com.sapuseven.untis.ui.navigation

import androidx.navigation.NavOptions
import com.sapuseven.untis.api.model.untis.SchoolInfo
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.serialization.Serializable
import javax.inject.Inject

data class NavigationAction <T: Any> (
	val destination: T
)

@ActivityRetainedScoped
class AppNavigator @Inject constructor() {
	private val _navActions = Channel<NavigationAction<*>>(Channel.UNLIMITED)
	val navActions = _navActions.receiveAsFlow()

	fun <T: Any> navigate(action: NavigationAction<T>) {
		_navActions.trySend(action)
	}

	fun <T: Any> navigate(route: T) {
		_navActions.trySend(NavigationAction(route))
	}
}
