package com.sapuseven.untis.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.annotation.MainThread
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.lifecycle.DEFAULT_ARGS_KEY
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.MutableCreationExtras
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.sapuseven.untis.R
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.data.databases.entities.User
import com.sapuseven.untis.helpers.config.globalDataStore
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.modules.ThemeManager
import com.sapuseven.untis.preferences.dataStorePreferences
import com.sapuseven.untis.ui.activities.ActivityEvents
import com.sapuseven.untis.ui.activities.ActivityViewModel
import com.sapuseven.untis.ui.common.conditional
import com.sapuseven.untis.ui.functional.bottomInsets
import com.sapuseven.untis.ui.material.scheme.Scheme
import com.sapuseven.untis.ui.theme.animated
import com.sapuseven.untis.ui.theme.generateColorScheme
import com.sapuseven.untis.ui.theme.toColorScheme
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

abstract class BaseComposeActivity : ComponentActivity() {
	internal var user by mutableStateOf<User?>(null)
	internal var customThemeColor by mutableStateOf<Color?>(null) // Workaround to allow legacy views to respond to theme color changes
	internal var colorScheme by mutableStateOf<ColorScheme?>(null)
	internal lateinit var userDatabase: UserDatabase
	internal lateinit var timetableDatabaseInterface: TimetableDatabaseInterface

	private var dialogOpenUrl: MutableState<String?>? = null

	@Inject
	internal lateinit var themeManager: ThemeManager

	companion object {
		const val EXTRA_LONG_USER_ID = "com.sapuseven.untis.activities.profileid"
		private const val EXTRA_INT_BACKGROUND_COLOR =
			"com.sapuseven.untis.activities.backgroundcolor"

		val DATASTORE_KEY_USER_ID = longPreferencesKey("userid")


		fun openUrl(context: Context, url: String) {
			val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
				addCategory(Intent.CATEGORY_BROWSABLE)
				flags = Intent.FLAG_ACTIVITY_NEW_TASK
			}

			if (intent.resolveActivity(context.packageManager) != null) {
				context.startActivity(intent)
			}
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		userDatabase = UserDatabase.getInstance(applicationContext)

		runBlocking { // Not ideal, but works well enough
			loadInitialUser()
		}

		super.onCreate(savedInstanceState)

		WindowCompat.setDecorFitsSystemWindows(window, false)
	}

	@Composable
	fun withUser(
		invalidContent: @Composable () -> Unit = { InvalidProfileDialog() },
		content: @Composable (User) -> Unit
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
		val userDao = userDatabase.userDao()
		val user = userDao.getById(
			getUserIdExtra(intent) ?: loadSelectedUserId()
		) ?: userDao.getAll().getOrNull(0)

		user?.let {
			setUser(it)
		}
	}

	@OptIn(DelicateCoroutinesApi::class)
	fun setUser(user: User, save: Boolean = false) {
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
	fun AppThemeNew(
		//initialDarkTheme: Boolean = isSystemInDarkTheme(),
		navBarInset: Boolean = true,
		systemUiController: SystemUiController? = rememberSystemUiController(),
		dynamicColor: Boolean = true,
		content: @Composable () -> Unit
	) {
		val themeState by themeManager.themeState.collectAsState()

		val colorScheme = when {
			dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
				val context = LocalContext.current
				if (themeState.isDarkMode) dynamicDarkColorScheme(context) else dynamicLightColorScheme(
					context
				)
			}

			themeState.isDarkMode -> Scheme.dark(Color.Red.toArgb()).toColorScheme()
			else -> Scheme.light(Color.Red.toArgb()).toColorScheme()
		}

		Log.d("AppTheme", "dark mode: ${themeState.isDarkMode}")

		MaterialTheme(
			colorScheme = colorScheme.animated()
		) {
			val darkIcons = MaterialTheme.colorScheme.background.luminance() > .5f

			Surface(
				modifier = Modifier
					.fillMaxSize()
					.background(MaterialTheme.colorScheme.background)
					.conditional(navBarInset) {
						bottomInsets()
					}
					.windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)),
				content = content
			)

			SideEffect {
				systemUiController?.let {
					setSystemUiColor(it, Color.Transparent, darkIcons)
				}
			}
		}
	}

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

// TODO: Rename and merge with old one once all activities are migrated
abstract class BaseComposeActivityNew<VM : ActivityViewModel> : BaseComposeActivity() {
	protected abstract val viewModel: VM

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		viewModel.activityEvents().observe(this) { eventData ->
			when (eventData) {
				is ActivityEvents.Finish -> {
					if (eventData.data != null && eventData.resultCode != null)
						setResult(eventData.resultCode, eventData.data)
					else if (eventData.resultCode != null)
						setResult(eventData.resultCode)
					finish()
				}
			}
		}
	}
}

const val SAVED_STATE_INTENT_DATA = "data"

/**
 * A replacement for `viewModels()` that adds the data from the intent to the extras.
 */
@MainThread
public inline fun <reified VM : ViewModel> ComponentActivity.viewModelsWithData(
	noinline factoryProducer: (() -> ViewModelProvider.Factory)? = null
): Lazy<VM> = viewModels(
	factoryProducer = factoryProducer,
	extrasProducer = {
		MutableCreationExtras(defaultViewModelCreationExtras).apply {
			set(DEFAULT_ARGS_KEY,
				(get(DEFAULT_ARGS_KEY) ?: Bundle()).apply {
					putString(
						SAVED_STATE_INTENT_DATA,
						intent.dataString
					)
				}
			)
		}
	}
)
