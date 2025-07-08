package com.sapuseven.untis.ui.pages.settings

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.sapuseven.untis.R
import com.sapuseven.untis.ui.navigation.AppRoutes
import com.sapuseven.untis.ui.pages.settings.fragments.SettingsCategoryAbout
import com.sapuseven.untis.ui.pages.settings.fragments.SettingsCategoryAboutContributors
import com.sapuseven.untis.ui.pages.settings.fragments.SettingsCategoryAboutLibraries
import com.sapuseven.untis.ui.pages.settings.fragments.SettingsCategoryGeneral
import com.sapuseven.untis.ui.pages.settings.fragments.SettingsCategoryNotifications
import com.sapuseven.untis.ui.pages.settings.fragments.SettingsCategoryStyling
import com.sapuseven.untis.ui.pages.settings.fragments.SettingsCategoryTimetable
import soup.compose.material.motion.animation.materialSharedAxisXIn
import soup.compose.material.motion.animation.materialSharedAxisXOut

fun NavGraphBuilder.settingsNav(
	navController: NavHostController
) {
	composable<AppRoutes.Settings.Categories>(
		enterTransition = { slideInHorizontally { it / 2 } + fadeIn() },
		exitTransition = { materialSharedAxisXOut(true, 30) },
		popEnterTransition = { materialSharedAxisXIn(false, 30) },
		popExitTransition = { slideOutHorizontally { it / 2 } + fadeOut() },
	) {
		SettingsScreen(navController = navController, title = null) {
			Spacer(modifier = Modifier.height(8.dp))

			SettingsCategory(
				key = AppRoutes.Settings.General,
				title = { Text(stringResource(id = R.string.preferences_general)) },
				summary = { Text(stringResource(id = R.string.preferences_general_summary)) },
				icon = {
					Icon(
						painter = painterResource(id = R.drawable.settings_general),
						contentDescription = null
					)
				},
				navController = navController,
				isFirst = true
			)

			SettingsCategory(
				key = AppRoutes.Settings.Styling,
				title = { Text(stringResource(id = R.string.preferences_styling)) },
				summary = { Text(stringResource(id = R.string.preferences_styling_summary)) },
				icon = {
					Icon(
						painter = painterResource(id = R.drawable.settings_styling),
						contentDescription = null
					)
				},
				navController = navController
			)

			SettingsCategory(
				key = AppRoutes.Settings.Timetable(),
				title = { Text(stringResource(id = R.string.preferences_timetable)) },
				summary = { Text(stringResource(id = R.string.preferences_timetable_summary)) },
				icon = {
					Icon(
						painter = painterResource(id = R.drawable.settings_timetable),
						contentDescription = null
					)
				},
				navController = navController
			)

			SettingsCategory(
				key = AppRoutes.Settings.Notifications,
				title = { Text(stringResource(id = R.string.preferences_notifications)) },
				summary = { Text(stringResource(id = R.string.preferences_notifications_summary)) },
				icon = {
					Icon(
						painter = painterResource(id = R.drawable.settings_notifications),
						contentDescription = null
					)
				},
				navController = navController
			)

			SettingsCategory(
				key = AppRoutes.Settings.About,
				title = { Text(stringResource(id = R.string.preferences_info)) },
				summary = { Text(stringResource(id = R.string.preferences_info_summary)) },
				icon = {
					Icon(
						painter = painterResource(id = R.drawable.settings_info),
						contentDescription = null
					)
				},
				navController = navController,
				isLast = true
			)
		}
	}

	composable<AppRoutes.Settings.General> {
		SettingsScreen(
			navController = navController,
			title = stringResource(id = R.string.preferences_general)
		) { viewModel ->
			SettingsCategoryGeneral(viewModel)
		}
	}

	composable<AppRoutes.Settings.Styling> {
		SettingsScreen(
			navController = navController,
			title = stringResource(id = R.string.preferences_styling)
		) { viewModel ->
			SettingsCategoryStyling(viewModel)
		}
	}
	composable<AppRoutes.Settings.Timetable> {
		SettingsScreen(
			navController = navController,
			title = stringResource(id = R.string.preferences_timetable)
		) { viewModel ->
			SettingsCategoryTimetable(viewModel)
		}
	}
	composable<AppRoutes.Settings.Notifications> {
		SettingsScreen(
			navController = navController,
			title = stringResource(id = R.string.preferences_notifications)
		) { viewModel ->
			SettingsCategoryNotifications(viewModel)
		}
	}

	composable<AppRoutes.Settings.About> {
		SettingsScreen(
			navController = navController,
			title = stringResource(id = R.string.preferences_info)
		) {
			SettingsCategoryAbout(navController)
		}
	}

	composable<AppRoutes.Settings.About.Contributors> {
		SettingsCategoryAboutContributors(navController)
	}

	composable<AppRoutes.Settings.About.Libraries> {
		SettingsCategoryAboutLibraries(navController)
	}
}
