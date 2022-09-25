package com.sapuseven.untis.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.sapuseven.untis.R
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.helpers.config.globalDataStore
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.preferences.dataStorePreferences
import com.sapuseven.untis.ui.common.conditional
import com.sapuseven.untis.ui.functional.bottomInsets
import com.sapuseven.untis.ui.material.scheme.Scheme
import com.sapuseven.untis.ui.theme.toColorScheme
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@SuppressLint("Registered") // This activity is not intended to be used directly
open class BaseComposeActivity : ComponentActivity() {
	internal var user by mutableStateOf<UserDatabase.User?>(null)
	internal var customThemeColor by mutableStateOf<Color?>(null) // Workaround to allow legacy views to respond to theme color changes
	internal var colorScheme by mutableStateOf<ColorScheme?>(null)
	internal lateinit var userDatabase: UserDatabase
	internal lateinit var timetableDatabaseInterface: TimetableDatabaseInterface

	companion object {
		private const val EXTRA_LONG_USER_ID = "com.sapuseven.untis.activities.profileid"
		private const val EXTRA_INT_BACKGROUND_COLOR = "com.sapuseven.untis.activities.backgroundcolor"

		val DATASTORE_KEY_USER_ID = longPreferencesKey("userid")
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		userDatabase = UserDatabase.createInstance(this)
		runBlocking { // Not ideal, but works well enough
			loadInitialUser()
		}

		super.onCreate(savedInstanceState)

		WindowCompat.setDecorFitsSystemWindows(window, false)
	}

	override fun onDestroy() {
		userDatabase.close()

		super.onDestroy()
	}

	@Composable
	fun withUser(
		invalidContent: @Composable () -> Unit = { InvalidProfileDialog() },
		content: @Composable (UserDatabase.User) -> Unit
	) {
		user?.let {
			content(it)
		} ?: run {
			invalidContent()
		}
	}

	@Composable
	private fun InvalidProfileDialog() {
		Surface(
			modifier = Modifier.fillMaxSize()
		) {
			AlertDialog(
				onDismissRequest = {
					finish()
				},
				text = {
					Text("Invalid profile ID") // TODO: Localize
				},
				confirmButton = {
					TextButton(
						onClick = {
							finish()
						}) {
						Text(stringResource(id = R.string.all_exit))
					}
				}
			)
		}
	}

	private suspend fun loadInitialUser() {
		val user = userDatabase.getUser(
			intent.getUserIdExtra() ?: loadSelectedUserId()
		) ?: userDatabase.getAllUsers().getOrNull(0)

		user?.let {
			setUser(it)
		}
	}

	@OptIn(DelicateCoroutinesApi::class)
	fun setUser(user: UserDatabase.User, save: Boolean = false) {
		this.user = user
		this.customThemeColor = null

		timetableDatabaseInterface = TimetableDatabaseInterface(userDatabase, user.id)

		if (save)
			GlobalScope.launch(Dispatchers.IO) {
				saveSelectedUserId(user.id)
			}
	}

	private suspend fun loadSelectedUserId(): Long {
		return globalDataStore.data.map { prefs -> prefs[DATASTORE_KEY_USER_ID] ?: -1 }.first()
	}

	private suspend fun saveSelectedUserId(id: Long) {
		globalDataStore.edit { prefs ->
			prefs[DATASTORE_KEY_USER_ID] = id
		}
	}

	fun currentUserId() = user?.id ?: -1

	private fun generateColorScheme(
		context: Context,
		dynamicColor: Boolean,
		themeColor: Color,
		darkTheme: Boolean,
		darkThemeOled: Boolean
	): ColorScheme {
		customThemeColor = if (dynamicColor) null else themeColor

		return when {
			dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
				if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
			}
			darkTheme -> Scheme.dark(themeColor.toArgb()).toColorScheme()
			else -> Scheme.light(themeColor.toArgb()).toColorScheme()
		}.run {
			if (darkTheme && darkThemeOled)
				copy(background = Color.Black, surface = Color.Black)
			else
				this
		}
	}

	@Composable
	fun AppTheme(
		initialDarkTheme: Boolean = isSystemInDarkTheme(),
		navBarInset: Boolean = true,
		content: @Composable () -> Unit
	) {
		val context = LocalContext.current
		val scope = rememberCoroutineScope()

		val themeColorPref = dataStorePreferences.themeColor
		val darkThemePref = dataStorePreferences.darkTheme
		val darkThemeOledPref = dataStorePreferences.darkThemeOled

		val systemUiController = rememberSystemUiController()

		DisposableEffect(user) {
			val job = scope.launch {
				combine(
					themeColorPref.getValueFlow().cancellable(),
					darkThemePref.getValueFlow().cancellable(),
					darkThemeOledPref.getValueFlow().cancellable()
				) { themeColor, darkTheme, darkThemeOled ->
					val darkThemeBool = when (darkTheme) {
						"on" -> true
						"off" -> false
						else -> initialDarkTheme
					}

					systemUiController.setSystemBarsColor(
						color = Color.Transparent,
						darkIcons = !darkThemeBool
					)

					systemUiController.setNavigationBarColor(
						color = Color.Transparent,
						darkIcons = !darkThemeBool
					)

					// setStatusBarsColor() and setNavigationBarColor() also exist

					generateColorScheme(
						context,
						themeColor == themeColorPref.defaultValue,
						Color(themeColor),
						darkTheme = darkThemeBool,
						darkThemeOled
					)
				}.collect {
					colorScheme = it
				}
			}

			onDispose {
				job.cancel()
			}
		}

		colorScheme?.let {
			MaterialTheme(
				colorScheme = it,
			) {
				Surface(
					modifier = Modifier
						.background(MaterialTheme.colorScheme.background)
						.conditional(navBarInset) {
							bottomInsets()
						}
						.windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)),
					content = content
				)
			}
		} ?: run {
			val backgroundColor = intent.getBackgroundColorExtra()
				?: Color(
					(if (initialDarkTheme) Scheme.dark(0) else Scheme.light(0)).background
				)

			Box(
				modifier = Modifier
					.fillMaxSize()
					.background(backgroundColor)
			) {}

			SideEffect {
				systemUiController.setSystemBarsColor(
					color = backgroundColor
				)

				systemUiController.setNavigationBarColor(
					color = backgroundColor
				)
			}
		}
	}

	fun Intent.getUserIdExtra(): Long? {
		return extras?.run {
			if (containsKey(EXTRA_LONG_USER_ID))
				getLong(EXTRA_LONG_USER_ID)
			else null
		}
	}

	internal fun Intent.putUserIdExtra(profileId: Long = currentUserId()) {
		this.putExtra(EXTRA_LONG_USER_ID, profileId)
	}

	private fun Intent.getBackgroundColorExtra(): Color? {
		return extras?.run {
			if (containsKey(EXTRA_INT_BACKGROUND_COLOR))
				Color(getInt(EXTRA_INT_BACKGROUND_COLOR))
			else null
		}
	}

	internal fun Intent.putBackgroundColorExtra(color: Color? = colorScheme?.background) {
		color?.let {
			this.putExtra(EXTRA_INT_BACKGROUND_COLOR, color.toArgb())
		}
	}
}
