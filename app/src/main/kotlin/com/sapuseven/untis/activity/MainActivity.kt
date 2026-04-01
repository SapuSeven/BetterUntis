package com.sapuseven.untis.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.sapuseven.untis.core.ui.theme.AppTheme
import com.sapuseven.untis.feature.login.navigation.LoginRoute
import com.sapuseven.untis.feature.timetable.navigation.TimetableBaseRoute
import com.sapuseven.untis.ui.MainApp
import com.sapuseven.untis.util.isSystemInDarkTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
	private val viewModel: MainActivityViewModel by viewModels()

	override fun onCreate(savedInstanceState: Bundle?) {
		val splashScreen = installSplashScreen()
		super.onCreate(savedInstanceState)

		// We keep this as a mutable state, so that we can track changes inside the composition.
		// This allows us to react to dark/light mode changes.
		var themeSettings by mutableStateOf(
			ThemeSettings(
				darkTheme = resources.configuration.isSystemInDarkTheme,
				darkThemeOled = MainActivityUiState.Loading.shouldUseDarkThemeOled,
				themeColor = MainActivityUiState.Loading.customThemeColor,
			),
		)

		lifecycleScope.launch {
			lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
				combine(
					isSystemInDarkTheme(),
					viewModel.uiState,
				) { systemDark, uiState ->
					ThemeSettings(
						darkTheme = uiState.shouldUseDarkTheme(systemDark),
						darkThemeOled = uiState.shouldUseDarkThemeOled,
						themeColor = uiState.customThemeColor,
					)
				}
					.onEach { themeSettings = it }
					.map { it.darkTheme }
					.distinctUntilChanged()
					.collect { darkTheme ->
						// Turn off the decor fitting system windows, which allows us to handle insets,
						// including IME animations, and go edge-to-edge.
						// This is the same parameters as the default enableEdgeToEdge call, but we manually
						// resolve whether to show dark theme using uiState, since it can be different
						// from the configuration's dark theme value based on the user preference.
						enableEdgeToEdge(
							statusBarStyle = SystemBarStyle.auto(
								lightScrim = android.graphics.Color.TRANSPARENT,
								darkScrim = android.graphics.Color.TRANSPARENT,
							) { darkTheme },
							navigationBarStyle = SystemBarStyle.auto(
								lightScrim = lightScrim,
								darkScrim = darkScrim,
							) { darkTheme },
						)
					}
			}
		}

		// Keep the splash screen on-screen until the UI state is loaded. This condition is
		// evaluated each time the app needs to be redrawn so it should be fast to avoid blocking
		// the UI.
		splashScreen.setKeepOnScreenCondition { viewModel.uiState.value.shouldKeepSplashScreen() }

		// Handle deep link (login data) BEFORE you setContent
		//handleIntent(intent)

		setContent {
			val uiState by viewModel.uiState.collectAsStateWithLifecycle()

			if (!uiState.shouldKeepSplashScreen()) {
				val startDestination = when (uiState) {
					is MainActivityUiState.Success -> TimetableBaseRoute
					else -> LoginRoute
				}

				// could wrap this in CompositionLocalProvider and provide dependencies injected at the top
				AppTheme(
					darkTheme = themeSettings.darkTheme,
					darkThemeOled = themeSettings.darkThemeOled,
					themeColor = themeSettings.themeColor,
				) {
					MainApp(startDestination = startDestination)
				}
			}

			/*// ViewModel (or other injector) that exposes current userState + settings
			val viewModel: MainActivityViewModel = hiltViewModel()
			val userState by viewModel.userState.collectAsState()
			val currentIntentData by viewModel.pendingIntentData.collectAsState()

			// If there was a “loginData” in the Intent, navigate once:
			LaunchedEffect(currentIntentData) {
				currentIntentData?.let { dataString ->
					// send this into your NavController or AppNavigator
					viewModel.consumeIntentData()
					//TODO viewModel.appNavigator.navigate(AppRoutes.LoginDataInput(autoLoginData = dataString))
				}
			}

			MainAppContent(
				userState = userState,
				globalSettings = globalSettings,
				settingsFlow = viewModel.userSettingsFlow,
				navigator = viewModel.appNavigator
			)*/
		}
	}


	override fun onNewIntent(intent: Intent) {
		super.onNewIntent(intent)
		handleIntent(intent)
	}

	private fun handleIntent(intent: Intent) {
		when {
			Intent.ACTION_VIEW == intent.action && intent.data?.host == "setschool" -> {
				//TODO appNavigator.navigate(AppRoutes.LoginDataInput(autoLoginData = intent.data.toString()))
			}
		}
	}
}

/**
 * The default light scrim, as defined by androidx and the platform:
 * https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:activity/activity/src/main/java/androidx/activity/EdgeToEdge.kt;l=35-38;drc=27e7d52e8604a080133e8b842db10c89b4482598
 */
private val lightScrim = android.graphics.Color.argb(0xe6, 0xFF, 0xFF, 0xFF)

/**
 * The default dark scrim, as defined by androidx and the platform:
 * https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:activity/activity/src/main/java/androidx/activity/EdgeToEdge.kt;l=40-44;drc=27e7d52e8604a080133e8b842db10c89b4482598
 */
private val darkScrim = android.graphics.Color.argb(0x80, 0x1b, 0x1b, 0x1b)

/**
 * Class for the system theme settings.
 * This wrapping class allows us to combine all the changes and prevent unnecessary recompositions.
 */
data class ThemeSettings(
	val darkTheme: Boolean,
	val darkThemeOled: Boolean,
	val themeColor: Color? = null,
)
