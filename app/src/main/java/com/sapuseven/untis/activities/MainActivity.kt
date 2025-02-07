package com.sapuseven.untis.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.sapuseven.untis.components.UserManager
import com.sapuseven.untis.components.UserState
import com.sapuseven.untis.helpers.AppTheme
import com.sapuseven.untis.helpers.ThemeMode
import com.sapuseven.untis.scope.UserScopeManager
import com.sapuseven.untis.ui.navigation.AppNavHost
import com.sapuseven.untis.ui.navigation.AppNavigator
import com.sapuseven.untis.ui.navigation.AppRoutes
import com.sapuseven.untis.ui.pages.settings.UserSettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
	companion object {
		const val MESSENGER_PACKAGE_NAME = "com.untis.chat"

		const val EXTRA_STRING_PERIOD_ELEMENT = "com.sapuseven.untis.activities.main.element"
	}

	@Inject
	lateinit var userSettingsRepositoryFactory: UserSettingsRepository.Factory

	@Inject
	lateinit var appNavigator: AppNavigator

	@Inject
	lateinit var userManager: UserManager

	@Inject
	lateinit var userScopeManager: UserScopeManager

	override fun onCreate(savedInstanceState: Bundle?) {
		enableEdgeToEdge()
		super.onCreate(savedInstanceState)

		val action: String? = intent?.action
		val data: Uri? = intent?.data
		when {
			Intent.ACTION_VIEW == action && data?.host == "setschool" -> {
				appNavigator.navigate(AppRoutes.LoginDataInput(autoLoginData = data.toString()))
			}
		}

		setContent {
			val userSettingsRepository = userSettingsRepositoryFactory.create(MaterialTheme.colorScheme)

			val darkTheme by userSettingsRepository.getSettings().map {
				when (it.darkTheme) {
					"on" -> ThemeMode.AlwaysDark
					"off" -> ThemeMode.AlwaysLight
					else -> ThemeMode.FollowSystem
				}
			}.collectAsState(ThemeMode.FollowSystem)

			val darkThemeOled by userSettingsRepository.getSettings().map {
				it.darkThemeOled
			}.collectAsState(false)

			val themeColor by userSettingsRepository.getSettings().map {
				if (it.customThemeColor) Color(it.themeColor) else null
			}.collectAsState(null)

			AppTheme(darkTheme, darkThemeOled, themeColor) {
				Surface(
					modifier = Modifier.fillMaxSize()
				) {
					val userState by userManager.userState.collectAsState()

					when (userState) {
						is UserState.Loading -> Box(
							modifier = Modifier.fillMaxSize(),
							contentAlignment = Alignment.Center
						) {
							CircularProgressIndicator()
						}

						is UserState.NoUsers -> {
							AppNavHost(
								navigator = appNavigator,
								startDestination = AppRoutes.Login
							)
						}

						is UserState.User -> {
							AppNavHost(
								navigator = appNavigator,
								startDestination = AppRoutes.Timetable()
							)

							// Re-create the view when the user changes
							LaunchedEffect(Unit) {
								snapshotFlow { userState }
									.drop(1)
									.collect {
										appNavigator.navigate(AppRoutes.Timetable()) {
											popUpTo(0) // Clear backstack
										}
									}
							}
						}
					}
				}
			}
		}
	}
}
