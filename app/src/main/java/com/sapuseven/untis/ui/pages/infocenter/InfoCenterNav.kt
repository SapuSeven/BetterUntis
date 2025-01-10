package com.sapuseven.untis.ui.pages.infocenter

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sapuseven.untis.ui.navigation.AppRoutes
import com.sapuseven.untis.ui.pages.infocenter.fragments.InfoCenterEvents
import com.sapuseven.untis.ui.pages.infocenter.fragments.InfoCenterMessages

fun NavGraphBuilder.InfoCenterNav(
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
        InfoCenterMessages(viewModel.messages.value)
    }

    composable<AppRoutes.InfoCenter.Events>(
        enterTransition = { slideIntoContainer(slideDirection()) },
        exitTransition = { slideOutOfContainer(slideDirection()) },
        popEnterTransition = { slideIntoContainer(slideDirection()) },
        popExitTransition = { slideOutOfContainer(slideDirection()) }
    ) {
        InfoCenterEvents(viewModel.events.value)
    }

    composable<AppRoutes.InfoCenter.Absences>(
        enterTransition = { slideIntoContainer(slideDirection()) },
        exitTransition = { slideOutOfContainer(slideDirection()) },
        popEnterTransition = { slideIntoContainer(slideDirection()) },
        popExitTransition = { slideOutOfContainer(slideDirection()) }
    ) {
        //InfoCenterEvents()
    }

    composable<AppRoutes.InfoCenter.OfficeHours>(
        enterTransition = { slideIntoContainer(slideDirection()) },
        exitTransition = { slideOutOfContainer(slideDirection()) },
        popEnterTransition = { slideIntoContainer(slideDirection()) },
        popExitTransition = { slideOutOfContainer(slideDirection()) }
    ) {
        //InfoCenterEvents()
    }
}
