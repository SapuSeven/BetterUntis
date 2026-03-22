package com.sapuseven.untis.feature.infocenter.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.sapuseven.untis.core.domain.navigation.FeatureRoute
import com.sapuseven.untis.core.domain.navigation.FeatureRouteDsl
import com.sapuseven.untis.core.domain.navigation.FeatureRouteItem
import com.sapuseven.untis.feature.infocenter.InfoCenterScreen
import com.sapuseven.untis.feature.infocenter.InfoCenterViewModel
import com.sapuseven.untis.feature.infocenter.R
import com.sapuseven.untis.feature.infocenter.pages.InfoCenterAbsences
import com.sapuseven.untis.feature.infocenter.pages.InfoCenterEvents
import com.sapuseven.untis.feature.infocenter.pages.messages.InfoCenterMessages
import com.sapuseven.untis.feature.infocenter.pages.InfoCenterOfficeHours
import kotlinx.serialization.Serializable

@Serializable
data object InfoCenterRoute {
	@Serializable
	data object Messages

	@Serializable
	data object Events

	@Serializable
	data object Absences

	@Serializable
	data object OfficeHours
}

fun NavController.navigateToInfoCenterMessages(
	navOptions: NavOptionsBuilder.() -> Unit = {}
) = navigateToInfoCenterPage(InfoCenterRoute.Messages, navOptions)

fun NavController.navigateToInfoCenterEvents(
	navOptions: NavOptionsBuilder.() -> Unit = {}
) = navigateToInfoCenterPage(InfoCenterRoute.Events, navOptions)

fun NavController.navigateToInfoCenterAbsences(
	navOptions: NavOptionsBuilder.() -> Unit = {}
) = navigateToInfoCenterPage(InfoCenterRoute.Absences, navOptions)

fun NavController.navigateToInfoCenterOfficeHours(
	navOptions: NavOptionsBuilder.() -> Unit = {}
) = navigateToInfoCenterPage(InfoCenterRoute.OfficeHours, navOptions)

private fun <T : Any> NavController.navigateToInfoCenterPage(
	route: T,
	navOptions: NavOptionsBuilder.() -> Unit = {}
) {
	// Don't navigate if requested route is already shown
	if (currentBackStackEntry?.destination?.route == route::class.qualifiedName) return

	navigate(route) {
		// Make "Back" always go to the start destination
		graph.startDestinationRoute?.let { route ->
			popUpTo(route)
		}
		launchSingleTop = true

		navOptions()
	}
}

fun NavGraphBuilder.infoCenterScreen(
	navController: NavHostController,
) {
	composable<InfoCenterRoute> {
		val vm = hiltViewModel<InfoCenterViewModel>()
		InfoCenterScreen(
			viewModel = vm,
			onBackClick = navController::popBackStack
		)
	}
}

internal fun NavGraphBuilder.infoCenterPages() {
	val pages = listOf(
		InfoCenterRoute.Messages,
		InfoCenterRoute.Events,
		InfoCenterRoute.Absences,
		InfoCenterRoute.OfficeHours
	).map { it::class.qualifiedName }

	val slideDirection: AnimatedContentTransitionScope<NavBackStackEntry>.() -> AnimatedContentTransitionScope.SlideDirection =
		{
			if (pages.indexOf(initialState.destination.route) < pages.indexOf(targetState.destination.route))
				AnimatedContentTransitionScope.SlideDirection.Left
			else
				AnimatedContentTransitionScope.SlideDirection.Right
		}

	composable<InfoCenterRoute.Messages>(
		enterTransition = { slideIntoContainer(slideDirection()) },
		exitTransition = { slideOutOfContainer(slideDirection()) },
	) {
		val viewModel = hiltViewModel<InfoCenterViewModel>()
		InfoCenterMessages(viewModel)
	}

	composable<InfoCenterRoute.Events>(
		enterTransition = { slideIntoContainer(slideDirection()) },
		exitTransition = { slideOutOfContainer(slideDirection()) },
	) {
		val viewModel = hiltViewModel<InfoCenterViewModel>()
		val state by viewModel.eventsState.collectAsStateWithLifecycle()
		InfoCenterEvents(state)
	}

	composable<InfoCenterRoute.Absences>(
		enterTransition = { slideIntoContainer(slideDirection()) },
		exitTransition = { slideOutOfContainer(slideDirection()) },
	) {
		val viewModel = hiltViewModel<InfoCenterViewModel>()
		val state by viewModel.absencesState.collectAsStateWithLifecycle()
		InfoCenterAbsences(state)
	}

	composable<InfoCenterRoute.OfficeHours>(
		enterTransition = { slideIntoContainer(slideDirection()) },
		exitTransition = { slideOutOfContainer(slideDirection()) },
	) {
		val viewModel = hiltViewModel<InfoCenterViewModel>()
		val state by viewModel.officeHoursState.collectAsStateWithLifecycle()
		InfoCenterOfficeHours(state)
	}
}

@FeatureRouteDsl
fun FeatureRoute.infoCenterRoute(): FeatureRouteItem = FeatureRouteItem(
	R.drawable.feature_infocenter_nav_icon,
	R.string.feature_infocenter_title,
	InfoCenterRoute
)
