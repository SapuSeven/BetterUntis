package com.sapuseven.untis.ui.navigation

import android.util.Log
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import com.sapuseven.untis.ui.pages.infocenter.InfoCenter
import com.sapuseven.untis.ui.pages.login.Login
import com.sapuseven.untis.ui.pages.login.datainput.LoginDataInput
import com.sapuseven.untis.ui.pages.roomfinder.RoomFinder
import com.sapuseven.untis.ui.pages.settings.settingsNav
import com.sapuseven.untis.ui.pages.splash.Splash
import com.sapuseven.untis.ui.pages.timetable.Timetable
import soup.compose.material.motion.animation.materialSharedAxisXIn
import soup.compose.material.motion.animation.materialSharedAxisXOut
import kotlin.reflect.typeOf

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

	val navBackStackEntry by navController.currentBackStackEntryAsState()
	LaunchedEffect(navBackStackEntry) {
		var route = navController.currentBackStackEntry?.destination?.route
		navController.currentBackStackEntry?.arguments?.keySet()

		navController.currentBackStackEntry?.arguments?.let {
			navController.currentBackStackEntry?.arguments?.keySet()?.forEach { key ->
				val value = navController.currentBackStackEntry?.arguments?.get(key)?.toString() ?: "[null]"
				route = route?.replaceFirst("{$key}", value)
			}
		}
		Log.d("AppNavigation", route ?: "/")
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
			typeMap = mapOf(typeOf<ElementType>() to NavType.EnumType(ElementType::class.java)),
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

		composable<AppRoutes.InfoCenter>(
			enterTransition = {
				slideInVertically() { it / 2 } + fadeIn()
			},
			exitTransition = {
				slideOutVertically() { it / 2 } + fadeOut()
			},
			popEnterTransition = {
				slideInVertically() { it / 2 } + fadeIn()
			},
			popExitTransition = {
				slideOutVertically() { it / 2 } + fadeOut()
			},
		) {
			InfoCenter()
		}

		composable<AppRoutes.RoomFinder>(
			enterTransition = {
				slideInVertically() { it / 2 } + fadeIn()
			},
			exitTransition = {
				slideOutVertically() { it / 2 } + fadeOut()
			},
			popEnterTransition = {
				slideInVertically() { it / 2 } + fadeIn()
			},
			popExitTransition = {
				slideOutVertically() { it / 2 } + fadeOut()
			},
		) {
			RoomFinder()
		}

		navigation<AppRoutes.Settings>(
			startDestination = AppRoutes.Settings.Categories,
			enterTransition = { materialSharedAxisXIn(true, 100) },
			exitTransition = { materialSharedAxisXOut(true, 100) },
			popEnterTransition = { materialSharedAxisXIn(false, 100) },
			popExitTransition = { materialSharedAxisXOut(false, 100) }
		) {
			settingsNav(navController = navController)
		}
	}
}
