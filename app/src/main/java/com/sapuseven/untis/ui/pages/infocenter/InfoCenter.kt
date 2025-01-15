package com.sapuseven.untis.ui.pages.infocenter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sapuseven.untis.R
import com.sapuseven.untis.data.repository.LocalMasterDataRepository
import com.sapuseven.untis.ui.animations.fullscreenDialogAnimationEnter
import com.sapuseven.untis.ui.animations.fullscreenDialogAnimationExit
import com.sapuseven.untis.ui.common.AppScaffold
import com.sapuseven.untis.ui.common.NavigationBarInset
import com.sapuseven.untis.ui.navigation.AppRoutes
import com.sapuseven.untis.ui.pages.infocenter.fragments.AbsenceFilterDialog


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoCenter(
	bottomNavController: NavHostController = rememberNavController(),
	viewModel: InfoCenterViewModel = hiltViewModel()
) {
	val currentRoute by bottomNavController.currentBackStackEntryAsState()

	fun <T : Any> isCurrentRoute(route: T) = currentRoute?.destination?.route == route::class.qualifiedName

	fun <T : Any> navigate(route: T): () -> Unit = {
		if (!isCurrentRoute(route))
			bottomNavController.navigate(route) {
				bottomNavController.graph.startDestinationRoute?.let { route ->
					popUpTo(route)
				}
				launchSingleTop = true
			}
	}

	AppScaffold(
		topBar = {
			CenterAlignedTopAppBar(
				title = {
					Text(stringResource(id = R.string.activity_title_info_center))
				},
				navigationIcon = {
					IconButton(onClick = { viewModel.goBack() }) {
						Icon(
							imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
							contentDescription = stringResource(id = R.string.all_back)
						)
					}
				},
				actions = {
					if (isCurrentRoute(AppRoutes.InfoCenter.Absences)) {
						IconButton(
							onClick = { viewModel.onAbsenceFilterShow() }
						) {
							Icon(painter = painterResource(id = R.drawable.all_filter), contentDescription = null)
						}
					}
				}
			)
		}
	) { innerPadding ->
		Column(
			modifier = Modifier
				.padding(innerPadding)
				.fillMaxSize()
		) {
			CompositionLocalProvider(LocalMasterDataRepository provides viewModel.masterDataRepository) {
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.weight(1f)
				) {
					NavHost(
						navController = bottomNavController,
						startDestination = AppRoutes.InfoCenter.Messages
					) {
						infoCenterNav(viewModel = viewModel)
					}
				}
			}

			NavigationBarInset {
				NavigationBarItem(
					icon = {
						Icon(
							painterResource(id = R.drawable.infocenter_messages),
							contentDescription = null
						)
					},
					label = { Text(stringResource(id = R.string.menu_infocenter_messagesofday)) },
					selected = isCurrentRoute(AppRoutes.InfoCenter.Messages),
					onClick = navigate(AppRoutes.InfoCenter.Messages)
				)

				NavigationBarItem(
					icon = {
						Icon(
							painterResource(id = R.drawable.infocenter_events),
							contentDescription = null
						)
					},
					label = { Text(stringResource(id = R.string.menu_infocenter_events)) },
					selected = isCurrentRoute(AppRoutes.InfoCenter.Events),
					onClick = navigate(AppRoutes.InfoCenter.Events)
				)

				if (viewModel.shouldShowAbsences)
					NavigationBarItem(
						icon = {
							Icon(
								painterResource(id = R.drawable.infocenter_absences),
								contentDescription = null
							)
						},
						label = { Text(stringResource(id = R.string.menu_infocenter_absences)) },
						selected = isCurrentRoute(AppRoutes.InfoCenter.Absences),
						onClick = navigate(AppRoutes.InfoCenter.Absences)
					)

				if (viewModel.shouldShowOfficeHours)
					NavigationBarItem(
						icon = {
							Icon(
								painterResource(id = R.drawable.infocenter_contact),
								contentDescription = null
							)
						},
						label = { Text(stringResource(id = R.string.menu_infocenter_officehours)) },
						selected = isCurrentRoute(AppRoutes.InfoCenter.OfficeHours),
						onClick = navigate(AppRoutes.InfoCenter.OfficeHours)
					)
			}
		}
	}

	val showAbsenceFilter = viewModel.showAbsenceFilter.collectAsState()
	AnimatedVisibility(
		visible = showAbsenceFilter.value,
		enter = fullscreenDialogAnimationEnter(),
		exit = fullscreenDialogAnimationExit()
	) {
		AbsenceFilterDialog(viewModel.userSettingsRepository) {
			viewModel.onAbsenceFilterDismiss()
		}
	}
}

