package com.sapuseven.untis.activities

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.sapuseven.untis.R
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.helpers.config.PreferenceHelper
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.preferences.dataStorePreferences
import com.sapuseven.untis.ui.material.scheme.Scheme
import com.sapuseven.untis.ui.preferences.materialColors
import com.sapuseven.untis.ui.theme.toColorScheme
import kotlinx.coroutines.launch

@SuppressLint("Registered") // This activity is not intended to be used directly
open class BaseComposeActivity : ComponentActivity() {
	/*protected var hasOwnToolbar: Boolean = false
	protected var currentTheme: String = ""
	private var currentDarkTheme: String = ""*/

	lateinit var preferences: PreferenceHelper
	internal var user by mutableStateOf<UserDatabase.User?>(null)
	internal var customThemeColor by mutableStateOf<Color?>(null) // Workaround to allow legacy views to respond to theme color changes
	internal lateinit var userDatabase: UserDatabase
	internal lateinit var timetableDatabaseInterface: TimetableDatabaseInterface

	companion object {
		const val EXTRA_LONG_PROFILE_ID = "com.sapuseven.untis.activities.profileid"
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		//ErrorLogger.initialize(this)

		//Thread.setDefaultUncaughtExceptionHandler(CrashHandler(Thread.getDefaultUncaughtExceptionHandler()))

		preferences = PreferenceHelper(this)
		userDatabase = UserDatabase.createInstance(this)
		loadUser()

		super.onCreate(savedInstanceState)
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

	fun loadUser() {
		userDatabase.getUser(
			intent.extras?.getLong(EXTRA_LONG_PROFILE_ID) ?: preferences.loadProfileId()
		)?.let {
			loadUser(it)
		}
	}

	fun loadUser(user: UserDatabase.User) {
		this.user = user

		timetableDatabaseInterface = TimetableDatabaseInterface(userDatabase, user.id)
		preferences.loadProfile(user.id)
		preferences.saveProfileId(user.id)
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
		content: @Composable () -> Unit
	) {
		val context = LocalContext.current
		val scope = rememberCoroutineScope()

		val themeColorPref = dataStorePreferences.themeColor
		val darkThemePref = dataStorePreferences.darkTheme
		val darkThemeOledPref = dataStorePreferences.darkThemeOled

		var themeColor by remember { mutableStateOf(themeColorPref.defaultValue) }
		var darkTheme by remember { mutableStateOf(initialDarkTheme) }
		var darkThemeOled by remember { mutableStateOf(false) }

		val colorScheme = remember(themeColor, darkTheme, darkThemeOled) {
			generateColorScheme(
				context,
				themeColor == themeColorPref.defaultValue,
				Color(themeColor),
				darkTheme,
				darkThemeOled
			)
		}

		LaunchedEffect(Unit) {
			scope.launch {
				themeColorPref.getValueFlow().collect {
					themeColor = it
				}
			}

			scope.launch {
				darkThemePref.getValueFlow().collect {
					darkTheme = when (it) {
						"on" -> true
						"off" -> false
						else -> initialDarkTheme
					}
				}
			}

			scope.launch {
				darkThemeOledPref.getValueFlow().collect {
					darkThemeOled = it
				}
			}
		}

		val systemUiController = rememberSystemUiController()

		SideEffect {
			systemUiController.setSystemBarsColor(
				color = colorScheme.background,
				darkIcons = !darkTheme
			)

			// setStatusBarsColor() and setNavigationBarColor() also exist
		}

		MaterialTheme(
			colorScheme = colorScheme,
			content = content
		)
	}

/*
	/**
	 * Checks for saved crashes. Calls [onErrorLogFound] if logs are found.
	 *
	 * @return `true` if the logs contain a critical application crash, `false` otherwise
	 */
	protected fun checkForCrashes(): Boolean {
		val logFiles = File(filesDir, "logs").listFiles()
		if (logFiles?.isNotEmpty() == true) {
			onErrorLogFound()

			return logFiles.find { f -> f.name.startsWith("_") } != null
		}
		return false
	}

	/**
	 * Gets called if any error logs are found.
	 *
	 * Override this function in your actual activity.
	 */
	open fun onErrorLogFound() {
		return
	}

	protected fun readCrashData(crashFile: File): String {
		val reader = crashFile.bufferedReader()

		val stackTrace = StringBuilder()
		val buffer = CharArray(1024)
		var length = reader.read(buffer)

		while (length != -1) {
			stackTrace.append(String(buffer, 0, length))
			length = reader.read(buffer)
		}

		return stackTrace.toString()
	}

	override fun onStart() {
		super.onStart()
		setBlackBackground(preferences["preference_dark_theme_oled"])
	}

	override fun onResume() {
		super.onResume()
		val theme: String = preferences["preference_theme"]
		val darkTheme: String = preferences["preference_dark_theme"]

		if (currentTheme != theme || currentDarkTheme != darkTheme)
			recreate()

		currentTheme = theme
		currentDarkTheme = darkTheme
	}

	private fun setAppTheme(hasOwnToolbar: Boolean) {
		when (currentTheme) {
			"untis" -> setTheme(if (hasOwnToolbar) R.style.AppTheme_ThemeUntis_NoActionBar else R.style.AppTheme_ThemeUntis)
			"blue" -> setTheme(if (hasOwnToolbar) R.style.AppTheme_ThemeBlue_NoActionBar else R.style.AppTheme_ThemeBlue)
			"green" -> setTheme(if (hasOwnToolbar) R.style.AppTheme_ThemeGreen_NoActionBar else R.style.AppTheme_ThemeGreen)
			"pink" -> setTheme(if (hasOwnToolbar) R.style.AppTheme_ThemePink_NoActionBar else R.style.AppTheme_ThemePink)
			"cyan" -> setTheme(if (hasOwnToolbar) R.style.AppTheme_ThemeCyan_NoActionBar else R.style.AppTheme_ThemeCyan)
			"pixel" -> setTheme(if (hasOwnToolbar) R.style.AppTheme_ThemePixel_NoActionBar else R.style.AppTheme_ThemePixel)
			else -> setTheme(if (hasOwnToolbar) R.style.AppTheme_NoActionBar else R.style.AppTheme)
		}

		AppCompatDelegate.setDefaultNightMode(
			when (preferences["preference_dark_theme", currentDarkTheme]) {
				"on" -> AppCompatDelegate.MODE_NIGHT_YES
				"off" -> AppCompatDelegate.MODE_NIGHT_NO
				else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
			}
		)
	}

	private fun setBlackBackground(blackBackground: Boolean) {
		if (blackBackground
			&& resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
		)
			window.decorView.setBackgroundColor(Color.BLACK)
		else {
			val typedValue = TypedValue()
			theme.resolveAttribute(android.R.attr.windowBackground, typedValue, true)
			if (typedValue.type >= TypedValue.TYPE_FIRST_COLOR_INT && typedValue.type <= TypedValue.TYPE_LAST_COLOR_INT)
				window.decorView.setBackgroundColor(typedValue.data)
		}
	}

	protected fun getAttr(@AttrRes attr: Int): Int {
		val typedValue = TypedValue()
		theme.resolveAttribute(attr, typedValue, true)
		return typedValue.data
	}

	private class CrashHandler(private val defaultUncaughtExceptionHandler: Thread.UncaughtExceptionHandler?) :
		Thread.UncaughtExceptionHandler {
		override fun uncaughtException(t: Thread, e: Throwable) {
			Log.e("BetterUntis", "Application crashed!", e)
			saveCrash(e)
			defaultUncaughtExceptionHandler?.uncaughtException(t, e)
		}

		private fun saveCrash(e: Throwable) {
			ErrorLogger.instance?.logThrowable(e)
		}
	}*/
}
