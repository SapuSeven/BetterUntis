package com.sapuseven.untis.ui.navigation

import android.os.Bundle
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import com.sapuseven.untis.activities.LoginDataInputActivity.Companion.EXTRA_BOOLEAN_DEMO_LOGIN
import com.sapuseven.untis.activities.LoginDataInputActivity.Companion.EXTRA_STRING_SCHOOL_INFO
import com.sapuseven.untis.api.model.untis.SchoolInfo
import com.sapuseven.untis.helpers.SerializationUtils
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.encodeToString
import javax.inject.Inject

enum class Screen(val path: String) {
	SPLASH("/"),
	TIMETABLE("/timetable/{userId}"),
	LOGIN("/login"),
	LOGIN_DATA_INPUT("/logindata"),
	SETTINGS("/settings/{userId}"),
}

sealed class NavigationItem(val route: String) {
	data object Splash : NavigationItem(Screen.SPLASH.path)
	data object Timetable : NavigationItem(Screen.TIMETABLE.path)
	data object Login : NavigationItem(Screen.LOGIN.path)
	data object LoginDataInput : NavigationItem(Screen.LOGIN_DATA_INPUT.path)
	data object Settings : NavigationItem(Screen.SETTINGS.path)
}

interface NavigationAction {
	val destination: String
	val arguments: Bundle?
		get() = null
	val navOptions: NavOptions
		get() = NavOptions.Builder().build()
}

object NavigationActions {
	object Splash {
		private val splashNavOptions = NavOptions.Builder().apply {
			setPopUpTo(NavigationItem.Splash.route, inclusive = true)
		}.build()

		fun toLogin() = object : NavigationAction {
			override val destination = NavigationItem.Login.route
			override val navOptions = splashNavOptions
		}

		fun toTimetable(userId: Long) = object : NavigationAction {
			override val destination = "/timetable/$userId"
			override val navOptions = splashNavOptions
		}
	}
	object Login {
		fun toDataInput(
			schoolInfo: SchoolInfo? = null,
			demoLogin: Boolean? = null
		) = object : NavigationAction {
			override val destination = NavigationItem.LoginDataInput.route
			override val arguments: Bundle
				get() = Bundle().apply {
					schoolInfo?.let {
						putString(
							EXTRA_STRING_SCHOOL_INFO,
							SerializationUtils.getJSON().encodeToString(it)
						)
					}
					demoLogin?.let {
						putBoolean(EXTRA_BOOLEAN_DEMO_LOGIN, it)
					}
				}
		}
	}
}

@ActivityRetainedScoped
class AppNavigator @Inject constructor() {
	private val _navActions = Channel<NavigationAction>(Channel.UNLIMITED)
	val navActions = _navActions.receiveAsFlow()

	fun navigate(navAction: NavigationAction) {
		_navActions.trySend(navAction)
	}
}
