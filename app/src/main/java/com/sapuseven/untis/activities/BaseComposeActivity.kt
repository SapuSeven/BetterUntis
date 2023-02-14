package com.sapuseven.untis.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.sapuseven.untis.R
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.helpers.config.globalDataStore
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.preferences.dataStorePreferences
import com.sapuseven.untis.ui.common.conditional
import com.sapuseven.untis.ui.functional.bottomInsets
import com.sapuseven.untis.ui.material.scheme.Scheme
import com.sapuseven.untis.ui.theme.generateColorScheme
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

	private var dialogOpenUrl: MutableState<String?>? = null

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
		val user = userDatabase.getUser(getUserIdExtra(intent) ?: loadSelectedUserId()
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

	@Composable
	fun AppTheme(
		initialDarkTheme: Boolean = isSystemInDarkTheme(),
		navBarInset: Boolean = true,
		systemUiController: SystemUiController? = rememberSystemUiController(),
		content: @Composable () -> Unit
	) {
		val context = LocalContext.current
		val scope = rememberCoroutineScope()

		val themeColorPref = dataStorePreferences.themeColor
		val darkThemePref = dataStorePreferences.darkTheme
		val darkThemeOledPref = dataStorePreferences.darkThemeOled

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

					systemUiController?.let {
						setSystemUiColor(it, Color.Transparent, !darkThemeBool)
					}

					// setStatusBarsColor() and setNavigationBarColor() also exist

					val dynamicColor = themeColor == themeColorPref.defaultValue
					customThemeColor = if (dynamicColor) null else Color(themeColor)

					generateColorScheme(
						context,
						dynamicColor,
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

				dialogOpenUrl = remember { mutableStateOf<String?>(null) }
				dialogOpenUrl?.value?.let { url ->
					AlertDialog(
						onDismissRequest = {
							dialogOpenUrl?.value = null
						},
						title = {
							Text(text = stringResource(id = R.string.settings_dialog_url_open_title))
						},
						text = {
							Column {
								Text(text = stringResource(id = R.string.settings_dialog_url_open_text))
								Text(text = url, modifier = Modifier.padding(top = 16.dp))
							}
						},
						confirmButton = {
							TextButton(onClick = { dialogOpenUrl?.value = null }) {
								Text(text = stringResource(id = R.string.all_close))
							}
						}
					)
				}
			}
		} ?: run {
			val backgroundColor = getBackgroundColorExtra(intent)
				?: Color(
					(if (initialDarkTheme) Scheme.dark(0) else Scheme.light(0)).background
				)

			Box(
				modifier = Modifier
					.fillMaxSize()
					.background(backgroundColor)
			) {}

			SideEffect {
				systemUiController?.let {
					setSystemUiColor(it, backgroundColor)
				}
			}
		}
	}

	fun setSystemUiColor(
		systemUiController: SystemUiController,
		color: Color = Color.Transparent,
		darkIcons: Boolean = color.luminance() > 0.5f
	) {
		systemUiController.run {
			setSystemBarsColor(
				color = color,
				darkIcons = darkIcons
			)

			setNavigationBarColor(
				color = color,
				darkIcons = darkIcons
			)
		}
	}

	fun getUserIdExtra(intent: Intent): Long? {
		return intent.extras?.run {
			if (containsKey(EXTRA_LONG_USER_ID))
				getLong(EXTRA_LONG_USER_ID)
			else null
		}
	}

	internal fun putUserIdExtra(intent: Intent, profileId: Long = currentUserId()) {
		intent.putExtra(EXTRA_LONG_USER_ID, profileId)
	}

	private fun getBackgroundColorExtra(intent: Intent): Color? {
		return intent.extras?.run {
			if (containsKey(EXTRA_INT_BACKGROUND_COLOR))
				Color(getInt(EXTRA_INT_BACKGROUND_COLOR))
			else null
		}
	}

	internal fun putBackgroundColorExtra(intent: Intent, color: Color? = colorScheme?.background) {
		color?.let {
			intent.putExtra(EXTRA_INT_BACKGROUND_COLOR, color.toArgb())
		}
	}

	fun openUrl(url: String) {
		val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
			addCategory(Intent.CATEGORY_BROWSABLE)
			flags = Intent.FLAG_ACTIVITY_NEW_TASK
		}

		if (intent.resolveActivity(packageManager) != null) {
			startActivity(intent)
		} else {
			dialogOpenUrl?.value = url
		}
	}
}
