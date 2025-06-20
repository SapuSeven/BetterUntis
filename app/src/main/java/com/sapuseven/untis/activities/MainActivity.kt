package com.sapuseven.untis.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.sapuseven.untis.ui.navigation.AppNavigator
import com.sapuseven.untis.ui.navigation.AppRoutes
import com.sapuseven.untis.ui.pages.main.MainAppContent
import com.sapuseven.untis.ui.pages.main.MainViewModel
import com.sapuseven.untis.data.repository.GlobalSettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
	companion object {
		const val MESSENGER_PACKAGE_NAME = "com.untis.chat"

		const val EXTRA_STRING_PERIOD_ELEMENT = "com.sapuseven.untis.activities.main.element"
	}

	@Inject
	lateinit var globalSettingsRepository: GlobalSettingsRepository

	@Inject
	lateinit var appNavigator: AppNavigator

	override fun onCreate(savedInstanceState: Bundle?) {
		enableEdgeToEdge()
		super.onCreate(savedInstanceState)

		// Handle deep link (login data) BEFORE you setContent
		handleIntent(intent)

		setContent {
			// ViewModel (or other injector) that exposes current userState + settings
			val viewModel: MainViewModel = hiltViewModel()
			val userState by viewModel.userState.collectAsState()
			val currentIntentData by viewModel.pendingIntentData.collectAsState()

			// If there was a “loginData” in the Intent, navigate once:
			LaunchedEffect(currentIntentData) {
				currentIntentData?.let { dataString ->
					// send this into your NavController or AppNavigator
					viewModel.consumeIntentData()
					viewModel.appNavigator.navigate(AppRoutes.LoginDataInput(autoLoginData = dataString))
				}
			}

			MainAppContent(
				userState = userState,
				globalSettingsRepository = globalSettingsRepository,
				settingsFlow = viewModel.userSettingsFlow,
				navigator = viewModel.appNavigator
			)
		}

		/*val action: String? = intent?.action
		val data: Uri? = intent?.data
		when {
			Intent.ACTION_VIEW == action && data?.host == "setschool" -> {
				appNavigator.navigate(AppRoutes.LoginDataInput(autoLoginData = data.toString()))
			}
		}

		setContent {
			// TODO: Sooo... this doesn't change the theme when the user is switched...
			val scope = rememberCoroutineScope()
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

			val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
			LaunchedEffect(globalSettingsRepository) {
				scope.launch {
					globalSettingsRepository.getSettings().first().let {
						if (!it.errorReportingSet) {
							bottomSheetState.show()
						}
					}
				}
			}

			AppTheme(darkTheme, darkThemeOled, themeColor) {
				Surface(
					modifier = Modifier.fillMaxSize()
				) {
					val userState by userRepository.userState.collectAsState()

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

							ReportsInfoBottomSheet(globalSettingsRepository, bottomSheetState)

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
		}*/
	}


	override fun onNewIntent(intent: Intent) {
		super.onNewIntent(intent)
		handleIntent(intent)
	}

	private fun handleIntent(intent: Intent) {
		when {
			Intent.ACTION_VIEW == intent.action && intent.data?.host == "setschool" -> {
				appNavigator.navigate(AppRoutes.LoginDataInput(autoLoginData = intent.data.toString()))
			}
		}
	}
}
