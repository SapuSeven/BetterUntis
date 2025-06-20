package com.sapuseven.untis.ui.pages.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.sapuseven.untis.data.repository.GlobalSettingsRepository
import com.sapuseven.untis.data.repository.UserRepository
import com.sapuseven.untis.data.settings.model.DarkTheme
import com.sapuseven.untis.data.settings.model.UserSettings
import com.sapuseven.untis.helpers.AppTheme
import com.sapuseven.untis.helpers.ThemeMode
import com.sapuseven.untis.ui.common.ReportsInfoBottomSheet
import com.sapuseven.untis.ui.navigation.AppNavHost
import com.sapuseven.untis.ui.navigation.AppNavigator
import com.sapuseven.untis.ui.navigation.AppRoutes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(
	userState: UserRepository.UserState,
	globalSettingsRepository: GlobalSettingsRepository,
	settingsFlow: Flow<UserSettings>,
	navigator: AppNavigator
) {
	// TODO Build a nice loading screen - a skeleton ui perhaps?
	if (userState is UserRepository.UserState.Loading) {
		Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
			CircularProgressIndicator()
		}
		return
	}

	// FIXME There is still one big issue with this approach - since the colorScheme is injected into TimetableMapper
	//  as soon as the Timetable ViewModel is created, and the settings take a while to load, the old colorScheme
	//  is used for assisted injection.
	//  Either find a way to dynamically update the colorScheme used for WeekView events,
	//  or delay the creation of the ViewModel until the settings are loaded.
	val settings by settingsFlow.collectAsState(initial = UserSettings.getDefaultInstance())

	val darkTheme = when (settings.darkTheme) {
		DarkTheme.DARK -> ThemeMode.AlwaysDark
		DarkTheme.LIGHT -> ThemeMode.AlwaysLight
		else -> ThemeMode.FollowSystem
	}
	val darkThemeOled by remember { derivedStateOf { settings.darkThemeOled } }
	val themeColor by remember { derivedStateOf { if (settings.hasThemeColor()) Color(settings.themeColor) else null } }

	AppTheme(darkTheme, darkThemeOled, themeColor) {
		Surface(modifier = Modifier.fillMaxSize()) {
			when (userState) {
				is UserRepository.UserState.NoUsers -> {
					AppNavHost(
						navigator = navigator,
						startDestination = AppRoutes.Login
					)
				}
				is UserRepository.UserState.User -> {
					key(userState.user.id) {
						AppNavHost(
							navigator = navigator,
							startDestination = AppRoutes.Timetable()
						)
					}

					val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
					LaunchedEffect(globalSettingsRepository) {
						globalSettingsRepository.getSettings().first().let {
							if (!it.errorReportingSet) {
								bottomSheetState.show()
							}
						}
					}
					ReportsInfoBottomSheet(globalSettingsRepository, bottomSheetState)
				}
				// (UserState.Loading is already handled above)
				else -> { /* no‐op */ }
			}
		}
	}
}
