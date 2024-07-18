package com.sapuseven.untis.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sapuseven.untis.ui.activities.login.Login
import com.sapuseven.untis.ui.activities.logindatainput.LoginDataInput
import com.sapuseven.untis.ui.activities.splash.Splash
import com.sapuseven.untis.ui.activities.timetable.Timetable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

@Composable
fun AppNavHost(
	navigator: AppNavigator,
	modifier: Modifier = Modifier,
	navController: NavHostController = rememberNavController(),
	startDestination: Any = AppRoutes.Splash,
) {
	LaunchedEffect(navigator) {
		navigator.navActions.collect { action ->
			action?.let {
				navController.navigate(it.destination, it.navOptions)
			} ?: navController.popBackStack()
		}
	}

	NavHost(
		modifier = modifier,
		navController = navController,
		startDestination = startDestination,
	) {
		composable<AppRoutes.Splash> { Splash() }
		composable<AppRoutes.Login>(
			enterTransition = {
				fadeIn(animationSpec = tween(500))
			},
			exitTransition = {
				slideOutOfContainer(
					AnimatedContentTransitionScope.SlideDirection.Left,
					animationSpec = tween(500)
				)
			},
			popEnterTransition = {
				slideIntoContainer(
					AnimatedContentTransitionScope.SlideDirection.Right,
					animationSpec = tween(500)
				)
			},
			popExitTransition = {
				slideOutOfContainer(
					AnimatedContentTransitionScope.SlideDirection.Right,
					animationSpec = tween(500)
				)
			},
		) { Login() }
		composable<AppRoutes.LoginDataInput>(
			enterTransition = {
				slideIntoContainer(
					AnimatedContentTransitionScope.SlideDirection.Left,
					animationSpec = tween(500)
				)
			},
			exitTransition = {
				slideOutOfContainer(
					AnimatedContentTransitionScope.SlideDirection.Down,
					animationSpec = tween(500)
				) + fadeOut(animationSpec = tween(500))
			},
			popEnterTransition = {
				slideIntoContainer(
					AnimatedContentTransitionScope.SlideDirection.Right,
					animationSpec = tween(500)
				)
			},
			popExitTransition = {
				slideOutOfContainer(
					AnimatedContentTransitionScope.SlideDirection.Right,
					animationSpec = tween(500)
				)
			},
		) { LoginDataInput() }
		composable<AppRoutes.Timetable>(
			enterTransition = {
				fadeIn(animationSpec = tween(500))
			},
			exitTransition = {
				fadeOut(animationSpec = tween(500))
			},
			popEnterTransition = {
				fadeIn(animationSpec = tween(500))
			},
			popExitTransition = {
				fadeOut(animationSpec = tween(500))
			},
		) { Timetable() }
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
