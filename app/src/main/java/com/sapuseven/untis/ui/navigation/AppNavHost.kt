package com.sapuseven.untis.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sapuseven.untis.ui.activities.login.Login
import com.sapuseven.untis.ui.activities.logindatainput.LoginDataInput
import com.sapuseven.untis.ui.activities.splash.Splash
import com.sapuseven.untis.ui.activities.timetable.Timetable
import kotlinx.coroutines.flow.Flow

@Composable
fun AppNavHost(
	navigator: AppNavigator,
	modifier: Modifier = Modifier,
	navController: NavHostController = rememberNavController(),
	startDestination: String = NavigationItem.Splash.route,
) {
	val lifecycleOwner = LocalLifecycleOwner.current
	val navigatorState by navigator.navActions.asLifecycleAwareState(
		lifecycleOwner = lifecycleOwner,
		initialState = null
	)
	LaunchedEffect(navigatorState) {
		navigatorState?.let {
			it.arguments?.let { navController.currentBackStackEntry?.arguments?.putAll(it) }
			navController.navigate(it.destination, it.navOptions)
		}
	}

	NavHost(
		modifier = modifier,
		navController = navController,
		startDestination = startDestination
	) {
		composable(NavigationItem.Splash.route) { Splash() }
		composable(NavigationItem.Login.route) { Login(navController) }
		composable(NavigationItem.LoginDataInput.route) { LoginDataInput(navController) }
		composable(
			NavigationItem.Timetable.route,
			arguments = listOf(navArgument("userId") {
				type = NavType.LongType
			})
		) { Timetable(navController) }
	}
}

@Composable
fun <T> Flow<T>.asLifecycleAwareState(lifecycleOwner: LifecycleOwner, initialState: T) =
	lifecycleAwareState(lifecycleOwner, this, initialState)

@Composable
fun <T> lifecycleAwareState(
	lifecycleOwner: LifecycleOwner,
	flow: Flow<T>,
	initialState: T
): State<T> {
	val lifecycleAwareStateFlow = remember(flow, lifecycleOwner) {
		flow.flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
	}
	return lifecycleAwareStateFlow.collectAsState(initialState)
}
