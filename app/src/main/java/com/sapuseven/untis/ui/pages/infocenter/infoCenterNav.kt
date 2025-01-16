package com.sapuseven.untis.ui.pages.infocenter

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sapuseven.untis.ui.navigation.AppRoutes
import com.sapuseven.untis.ui.pages.infocenter.fragments.InfoCenterAbsences
import com.sapuseven.untis.ui.pages.infocenter.fragments.InfoCenterEvents
import com.sapuseven.untis.ui.pages.infocenter.fragments.InfoCenterMessages
import com.sapuseven.untis.ui.pages.infocenter.fragments.InfoCenterOfficeHours

fun NavGraphBuilder.infoCenterNav(
    viewModel: InfoCenterViewModel
) {
    val pages = listOf(
        AppRoutes.InfoCenter.Messages,
        AppRoutes.InfoCenter.Events,
        AppRoutes.InfoCenter.Absences,
        AppRoutes.InfoCenter.OfficeHours
    ).map { it::class.qualifiedName }
    
    val slideDirection: AnimatedContentTransitionScope<NavBackStackEntry>.() -> AnimatedContentTransitionScope.SlideDirection = {
        if (pages.indexOf(initialState.destination.route) < pages.indexOf(targetState.destination.route))
            AnimatedContentTransitionScope.SlideDirection.Left
        else
            AnimatedContentTransitionScope.SlideDirection.Right
    }

    composable<AppRoutes.InfoCenter.Messages>(
        enterTransition = { slideIntoContainer(slideDirection()) },
        exitTransition = { slideOutOfContainer(slideDirection()) },
        popEnterTransition = { slideIntoContainer(slideDirection()) },
        popExitTransition = { slideOutOfContainer(slideDirection()) }
    ) {
		val messagesState by viewModel.messagesState.collectAsStateWithLifecycle()
        InfoCenterMessages(messagesState)
    }

    composable<AppRoutes.InfoCenter.Events>(
        enterTransition = { slideIntoContainer(slideDirection()) },
        exitTransition = { slideOutOfContainer(slideDirection()) },
        popEnterTransition = { slideIntoContainer(slideDirection()) },
        popExitTransition = { slideOutOfContainer(slideDirection()) }
    ) {
		val eventsState by viewModel.eventsState.collectAsStateWithLifecycle()
        InfoCenterEvents(eventsState)
    }

    composable<AppRoutes.InfoCenter.Absences>(
        enterTransition = { slideIntoContainer(slideDirection()) },
        exitTransition = { slideOutOfContainer(slideDirection()) },
        popEnterTransition = { slideIntoContainer(slideDirection()) },
        popExitTransition = { slideOutOfContainer(slideDirection()) }
    ) {
		val absencesState by viewModel.absencesState.collectAsStateWithLifecycle()
        InfoCenterAbsences(absencesState)
    }

    composable<AppRoutes.InfoCenter.OfficeHours>(
        enterTransition = { slideIntoContainer(slideDirection()) },
        exitTransition = { slideOutOfContainer(slideDirection()) },
        popEnterTransition = { slideIntoContainer(slideDirection()) },
        popExitTransition = { slideOutOfContainer(slideDirection()) }
    ) {
		val officeHoursState by viewModel.officeHoursState.collectAsStateWithLifecycle()
        InfoCenterOfficeHours(officeHoursState)
    }
}
