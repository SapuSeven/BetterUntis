package com.sapuseven.untis.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.sapuseven.untis.BuildConfig
import com.sapuseven.untis.R
import com.sapuseven.untis.activities.SettingsActivity.Companion.EXTRA_STRING_PREFERENCE_HIGHLIGHT
import com.sapuseven.untis.activities.SettingsActivity.Companion.EXTRA_STRING_PREFERENCE_ROUTE
import com.sapuseven.untis.activities.main.DrawerItems
import com.sapuseven.untis.activities.main.DrawerText
import com.sapuseven.untis.data.databases.entities.User
import com.sapuseven.untis.data.timetable.PeriodData
import com.sapuseven.untis.data.timetable.TimegridItem
import com.sapuseven.untis.helpers.DateTimeUtils
import com.sapuseven.untis.helpers.config.deleteProfile
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.helpers.timetable.TimetableLoader
import com.sapuseven.untis.models.TimetableBookmark
import com.sapuseven.untis.models.untis.UntisDate
import com.sapuseven.untis.models.untis.timetable.PeriodElement
import com.sapuseven.untis.modules.ThemeManager
import com.sapuseven.untis.preferences.DataStorePreferences
import com.sapuseven.untis.preferences.dataStorePreferences
import com.sapuseven.untis.ui.activities.main.MainViewModel
import com.sapuseven.untis.ui.animations.fullscreenDialogAnimationEnter
import com.sapuseven.untis.ui.animations.fullscreenDialogAnimationExit
import com.sapuseven.untis.ui.common.AppScaffold
import com.sapuseven.untis.ui.common.DebugDesclaimerAction
import com.sapuseven.untis.ui.common.ProfileSelectorAction
import com.sapuseven.untis.ui.common.ReportsInfoBottomSheet
import com.sapuseven.untis.ui.common.Weekday
import com.sapuseven.untis.ui.common.disabled
import com.sapuseven.untis.ui.dialogs.ElementPickerDialogFullscreen
import com.sapuseven.untis.ui.dialogs.FeedbackDialog
import com.sapuseven.untis.ui.dialogs.TimetableItemDetailsDialog
import com.sapuseven.untis.ui.functional.bottomInsets
import com.sapuseven.untis.ui.functional.insetsPaddingValues
import com.sapuseven.untis.ui.material.scheme.Scheme
import com.sapuseven.untis.ui.models.NavItemShortcut
import com.sapuseven.untis.ui.navigation.AppNavHost
import com.sapuseven.untis.ui.navigation.AppRoutes
import com.sapuseven.untis.ui.preferences.convertRangeToPair
import com.sapuseven.untis.ui.theme.toColorScheme
import com.sapuseven.untis.ui.weekview.Event
import com.sapuseven.untis.ui.weekview.WeekViewColorScheme
import com.sapuseven.untis.ui.weekview.WeekViewCompose
import com.sapuseven.untis.ui.weekview.WeekViewHour
import com.sapuseven.untis.ui.weekview.pageIndexForDate
import com.sapuseven.untis.ui.weekview.startDateForPageIndex
import dagger.hilt.android.AndroidEntryPoint
import io.sentry.Breadcrumb
import io.sentry.Sentry
import io.sentry.SentryLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.joda.time.DateTimeConstants
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat
import java.lang.ref.WeakReference
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
	val viewModel: MainViewModel by viewModels()

	companion object {
		const val MESSENGER_PACKAGE_NAME = "com.untis.chat"

		const val EXTRA_STRING_PERIOD_ELEMENT = "com.sapuseven.untis.activities.main.element"
	}

	private val weekViewRefreshHandler = Handler(Looper.getMainLooper())

	@Inject
	internal lateinit var themeManager: ThemeManager

	// TODO
	/*private val weekViewUpdate = object : Runnable {
		override fun run() {
			weekView.invalidate()
			weekViewRefreshHandler.postDelayed(this, 60 * 1000)
		}
	}*/

	private val loginLauncher =
		registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
			if (it.resultCode == Activity.RESULT_OK) recreate() // TODO: Look at it.data for potential actions (e.g. show a specific timetable)
			else finish()
		}

	@OptIn(ExperimentalMaterial3Api::class)
	override fun onCreate(savedInstanceState: Bundle?) {
		enableEdgeToEdge()
		super.onCreate(savedInstanceState)

		setContent {
			AppTheme {
				Surface(
					modifier = Modifier.fillMaxSize()
					//.windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top))
					//.windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top))
				) {
					LaunchedEffect(Unit) {
						viewModel.activeUser.collect { user ->
							user?.let {
								viewModel.navigator.navigate(AppRoutes.Timetable(it.id)) {
									popUpTo(AppRoutes.Splash) { inclusive = true }
								}
							} ?: run {
								viewModel.navigator.navigate(AppRoutes.Login) {
									popUpTo(AppRoutes.Splash) { inclusive = true }
								}
							}
						}
					}

					AppNavHost(viewModel.navigator)
				}

				/*withUser(
								invalidContent = { login() }
							) { user ->
								/*val state =
									rememberMainAppState(
										user = user,
										contextActivity = this,
										//customThemeColor = customThemeColor,
										preferences = dataStorePreferences,
										globalPreferences = globalDataStore,
										//colorScheme = MaterialTheme.colorScheme//!! // Can't be null, AppTheme content isn't rendered if colorScheme is null
									)

								val prefs = dataStorePreferences
								LaunchedEffect(Unit) {
									val personalTimetableFlow = prefs.timetablePersonalTimetable.getValueFlow()

									state.scope.launch {
										personalTimetableFlow.collect { customTimetable ->
											if (user.anonymous || customTimetable != "") {
												val element = decodeStoredTimetableValue(customTimetable)
												val previousElement = state.personalTimetable?.first
												state.personalTimetable =
													element to element?.let { timetableDatabaseInterface.getLongName(it) }

												if (element != previousElement)
													state.displayElement(state.personalTimetable?.first, state.personalTimetable?.second)
											}
										}
									}
								}*/

								/*LaunchedEffect(user) {
									state.displayElement(
										state.personalTimetable?.first,
										state.personalTimetable?.second
									)
								}*/

								val state = rememberNewMainAppState(
									user = user,
									contextActivity = this,
									preferences = dataStorePreferences
								)

								//MainApp(state)
							}*/
			}
		}
	}

	private fun login() {
		loginLauncher.launch(Intent(this, LoginActivity::class.java).apply {
			//putUserIdExtra(this)
			//putBackgroundColorExtra(this)
		})
	}

	fun setSystemUiColor(
		systemUiController: SystemUiController,
		color: Color = Color.Transparent,
		darkIcons: Boolean = color.luminance() > 0.5f
	) {
		systemUiController.run {
			setSystemBarsColor(
				color = color, darkIcons = darkIcons
			)

			setNavigationBarColor(
				color = color, darkIcons = darkIcons
			)
		}
	}

	@Composable
	fun AppTheme(
		//initialDarkTheme: Boolean = isSystemInDarkTheme(),
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

		SideEffect {
			systemUiController?.let {
				setSystemUiColor(it, Color.Transparent)
			}
		}

		MaterialTheme(
			colorScheme = colorScheme,//.animated(),
			content = content
		) /*{
			val darkIcons = MaterialTheme.colorScheme.background.luminance() > .5f

			SideEffect {
				systemUiController?.let {
					setSystemUiColor(it, Color.Transparent, darkIcons)
				}
			}
		}*/
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Drawer(
	state: MainDrawerState,
	onShowTimetable: (Pair<PeriodElement?, String?>?) -> Unit,
	content: @Composable () -> Unit
) {
	val scope = rememberCoroutineScope()
	val drawerScrollState = rememberScrollState()

	var showElementPicker by remember {
		mutableStateOf<TimetableDatabaseInterface.Type?>(
			null
		)
	}

	var bookmarksElementPicker by remember {
		mutableStateOf<TimetableDatabaseInterface.Type?>(
			null
		)
	}

	val shortcutLauncher =
		rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
			val periodElement: PeriodElement? = activityResult.data?.let { intent ->
				Json.decodeFromString(
					PeriodElement.serializer(),
					intent.getStringExtra(MainActivity.EXTRA_STRING_PERIOD_ELEMENT) ?: ""
				)
			}

			periodElement?.let {
				onShowTimetable(it to state.timetableDatabaseInterface.getLongName(it))
			}
		}

	LaunchedEffect(state.drawerState) {
		snapshotFlow { state.drawerState.isOpen }.distinctUntilChanged().drop(1).collect {
			Log.i("Sentry", "Drawer isOpen: ${state.drawerState.isOpen}")
			Breadcrumb().apply {
				category = "ui.drawer"
				level = SentryLevel.INFO
				setData("isOpen", state.drawerState.isOpen)
				Sentry.addBreadcrumb(this)
			}
		}
	}

	BackHandler(enabled = state.drawerState.isOpen) {
		scope.launch {
			state.drawerState.close()
		}
	}

	ModalNavigationDrawer(
		gesturesEnabled = state.drawerGesturesEnabled,
		drawerState = state.drawerState,
		drawerContent = {
			ModalDrawerSheet(
				modifier = Modifier
                    .width(320.dp) // default: 360.dp
                    .fillMaxHeight()
                    .verticalScroll(drawerScrollState)
			) {
				Spacer(modifier = Modifier.height(24.dp))

				DrawerText(stringResource(id = R.string.all_favourites))

				NavigationDrawerItem(
					icon = {
						Icon(
							painterResource(id = R.drawable.all_prefs_personal),
							contentDescription = null
						)
					},
					label = { Text(stringResource(id = R.string.all_personal_timetable)) },
					selected = state.isPersonalTimetableDisplayed(),
					onClick = {
						state.closeDrawer()
						onShowTimetable(state.personalTimetable)
					},
					modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
				)

				var isBookmarkSelected = false
				state.getBookmarks().forEach { bookmark ->
					val isDisplayed = state.displayedElement.value?.let {
						it.id == bookmark.elementId && it.type == bookmark.elementType
					} == true
					isBookmarkSelected = isBookmarkSelected || isDisplayed

					NavigationDrawerItem(
						icon = {
							Icon(
								painter = painterResource(
									id = when (TimetableDatabaseInterface.Type.valueOf(
										bookmark.elementType
									)) {
										TimetableDatabaseInterface.Type.CLASS -> R.drawable.all_classes
										TimetableDatabaseInterface.Type.TEACHER -> R.drawable.all_teachers
										TimetableDatabaseInterface.Type.SUBJECT -> R.drawable.all_subject
										TimetableDatabaseInterface.Type.ROOM -> R.drawable.all_rooms
										else -> R.drawable.all_prefs_personal
									}
								), contentDescription = null
							)
						},
						badge = {
							IconButton(onClick = { state.bookmarkDeleteDialog.value = bookmark }) {
								Icon(
									painter = painterResource(id = R.drawable.all_bookmark_remove),
									contentDescription = "Remove Bookmark"
								) //TODO: Extract String resource
							}
						},
						label = { Text(text = bookmark.displayName) },
						selected = isDisplayed,
						onClick = {
							state.closeDrawer()
							val items = state.timetableDatabaseInterface.getElements(
								TimetableDatabaseInterface.Type.valueOf(bookmark.elementType)
							)
							val item = items.find {
								it.id == bookmark.elementId && it.type == bookmark.elementType
							}
							onShowTimetable(
								item to state.timetableDatabaseInterface.getLongName(item!!)
							)
						},
						modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
					)
				}

				NavigationDrawerItem(
					icon = {
						Icon(
							painterResource(id = R.drawable.all_add), contentDescription = null
						)
					},
					label = { Text(stringResource(id = R.string.maindrawer_bookmarks_add)) },
					selected = false,
					onClick = {
						state.closeDrawer()
						bookmarksElementPicker = TimetableDatabaseInterface.Type.CLASS
					},
					modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
				)

				DrawerText(stringResource(id = R.string.nav_all_timetables))

				DrawerItems(isMessengerAvailable = state.isMessengerAvailable(),
					disableTypeSelection = state.isPersonalTimetableDisplayed() || isBookmarkSelected,
					displayedElement = state.displayedElement.value,
					onTimetableClick = { item ->
						state.closeDrawer()
						showElementPicker = item.elementType
					},
					onShortcutClick = { item ->
						state.onShortcutItemClick(item, shortcutLauncher)
					})
			}
		},
		content = content
	)

	AnimatedVisibility(
		visible = showElementPicker != null,
		enter = fullscreenDialogAnimationEnter(),
		exit = fullscreenDialogAnimationExit()
	) {
		ElementPickerDialogFullscreen(
			title = { /*TODO*/ },
			timetableDatabaseInterface = state.timetableDatabaseInterface,
			onDismiss = { showElementPicker = null },
			onSelect = { item ->
				item?.let {
					onShowTimetable(
						item to state.timetableDatabaseInterface.getLongName(it)
					)
				} ?: run {
					onShowTimetable(state.personalTimetable)
				}
			},
			initialType = showElementPicker
		)
	}

	AnimatedVisibility(
		visible = bookmarksElementPicker != null,
		enter = fullscreenDialogAnimationEnter(),
		exit = fullscreenDialogAnimationExit()
	) {
		ElementPickerDialogFullscreen(
			title = { Text(stringResource(id = R.string.maindrawer_bookmarks_add)) },
			timetableDatabaseInterface = state.timetableDatabaseInterface,
			hideTypeSelectionPersonal = true,
			onDismiss = { bookmarksElementPicker = null },
			onSelect = { item ->
				item?.let {
					if (state.createBookmark(item)) onShowTimetable(
						item to state.timetableDatabaseInterface.getLongName(
							it
						)
					)
				}
			},
			initialType = bookmarksElementPicker
		)
	}

	state.bookmarkDeleteDialog.value?.let { bookmark ->
		AlertDialog(text = { Text(stringResource(id = R.string.main_dialog_delete_bookmark)) },
			onDismissRequest = { state.bookmarkDeleteDialog.value = null },
			confirmButton = {
				TextButton(onClick = {
					state.removeBookmark(bookmark)
				}) {
					Text(stringResource(id = R.string.all_delete))
				}
			},
			dismissButton = {
				TextButton(onClick = { state.bookmarkDeleteDialog.value = null }) {
					Text(stringResource(id = R.string.all_cancel))
				}
			})
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(state: NewMainAppState) {
	val containerColor by animateColorAsState(
		targetValue = MaterialTheme.colorScheme.background,
		animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
	)
	val snackbarHostState = remember { SnackbarHostState() }

	Drawer(state = state.mainDrawerState, onShowTimetable = {
		it.let { element ->
			state.setDisplayedElement(element?.first, element?.second)
		}
	}) {
		AppScaffold(containerColor = containerColor,
			snackbarHost = { SnackbarHost(snackbarHostState) },
			topBar = {
				CenterAlignedTopAppBar(title = { Text(state.getDisplayedName()) },
					navigationIcon = {
						IconButton(onClick = {
							state.openDrawer()
						}) {
							Icon(
								imageVector = Icons.Outlined.Menu,
								contentDescription = stringResource(id = R.string.main_drawer_open)
							)
						}
					},
					actions = {
						if (BuildConfig.DEBUG) DebugDesclaimerAction()

						ProfileSelectorAction(users = state.listUsers(),
							currentSelectionId = state.getCurrentUserId(),
							showProfileActions = true,
							onSelectionChange = {
								state.switchUser(it)
							},
							onActionEdit = {
								state.editUsers()
							})
					})
			}) { innerPadding ->
			Box(
				modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
			) {
				val density = LocalDensity.current
				val insets = insetsPaddingValues()
				val navBarHeight = remember {
					with(density) {
						(insets.calculateBottomPadding() + 48.dp).toPx()
					}
				}

				WeekViewCompose(
					events = state.weekViewEvents,
					onPageChange = { pageOffset ->
						state.onPageChange(pageOffset)
					},
					onReload = { pageOffset ->
						state.loadEvents(startDateForPageIndex(pageOffset))
					},
					onItemClick = { state.timetableItemDetailsDialog = it },
					startTime = state.weekViewPreferences.hourList.value.firstOrNull()?.startTime
						?: LocalTime.MIDNIGHT,
					endTime = state.weekViewPreferences.hourList.value.lastOrNull()?.endTime
						?: LocalTime.MIDNIGHT,
					endTimeOffset = navBarHeight,
					hourHeight = /*state.weekViewPreferences.hourHeight ?:*/ 72.dp,
					hourList = state.weekViewPreferences.hourList.value,
					dividerWidth = state.weekViewPreferences.dividerWidth,
					colorScheme = state.weekViewPreferences.colorScheme,
				)

				val timeColumnWidth = with(LocalDensity.current) {
					/*state.weekView.value?.config?.timeColumnWidth?.toDp()
						?: */48.dp
				}

				Text(
					text = state.lastRefreshText(),
					modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = timeColumnWidth + 8.dp, bottom = 8.dp)
                        .bottomInsets()
                        .disabled(state.isAnonymous)
				)

				IconButton(modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 8.dp)
                    .bottomInsets(), onClick = {
					state.showFeedback()
				}) {
					Icon(
						painter = painterResource(R.drawable.all_feedback),
						contentDescription = "Give feedback"
					)
				}

				if (state.feedbackDialog) FeedbackDialog(onDismiss = {
					state.feedbackDialog = false
				})

				if (state.isAnonymous) {
					Column(
						verticalArrangement = Arrangement.Center,
						horizontalAlignment = Alignment.CenterHorizontally,
						modifier = Modifier
                            .fillMaxSize()
                            .absolutePadding(left = 16.dp)
					) {
						Text(
							text = stringResource(id = R.string.main_anonymous_login_info_text),
							textAlign = TextAlign.Center,
							modifier = Modifier.padding(horizontal = 32.dp)
						)

						Button(
							onClick = state.onAnonymousSettingsClick,
							modifier = Modifier.padding(top = 16.dp)
						) {
							Text(text = stringResource(id = R.string.main_go_to_settings))
						}
					}
				}

				if (state.isLoading) CircularProgressIndicator(
					modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
				)
			}

			ReportsInfoBottomSheet()
		}
	}

	// TODO: Implement a smoother animation (see https://m3.material.io/components/dialogs/guidelines#007536b9-76b1-474a-a152-2f340caaff6f)
	AnimatedVisibility(
		visible = state.timetableItemDetailsDialog != null,
		enter = fullscreenDialogAnimationEnter(),
		exit = fullscreenDialogAnimationExit()
	) {
		// TODO: Incorrect insets
		TimetableItemDetailsDialog(timegridItems = remember {
			state.timetableItemDetailsDialog?.first ?: emptyList()
		},
			initialPage = remember {
				state.timetableItemDetailsDialog?.second ?: 0
			},
			user = state.user,
			timetableDatabaseInterface = state.timetableDatabaseInterface,
			onDismiss = {
				state.timetableItemDetailsDialog = null
				it?.let { state.setDisplayedElement(it) }
			})
	}

	/*AnimatedVisibility(
		visible = state.profileManagementDialog,
		enter = fullscreenDialogAnimationEnter(),
		exit = fullscreenDialogAnimationExit()
	) {
		ProfileManagementDialog(
			onDismiss = {
				state.profileManagementDialog = false
			}
		)
	}*/
}

class MainDrawerState(
	private val user: User,
	private val contextActivity: BaseComposeActivity,
	private val scope: CoroutineScope,
	val drawerState: DrawerState,
	val bookmarkDeleteDialog: MutableState<TimetableBookmark?>,
) {
	lateinit var displayedElement: MutableState<PeriodElement?>
	private val userDatabase = contextActivity.userDatabase
	val personalTimetable = getPersonalTimetableElement(user, contextActivity)

	var drawerGestureState = mutableStateOf(true)

	@OptIn(ExperimentalMaterial3Api::class)
	val drawerGesturesEnabled: Boolean
		get() = drawerGestureState.value || drawerState.isOpen

	val timetableDatabaseInterface: TimetableDatabaseInterface =
		contextActivity.timetableDatabaseInterface

	fun getBookmarks() = user.bookmarks

	fun isPersonalTimetableDisplayed() = displayedElement.value == personalTimetable?.first

	fun closeDrawer() {
		scope.launch { drawerState.close() }
	}

	fun isMessengerAvailable(): Boolean = false
	fun onShortcutItemClick(
		item: NavItemShortcut,
		shortcutLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>
	) {
		Log.i("Sentry", "Drawer onClick: ${item}")
		Breadcrumb().apply {
			category = "ui.drawer.click"
			level = SentryLevel.INFO
			setData("id", item.id)
			setData("label", item.label)
			Sentry.addBreadcrumb(this)
		}

		closeDrawer()
		if (item.target == null) {
			try {
				contextActivity.startActivity(
					contextActivity.packageManager.getLaunchIntentForPackage(
						MainActivity.MESSENGER_PACKAGE_NAME
					)
				)
			} catch (e: Exception) {
				try {
					contextActivity.startActivity(
						Intent(
							Intent.ACTION_VIEW,
							Uri.parse("market://details?id=${MainActivity.MESSENGER_PACKAGE_NAME}")
						)
					)
				} catch (e: Exception) {
					contextActivity.startActivity(
						Intent(
							Intent.ACTION_VIEW,
							Uri.parse("https://play.google.com/store/apps/details?id=${MainActivity.MESSENGER_PACKAGE_NAME}")
						)
					)
				}
			}
		} else {
			shortcutLauncher.launch(Intent(
				contextActivity, item.target
			).apply {
				contextActivity.putUserIdExtra(this, user.id)
				contextActivity.putBackgroundColorExtra(this)
			})
		}
	}

	fun createBookmark(item: PeriodElement): Boolean {
		val newBookmark = TimetableBookmark(
			elementId = item.id,
			elementType = TimetableDatabaseInterface.Type.valueOf(item.type).name,
			displayName = timetableDatabaseInterface.getLongName(item)
		)

		if (user.bookmarks.contains(newBookmark)) Toast.makeText(
			contextActivity, "Bookmark already exists", Toast.LENGTH_LONG
		) // TODO: Extract string resource
			.show()
		else {
			user.bookmarks = user.bookmarks.plus(newBookmark)
			userDatabase.userDao().update(user)
			return true
		}

		return false
	}

	fun removeBookmark(bookmark: TimetableBookmark) {
		user.bookmarks = user.bookmarks.minus(bookmark)
		userDatabase.userDao().update(user)
		bookmarkDeleteDialog.value = null
	}
}

class NewMainAppState @OptIn(ExperimentalMaterial3Api::class) constructor(
	internal val user: User,
	private val contextActivity: BaseComposeActivity,
	private val preferences: DataStorePreferences,
	private val scope: CoroutineScope,
	val mainDrawerState: MainDrawerState,
	internal val weekViewPreferences: WeekViewPreferences,
	private val colorScheme: ColorScheme,
) {
	companion object {
		private const val MINUTE_MILLIS: Int = 60 * 1000
		private const val HOUR_MILLIS: Int = 60 * MINUTE_MILLIS
		private const val DAY_MILLIS: Int = 24 * HOUR_MILLIS

		private const val UNTIS_DEFAULT_COLOR = "#f49f25"

		private val DATASTORE_KEY_WEEKVIEW_SCALE = intPreferencesKey("weekView.hourHeight")
	}

	private val userId = user.id
	private val userDatabase = contextActivity.userDatabase
	internal val timetableDatabaseInterface: TimetableDatabaseInterface =
		contextActivity.timetableDatabaseInterface
	private val timetableLoader = TimetableLoader(
		context = WeakReference(contextActivity),
		user = user,
		timetableDatabaseInterface = contextActivity.timetableDatabaseInterface
	)
	var lastSelectedDate: LocalDate = LocalDate.now()

	private val defaultDisplayedName = user.getDisplayedName(contextActivity)
	private val personalTimetable = getPersonalTimetableElement(user, contextActivity)
	private var displayedElement: MutableState<PeriodElement?> =
		mutableStateOf(personalTimetable?.first)
	private var displayedName: MutableState<String> = mutableStateOf(defaultDisplayedName)
	private var loading by mutableStateOf(0)

	var timetableItemDetailsDialog by mutableStateOf<Pair<List<PeriodData>, Int>?>(null)
	var profileManagementDialog by mutableStateOf(false)
	var feedbackDialog by mutableStateOf(false)

	var weekViewPage by mutableStateOf<Int>(0)
	var weekViewEvents = mutableStateMapOf<LocalDate, List<Event>>()
	private var weekViewRefreshTimestamps = mutableStateMapOf<Int, Long>()

	init {
		mainDrawerState.displayedElement = displayedElement
	}

	val isPersonalTimetable: Boolean
		get() = displayedElement.value == personalTimetable?.first

	val isAnonymous: Boolean
		get() = personalTimetable != null && displayedElement.value == null

	val isLoading: Boolean
		get() = loading > 0

	fun getCurrentUserId() = userId

	fun editUsers() {
		profileManagementDialog = true
	}

	fun editUser(
		user: User?, loginLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>
	) {
		loginLauncher.launch(Intent(contextActivity,
			user?.let { LoginDataInputActivity::class.java } ?: LoginActivity::class.java).apply {
			user?.id?.let { contextActivity.putUserIdExtra(this, it) }
			contextActivity.putBackgroundColorExtra(this)
			putExtra(LoginActivity.EXTRA_BOOLEAN_SHOW_BACK_BUTTON, true)
		})
	}

	fun deleteUser(
		user: User
	) {
		scope.launch {
			userDatabase.userDao().delete(user)
			contextActivity.deleteProfile(user.id)
			if (userDatabase.userDao().getAll().isEmpty()) contextActivity.recreate()
		}
	}

	fun switchUser(user: User) {
		contextActivity.setUser(user, true)
	}

	fun listUsers() = userDatabase.userDao().getAll()

	fun getDisplayedName(): String = displayedName.value
	fun openDrawer() {
		scope.launch { mainDrawerState.drawerState.open() }
	}

	fun setDisplayedElement(element: PeriodElement?, displayName: String? = null) {
		weekViewEvents.clear()
		scope.launch {
			onPageChange(weekViewPage)
		}
		displayedElement.value = element
		displayedName.value =
			displayName ?: element?.let { timetableDatabaseInterface.getLongName(it) }
				?: defaultDisplayedName
	}

	// WeekView
	data class WeeklyTimetableItems(
		var items: List<TimegridItem> = emptyList(),
		var lastUpdated: Long = 0,
		var dateRange: Pair<UntisDate, UntisDate>? = null
	)

	private suspend fun prepareItems(
		items: List<TimegridItem>
	): List<TimegridItem> {
		val newItems = mergeItems(items.mapNotNull { item ->
			if (item.periodData.isCancelled() && preferences.timetableHideCancelled.getValue()) return@mapNotNull null

			if (preferences.timetableSubstitutionsIrregular.getValue()) {
				item.periodData.apply {
					forceIrregular =
						classes.find { it.id != it.orgId } != null || teachers.find { it.id != it.orgId } != null || subjects.find { it.id != it.orgId } != null || rooms.find { it.id != it.orgId } != null || preferences.timetableBackgroundIrregular.getValue() && item.periodData.element.backColor != UNTIS_DEFAULT_COLOR
				}
			}
			item
		})
		colorItems(newItems)
		return newItems
	}

	private fun mergeItems(items: List<TimegridItem>): List<TimegridItem> {
		val days = user.timeGrid.days
		val itemGrid: Array<Array<MutableList<TimegridItem>>> =
			Array(days.size) { Array(days.maxByOrNull { it.units.size }!!.units.size) { mutableListOf() } }
		val leftover: MutableList<TimegridItem> = mutableListOf()

		// TODO: Check if the day from the Untis API is always an english string
		val firstDayOfWeek =
			DateTimeConstants.MONDAY //DateTimeFormat.forPattern("EEE").withLocale(Locale.ENGLISH).parseDateTime(days.first().day).dayOfWeek

		// Put all items into a two dimensional array depending on day and hour
		items.forEach { item ->
			val startDateTime = item.periodData.element.startDateTime.toLocalDateTime()
			val endDateTime = item.periodData.element.endDateTime.toLocalDateTime()

			val day = endDateTime.dayOfWeek - firstDayOfWeek

			if (day < 0 || day >= days.size) return@forEach

			val thisUnitStartIndex = days[day].units.indexOfFirst {
				it.startTime.time == startDateTime.toString(DateTimeUtils.tTimeNoSeconds())
			}

			val thisUnitEndIndex = days[day].units.indexOfFirst {
				it.endTime.time == endDateTime.toString(DateTimeUtils.tTimeNoSeconds())
			}

			if (thisUnitStartIndex != -1 && thisUnitEndIndex != -1) itemGrid[day][thisUnitStartIndex].add(
				item
			)
			else leftover.add(item)
		}

		val newItems = mutableListOf<TimegridItem>()
		newItems.addAll(leftover) // Add items that didn't fit inside the timegrid. These will always be single lessons.
		itemGrid.forEach { unitsOfDay ->
			unitsOfDay.forEachIndexed { unitIndex, items ->
				items.forEach {
					var i = 1
					while (unitIndex + i < unitsOfDay.size && it.mergeWith(unitsOfDay[unitIndex + i])) i++
				}

				newItems.addAll(items)
			}
		}
		return newItems
	}

	private suspend fun colorItems(
		items: List<TimegridItem>
	) {
		val regularColor = weekViewPreferences.backgroundRegular.value
		val regularPastColor = weekViewPreferences.backgroundRegularPast.value
		val examColor = weekViewPreferences.backgroundExam.value
		val examPastColor = weekViewPreferences.backgroundExamPast.value
		val cancelledColor = weekViewPreferences.backgroundCancelled.value
		val cancelledPastColor = weekViewPreferences.backgroundCancelledPast.value
		val irregularColor = weekViewPreferences.backgroundIrregular.value
		val irregularPastColor = weekViewPreferences.backgroundIrregularPast.value

		val useDefault = preferences.schoolBackground.getValue()

		items.forEach { item ->
			val defaultColor = android.graphics.Color.parseColor(item.periodData.element.backColor)
			val defaultTextColor =
				android.graphics.Color.parseColor(item.periodData.element.foreColor)

			item.color = when {
				item.periodData.isExam() -> if (useDefault.contains("exam")) defaultColor else examColor
				item.periodData.isCancelled() -> if (useDefault.contains("cancelled")) defaultColor else cancelledColor
				item.periodData.isIrregular() -> if (useDefault.contains("irregular")) defaultColor else irregularColor
				else -> if (useDefault.contains("regular")) defaultColor else regularColor
			}

			item.pastColor = when {
				item.periodData.isExam() -> if (useDefault.contains("exam")) defaultColor.darken(
					0.25f
				) else examPastColor

				item.periodData.isCancelled() -> if (useDefault.contains("cancelled")) defaultColor.darken(
					0.25f
				) else cancelledPastColor

				item.periodData.isIrregular() -> if (useDefault.contains("irregular")) defaultColor.darken(
					0.25f
				) else irregularPastColor

				else -> if (useDefault.contains("regular")) defaultColor.darken(0.25f) else regularPastColor
			}

			item.textColor = when {
				item.periodData.isExam() -> if (useDefault.contains("exam")) defaultTextColor else colorOn(
					Color(examColor)
				).toArgb()

				item.periodData.isCancelled() -> if (useDefault.contains("cancelled")) defaultTextColor else colorOn(
					Color(cancelledColor)
				).toArgb()

				item.periodData.isIrregular() -> if (useDefault.contains("irregular")) defaultTextColor else colorOn(
					Color(irregularColor)
				).toArgb()

				else -> if (useDefault.contains("regular")) defaultTextColor else colorOn(
					Color(
						regularColor
					)
				).toArgb()
			}
		}
	}

	private fun colorOn(color: Color): Color {
		return when (color.copy(alpha = 1f)) {
			colorScheme.primary -> colorScheme.onPrimary
			colorScheme.secondary -> colorScheme.onSecondary
			colorScheme.tertiary -> colorScheme.onTertiary
			else -> if (ColorUtils.calculateLuminance(color.toArgb()) < 0.5) Color.White else Color.Black
		}.copy(alpha = color.alpha)
	}

	private suspend fun loadTimetable(
		loader: TimetableLoader,
		target: TimetableLoader.TimetableLoaderTarget,
		forceRefresh: Boolean = false,
		onItemsReceived: (timetableItems: TimetableLoader.TimetableItems) -> Unit
	) {
		loader.loadAsync(
			target,
			preferences.proxyHost.getValue(),
			loadFromCache = !forceRefresh,
			loadFromServer = forceRefresh || preferences.connectivityRefreshInBackground.getValue(),
			onItemsReceived = onItemsReceived
		)
	}

	private suspend fun loadTimetableFlow(
		loader: TimetableLoader,
		target: TimetableLoader.TimetableLoaderTarget,
		forceRefresh: Boolean = false
	): Flow<TimetableLoader.TimetableItems> = loader.loadFlow(
		target,
		preferences.proxyHost.getValue(),
		loadFromCache = !forceRefresh,
		loadFromServer = forceRefresh || preferences.connectivityRefreshInBackground.getValue(),
	)

	@OptIn(ExperimentalCoroutinesApi::class)
	suspend fun loadEventsFlow(
		startDate: LocalDate, endDate: LocalDate
	): Flow<Pair<Long, List<Event>>> {
		val dateRange = UntisDate.fromLocalDate(LocalDate(startDate)) to UntisDate.fromLocalDate(
			LocalDate(endDate)
		)

		return displayedElement.value?.let { element ->
			loadTimetableFlow(
				timetableLoader, TimetableLoader.TimetableLoaderTarget(
					dateRange.first, dateRange.second, element.id, // TODO: Handle nullability
					element.type
				), false
			).map {
				it.timestamp to prepareItems(it.items).map { item -> item.toEvent() }
			}
		} ?: emptyFlow()

		/*try {
			displayedElement.value?.let { element ->
					loadTimetableFlow(
						timetableLoader,
						TimetableLoader.TimetableLoaderTarget(
							dateRange.first,
							dateRange.second,
							element.id,
							element.type
						),
						false//forceRefresh
					)
						.onEach { timetableItems ->
							setItems(runBlocking { prepareItems(timetableItems.items) }.map { item -> item.toEvent() })
						}
			}
		} catch (e: TimetableLoader.TimetableLoaderException) {
			//cont.resumeWithException(e)
		}*/
	}

	suspend fun loadEvents(startDate: LocalDate = startDateForPageIndex(weekViewPage)) = loadEvents(
		startDate, startDate.plusDays(weekViewPreferences.weekLength.value)
	)

	suspend fun loadEvents(startDate: LocalDate, endDate: LocalDate) {
		Log.d("WeekView", "Loading items for $startDate")
		loading++
		loadEventsFlow(startDate, endDate).onCompletion {
			loading--
			Log.d("WeekView", "All items received for $startDate")
		}.collect {
			Log.d("WeekView", "New items received for $startDate")
			weekViewRefreshTimestamps[pageIndexForDate(startDate)] = it.first
			weekViewEvents[startDate] = it.second
		}
	}

	suspend fun loadAllEvents(pageOffset: Int = weekViewPage) {
		coroutineScope {
			((pageOffset - 1)..(pageOffset + 1)).map {
				async {
					val startDate = startDateForPageIndex(it)
					Log.d(
						"WeekView",
						"Items available for $startDate: ${weekViewEvents.contains(startDate)}"
					)
					if (!weekViewEvents.contains(startDate)) loadEvents(startDate)
				}
			}.awaitAll()
		}
	}

	// Last refresh
	@Composable
	fun lastRefreshText() = stringResource(
		id = R.string.main_last_refreshed,
		if (weekViewRefreshTimestamps[weekViewPage] ?: 0L > 0L) formatTimeDiff(Instant.now().millis - weekViewRefreshTimestamps[weekViewPage]!!)
		else stringResource(id = R.string.main_last_refreshed_never)
	)

	fun showFeedback() {
		feedbackDialog = true
	}

	@OptIn(ExperimentalComposeUiApi::class)
	@Composable
	private fun formatTimeDiff(diff: Long): String {
		return when {
			diff < MINUTE_MILLIS -> stringResource(R.string.main_time_diff_just_now)
			diff < HOUR_MILLIS -> pluralStringResource(
				R.plurals.main_time_diff_minutes,
				((diff / MINUTE_MILLIS).toInt()),
				diff / MINUTE_MILLIS
			)

			diff < DAY_MILLIS -> pluralStringResource(
				R.plurals.main_time_diff_hours, ((diff / HOUR_MILLIS).toInt()), diff / HOUR_MILLIS
			)

			else -> pluralStringResource(
				R.plurals.main_time_diff_days, ((diff / DAY_MILLIS).toInt()), diff / DAY_MILLIS
			)
		}
	}

	private fun Int.darken(ratio: Float) = ColorUtils.blendARGB(this, Color.Black.toArgb(), ratio)

	// Event listeners
	val onAnonymousSettingsClick: () -> Unit = {
		contextActivity.startActivity(Intent(
			contextActivity, SettingsActivity::class.java
		).apply {
			contextActivity.putUserIdExtra(this, user.id)
			putExtra(
				EXTRA_STRING_PREFERENCE_ROUTE, "preferences_timetable"
			)
			putExtra(
				EXTRA_STRING_PREFERENCE_HIGHLIGHT, "preference_timetable_personal_timetable"
			)
			contextActivity.putBackgroundColorExtra(this)
		})
	}

	val onPageChange: suspend (Int) -> Unit = { pageOffset ->
		weekViewPage = pageOffset
		Log.d("WeekView", "Page changed to $pageOffset")
		loadAllEvents(pageOffset)
	}

	data class WeekViewPreferences(
		var hourList: State<List<WeekViewHour>>,
		var colorScheme: WeekViewColorScheme,
		var dividerWidth: Float,
		var backgroundRegular: State<Int>,
		var backgroundRegularPast: State<Int>,
		var backgroundExam: State<Int>,
		var backgroundExamPast: State<Int>,
		var backgroundCancelled: State<Int>,
		var backgroundCancelledPast: State<Int>,
		var backgroundIrregular: State<Int>,
		var backgroundIrregularPast: State<Int>,
		var weekLength: State<Int>,
		var weekStartOffset: State<Int>,
		/*var hourHeight: Dp,
		var startTime: LocalTime,
		var endTime: LocalTime,
		var endTimeOffset: Float,*/
	)
}

/*@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseComposeActivity.MainApp(state: MainAppState) {
	val snackbarHostState = remember { SnackbarHostState() }

	/*if (state.preferences.doubleTapToExit.getState().value)
		BackPressConfirm(snackbarHostState)*/

	Drawer(
		state = state,
		onShowTimetable = {
			it.let { element ->
				state.displayElement(element?.first, element?.second)
			}
		}
	) {
		AppScaffold(
			snackbarHost = { SnackbarHost(snackbarHostState) },
			topBar = {
				CenterAlignedTopAppBar(
					title = { Text(state.displayedName.value) },
					navigationIcon = {
						IconButton(onClick = {
							state.scope.launch { state.drawerState.open() }
						}) {
							Icon(
								imageVector = Icons.Outlined.Menu,
								contentDescription = stringResource(id = R.string.main_drawer_open)
							)
						}
					},
					actions = {
						ProfileSelectorAction(
							users = state.listUsers(),
							currentSelectionId = state.user.id,
							showProfileActions = true,
							onSelectionChange = {
								state.switchUser(it)
							},
							onActionEdit = {
								state.editUsers()
							}
						)
					}
				)
			}
		) { innerPadding ->
			Box(
				modifier = Modifier
					.padding(innerPadding)
					.fillMaxSize()
			) {
				val density = LocalDensity.current
				val insets = insetsPaddingValues()
				val navBarHeight = remember {
					with(density) {
						(insets.calculateBottomPadding() + 48.dp).toPx()
					}
				}

				Text(
					text = state.weekViewEvents.size.toString(),
				)

				/*val color = dataStorePreferences.backgroundRegular.getState()*/
				//WeekViewTest(bg = Color(state.weekViewPreferences.backgroundRegular.value))

				WeekViewCompose(
						events = state.weekViewEvents,
						onPageChange = { pageOffset ->
							state.weekViewPage.value = pageOffset
							state.loadAllEvents(pageOffset)
						},
						onReload = { pageOffset ->
							state.loadEvents(state.startDateForPage(pageOffset))
						},
						startTime = state.weekViewPreferences.hourList.value.firstOrNull()?.startTime
							?: LocalTime.MIDNIGHT,
						endTime = state.weekViewPreferences.hourList.value.lastOrNull()?.endTime
							?: LocalTime.MIDNIGHT,
						endTimeOffset = navBarHeight,
						hourHeight = /*state.weekViewPreferences.hourHeight ?:*/ 72.dp,
						hourList = state.weekViewPreferences.hourList.value,
						dividerWidth = state.weekViewPreferences.dividerWidth,
						dividerColor = state.weekViewPreferences.dividerColor,
					)

					val timeColumnWidth = with(LocalDensity.current) {
						/*state.weekView.value?.config?.timeColumnWidth?.toDp()
							?: */48.dp
					}

					Text(
						text = state.lastRefreshText(),
						modifier = Modifier
							.align(Alignment.BottomStart)
							.padding(start = timeColumnWidth + 8.dp, bottom = 8.dp)
							.bottomInsets()
							.disabled(state.isAnonymous)
					)

					/*Text(
						text = state.testValue.value,
						modifier = Modifier
							.align(Alignment.BottomEnd)
							.padding(end = 52.dp, bottom = 8.dp)
							.bottomInsets()
					)*/

					if (state.isAnonymous) {
						Column(
							verticalArrangement = Arrangement.Center,
							horizontalAlignment = Alignment.CenterHorizontally,
							modifier = Modifier
								.fillMaxSize()
								.absolutePadding(left = 16.dp)
						) {
							Text(
								text = stringResource(id = R.string.main_anonymous_login_info_text),
								textAlign = TextAlign.Center,
								modifier = Modifier
									.padding(horizontal = 32.dp)
							)

							Button(
								onClick = {
									state.contextActivity.startActivity(
										Intent(
											state.contextActivity,
											SettingsActivity::class.java
										).apply {
											state.contextActivity.putUserIdExtra(this, state.user.id)
											putExtra(
												EXTRA_STRING_PREFERENCE_ROUTE,
												"preferences_timetable"
											)
											putExtra(
												EXTRA_STRING_PREFERENCE_HIGHLIGHT,
												"preference_timetable_personal_timetable"
											)
											state.contextActivity.putBackgroundColorExtra(this)
										}
									)
								},
								modifier = Modifier
									.padding(top = 16.dp)
							) {
								Text(text = stringResource(id = R.string.main_go_to_settings))
							}
						}
					}

					/*if (state.isLoading)
						CircularProgressIndicator(
							modifier = Modifier
								.align(Alignment.BottomEnd)
								.padding(8.dp)
						)*/
			}

			ReportsInfoBottomSheet()
		}
	}

}
*/
class MainAppState @OptIn(ExperimentalMaterial3Api::class) constructor(
	val user: User,
	val contextActivity: BaseComposeActivity,    /*val weekViewSwipeRefresh: MutableState<WeekViewSwipeRefreshLayout?>,
	val weekView: MutableState<WeekView<TimegridItem>?>,
	val context: Context,*/
	val scope: CoroutineScope,
	val colorScheme: ColorScheme,
	//val currentDensity: Density,
	val preferences: DataStorePreferences,
	val globalPreferences: DataStore<Preferences>,
	var personalTimetable: Pair<PeriodElement?, String?>?,
	val defaultDisplayedName: String,
	val drawerState: DrawerState,
	var drawerGestureState: MutableState<Boolean>,    /*val loading: MutableState<Int>,
	val currentWeekIndex: MutableState<Int>,*/
	val lastRefreshTimestamp: MutableState<Long>,
	//val weeklyTimetableItems: SnapshotStateMap<Int, WeeklyTimetableItems?>,
	//val timetableItemDetailsDialog: MutableState<Pair<List<PeriodData>, Int>?>,
	//val showDatePicker: MutableState<Boolean>,
	val profileManagementDialog: MutableState<Boolean>,
	val bookmarkDeleteDialog: MutableState<TimetableBookmark?>,
	//val weekViewPreferences: MainAppState.WeekViewPreferences,
	val weekViewEvents: SnapshotStateMap<LocalDate, List<Event>>,
	val weekViewPage: MutableState<Int>
) {

	/*init {
	    weekViewEvents.clear()
	}*/

	private val userDatabase = contextActivity.userDatabase
	private var drawerGestures by drawerGestureState

	val timetableLoader = TimetableLoader(
		context = WeakReference(contextActivity),
		user = user,
		timetableDatabaseInterface = contextActivity.timetableDatabaseInterface
	)

	val timetableDatabaseInterface: TimetableDatabaseInterface =
		contextActivity.timetableDatabaseInterface

	//var lastSelectedDate: LocalDate = LocalDate.now()

	var displayedElement: MutableState<PeriodElement?> = mutableStateOf(personalTimetable?.first)
	var displayedName: MutableState<String> =
		mutableStateOf(/*personalTimetable?.second ?:*/ defaultDisplayedName)

	val isPersonalTimetable: Boolean
		get() = displayedElement.value == personalTimetable?.first

	val isAnonymous: Boolean
		get() = personalTimetable != null && displayedElement.value == null

	/*private var isRefreshing: Boolean
		get() = weekViewSwipeRefresh.value?.isRefreshing ?: false
		set(value) {
			weekViewSwipeRefresh.value?.isRefreshing = value
		}

	private var shouldUpdateWeekView = true*/

	val isMessengerAvailable: Boolean
		get() {            /*for (item in this.weeklyTimetableItems.values) {
				if (item != null) {
					for (it in item.items) {
						if (it.data?.periodData?.element?.messengerChannel != null) {
							return true
						}
						break
					}
				}

			}*/
			return false
		}

	@OptIn(ExperimentalMaterial3Api::class)
	val drawerGesturesEnabled: Boolean
		get() = drawerGestures || drawerState.isOpen

	@OptIn(ExperimentalMaterial3Api::class)
	fun closeDrawer() {
		scope.launch { drawerState.close() }
	}

	fun displayElement(element: PeriodElement?, name: String? = null) {
		displayedElement.value = element
		displayedName.value = name ?: element?.let { timetableDatabaseInterface.getLongName(it) }
			?: defaultDisplayedName

		weekViewEvents.clear()
		scope.launch {
			//loadAllEvents()
		}
	}

	fun editUsers() {
		profileManagementDialog.value = true
	}

	fun switchUser(user: User) {
		contextActivity.setUser(user, true)
		scope.launch {
			//loadAllEvents()
		}
	}

	fun listUsers(): List<User> {
		return userDatabase.userDao().getAll()
	}


	/*private suspend fun loadWeeklyTimetableItems(
		loader: TimetableLoader?,
		startDate: LocalDate,
		endDate: LocalDate,
		element: PeriodElement,
		onItemsChanged: (items: WeeklyTimetableItems) -> Unit,
		forceRefresh: Boolean = false
	) {
		loader?.let {
			val dateRange =
				UntisDate.fromLocalDate(LocalDate(startDate)) to
						UntisDate.fromLocalDate(LocalDate(endDate))

			try {
				loadTimetable(
					loader,
					TimetableLoader.TimetableLoaderTarget(
						dateRange.first,
						dateRange.second,
						element.id,
						element.type
					),
					forceRefresh
				) { timetableItems ->
					/*for (item in timetableItems.items) {
						if (item.periodData.element.messengerChannel != null) {
							navigationview_main.menu.findItem(R.id.nav_messenger).isVisible = true
							break
						}
					}*/
					onItemsChanged(
						WeeklyTimetableItems(
							dateRange = dateRange,
							items = runBlocking { prepareItems(timetableItems.items) },
							lastUpdated = timetableItems.timestamp
						)
					)
				}
			} catch (e: TimetableLoader.TimetableLoaderException) {
				Log.e(
					"MainActivity",
					e.untisErrorMessage ?: e.message ?: "unknown error"
				)

				// TODO
				when (e.untisErrorCode) {
					/*TimetableLoader.CODE_CACHE_MISSING -> timetableLoader!!.repeat(
					it.requestId,
					TimetableLoader.FLAG_LOAD_SERVER,
					proxyHost
				)*/
					else -> {
						/*Snackbar.make(
						content_main,
						if (code != null) ErrorMessageDictionary.getErrorMessage(
							resources,
							code
						) else message
							?: getString(R.string.all_error),
						Snackbar.LENGTH_INDEFINITE
					)
						.setAction("Show") {
							ErrorReportingDialog(this).showRequestErrorDialog(
								requestId,
								code,
								message
							)
						}
						.show()*/
					}
				}
			}
		}
	}

	private fun convertLocalDateToWeekIndex(date: LocalDate) =
		date.year * 100 + date.dayOfYear.floorDiv(7) + 1

	private fun convertWeekIndexToDateTime(weekIndex: Int) =
		DateTime.now().withYear(weekIndex.floorDiv(100)).withDayOfYear(weekIndex % 100 * 7)

	/*private fun onRefresh() {
		displayedElement.value?.let { element ->
			val weekIndex = currentWeekIndex.value

			weeklyTimetableItems[weekIndex]?.dateRange?.let { dateRange ->
				scope.launch {
					loadWeeklyTimetableItems(
						timetableLoader,
						dateRange.first.toLocalDate(),
						dateRange.second.toLocalDate(),
						element,
						forceRefresh = true,
						onItemsChanged = { items ->
							weeklyTimetableItems[weekIndex] = items
							weekView.value?.notifyDataSetChanged()
						}
					)
					isRefreshing = false
				}
			}
		}
	}*/

	@Composable
	fun <T> loadWeekViewPreferences(
		weekView: WeekView<T>?,
		dataStorePreferences: DataStorePreferences
	) {
		val currentDensity = LocalDensity.current
		val scope = rememberCoroutineScope()

		with(dataStorePreferences) {
			val flingEnable = flingEnable.getValueFlow()
			val snapToDays = weekSnapToDays.getValueFlow()
			val weekRange = weekCustomRange.getValueFlow()
			val numberOfVisibleDays = weekCustomLength.getValueFlow()
			val eventTextColor = timetableItemTextLight.getValueFlow()
			val pastBackgroundColor = backgroundPast.getValueFlow()
			val futureBackgroundColor = backgroundFuture.getValueFlow()
			val nowLineColor = marker.getValueFlow()
			val minimalTimeColumn = timetableHideTimeStamps.getValueFlow()
			val eventGap = timetableItemPadding.getValueFlow()
			val overlappingEventGap = timetableItemPaddingOverlap.getValueFlow()
			val eventCornerRadius = timetableItemCornerRadius.getValueFlow()
			val eventSecondaryTextCentered = timetableCenteredLessonInfo.getValueFlow()
			val eventTextBold = timetableBoldLessonName.getValueFlow()
			val eventTextSize = timetableLessonNameFontSize.getValueFlow()
			val eventSecondaryTextSize = timetableLessonInfoFontSize.getValueFlow()
			val timetableRange = timetableRange.getValueFlow()
			val timetableRangeIndexReset = timetableRangeIndexReset.getValueFlow()

			weekView?.let {
				val navBarHeight = with(LocalDensity.current) {
					(insetsPaddingValues().calculateBottomPadding() + 32.dp).toPx()
				}.roundToInt()

				scope.launch {
					flingEnable.collect { weekView.horizontalFlingEnabled = it }
				}

				scope.launch {
					snapToDays.combine(numberOfVisibleDays) { f1, f2 ->
						!f1 && f2.roundToInt() != 1
					}.collect { weekView.snapToWeek = it }
				}

				scope.launch {
					weekRange.collect {
						weekView.weekLength = it.size.zeroToNull ?: user.timeGrid.days.size
						weekView.firstDayOfWeek =
							it.map { day -> Weekday.valueOf(day) }.minOrNull()?.ordinal
								?: DateTimeFormat.forPattern("E")
									.withLocale(Locale.ENGLISH) // TODO: Correct locale?
									.parseDateTime(user.timeGrid.days[0].day).dayOfWeek
					}
				}

				scope.launch {
					numberOfVisibleDays.combine(snapToDays) { numberOfVisibleDaysValue, snapToDaysValue ->
						// Ignore numberOfVisibleDays when snapping to weeks
						if (snapToDaysValue) numberOfVisibleDaysValue else 0f
					}.collect {
						weekView.numberOfVisibleDays =
							it.roundToInt().zeroToNull ?: weekView.weekLength
					}
				}

				scope.launch {
					eventTextColor.collect {
						weekView.eventTextColor = (if (it) Color.White else Color.Black).toArgb()
					}
				}

				scope.launch {
					pastBackgroundColor.collect { weekView.pastBackgroundColor = it }
				}

				scope.launch {
					futureBackgroundColor.collect { weekView.futureBackgroundColor = it }
				}

				scope.launch {
					nowLineColor.collect { weekView.nowLineColor = it }
				}

				scope.launch {
					minimalTimeColumn.collect { weekView.timeColumnVisibility = !it }
				}

				scope.launch {
					eventGap.collect {
						with(currentDensity) {
							weekView.columnGap = it.dp.toPx().roundToInt()
						}
					}
				}

				scope.launch {
					overlappingEventGap.collect {
						with(currentDensity) {
							weekView.overlappingEventGap = it.dp.toPx().roundToInt()
						}
					}
				}

				scope.launch {
					eventCornerRadius.collect {
						with(currentDensity) {
							weekView.eventCornerRadius = it.dp.toPx().roundToInt()
						}
					}
				}

				scope.launch {
					eventSecondaryTextCentered.collect { weekView.eventSecondaryTextCentered = it }
				}

				scope.launch {
					eventTextBold.collect { weekView.eventTextBold = it }
				}

				scope.launch {
					eventTextSize.collect {
						with(currentDensity) {
							weekView.eventTextSize = it.sp.toPx()
						}
					}
				}

				scope.launch {
					eventSecondaryTextSize.collect {
						with(currentDensity) {
							weekView.eventSecondaryTextSize = it.sp.toPx()
						}
					}
				}

				scope.launch {
					merge(
						preferences.backgroundRegular.getValueFlow(),
						preferences.backgroundRegularPast.getValueFlow(),
						preferences.backgroundExam.getValueFlow(),
						preferences.backgroundExamPast.getValueFlow(),
						preferences.backgroundCancelled.getValueFlow(),
						preferences.backgroundCancelledPast.getValueFlow(),
						preferences.backgroundIrregular.getValueFlow(),
						preferences.backgroundIrregularPast.getValueFlow(),
						preferences.schoolBackground.getValueFlow()
					).collect {
						/*weeklyTimetableItems.map {
							it.key to it.value?.let { value -> colorItems(value.items) }
						}*/

						// TODO: colorItems()
						//weekView.notifyDataSetChanged()
					}
				}

				/*scope.launch {
					merge(
						preferences.themeColor.getValueFlow(),
						preferences.darkTheme.getValueFlow()
					) {
						weekView
					}
				}*/

				scope.launch {
					weekView.hourHeight = globalPreferences.data
						.map { prefs -> prefs[DATASTORE_KEY_WEEKVIEW_SCALE] ?: -1 }
						.first()
				}
			}
		}
	}

	/*@SuppressLint("ClickableViewAccessibility")
	fun updateViews(container: WeekViewSwipeRefreshLayout) {
		val touchListener = View.OnTouchListener { view, motionEvent ->
			if (isAnonymous) true else view.onTouchEvent(motionEvent)
		}

		container.apply {
			isRefreshing = this@MainAppState.isRefreshing
		}

		if (!shouldUpdateWeekView) return

		container.apply {
			setOnRefreshListener { onRefresh() }
			setOnTouchListener(touchListener)
		}

		weekView.value?.apply {
			val outlineAlpha = 0.4f

			// Workaround to enable drawer gestures only when swiping from the left edge (won't work with RTL layout)
			onMotionEvent = { event -> onMotionEvent(event) }

			setOnTouchListener(touchListener)

			setPeriodChangeListener(object :
				WeekViewLoader.PeriodChangeListener<TimegridItem> {
				override fun onPeriodChange(
					startDate: LocalDate,
					endDate: LocalDate
				): List<WeekViewDisplayable<TimegridItem>> {
					return this@MainAppState.onPeriodChange(startDate, endDate)
				}
			})

			setOnEventClickListener(object :
				EventClickListener<TimegridItem> {
				override fun onEventClick(
					data: TimegridItem,
					eventRect: RectF
				) {
					val items =
						(weeklyTimetableItems[convertLocalDateToWeekIndex(data.startDateTime.toLocalDate())]?.items
							?: emptyList())
							.filter {
								it.startDateTime.millis <= data.startDateTime.millis &&
										it.endDateTime.millis >= data.endDateTime.millis
							}

					timetableItemDetailsDialog.value =
						items.map { it.periodData } to max(0, items.indexOf(data))
				}
			})

			setOnCornerClickListener(object : TopLeftCornerClickListener {
				override fun onCornerClick() {
					showDatePicker.value = true
				}

				override fun onCornerLongClick() {
					goToToday()
				}
			})

			scrollListener = object : ScrollListener {
				override fun onFirstVisibleDayChanged(
					newFirstVisibleDay: LocalDate,
					oldFirstVisibleDay: LocalDate?
				) {
					onScroll(newFirstVisibleDay)
				}
			}

			scaleListener = object : ScaleListener {
				override fun onScaleFinished() {
					scope.launch {
						globalPreferences.edit { prefs ->
							weekView.value?.let {
								prefs[DATASTORE_KEY_WEEKVIEW_SCALE] = it.hourHeight
							}
						}
					}
				}
			}

			config.apply {
				with(currentDensity) {
					daySeparatorColor = colorScheme.outline.copy(alpha = outlineAlpha).toArgb()
					defaultEventColor = colorScheme.primary.toArgb()
					eventMarginVertical = 4.dp.roundToPx()
					eventPadding = 4.dp.roundToPx()
					headerRowBackgroundColor = Color.Transparent.toArgb()
					headerRowPadding = 8.dp.roundToPx()
					headerRowSecondaryTextColor = colorScheme.onSurfaceVariant.toArgb()
					headerRowSecondaryTextSize = 12.sp.toPx()
					headerRowTextColor = colorScheme.onSurface.toArgb()
					headerRowTextSize = 18.sp.toPx()
					headerRowTextSpacing = 10.dp.roundToPx()
					holidayTextColor = colorScheme.onSurface.toArgb()
					holidayTextSize = 16.sp.toPx()
					hourHeight = 72.dp.roundToPx()
					hourSeparatorColor = colorScheme.outline.copy(alpha = outlineAlpha).toArgb()
					nowLineStrokeWidth = 2.dp.toPx()
					scrollDuration = 100
					showHourSeparator = true
					showNowLine = true
					timeColumnBackground = Color.Transparent.toArgb()
					timeColumnCaptionColor = colorScheme.onSurface.toArgb()
					timeColumnCaptionSize = 16.sp.toPx()
					timeColumnPadding = 4.dp.roundToPx()
					timeColumnTextColor = colorScheme.onSurfaceVariant.toArgb()
					timeColumnTextSize = 12.sp.toPx()
					todayHeaderTextColor = colorScheme.primary.toArgb()
					topLeftCornerDrawable =
						AppCompatResources.getDrawable(context, R.drawable.all_calendar_adjusted)
					topLeftCornerPadding = 4.dp.roundToPx()
					topLeftCornerTint = colorScheme.onSurface.toArgb()
				}
			}

			weeklyTimetableItems.clear()
			notifyDataSetChanged()
			restoreWeekViewScrollPosition()
		}

		shouldUpdateWeekView = false
	}

	private fun onMotionEvent(event: MotionEvent) {
		when (event.action) {
			MotionEvent.ACTION_DOWN -> {
				drawerGestures =
					event.x < with(currentDensity) { 48.dp.toPx() }
			}
			MotionEvent.ACTION_UP -> {
				drawerGestures = true
			}
		}
	}

	fun onPeriodChange(
		startDate: LocalDate,
		endDate: LocalDate
	): List<WeekViewDisplayable<TimegridItem>> {
		val weekIndex =
			convertLocalDateToWeekIndex(startDate)
		return weeklyTimetableItems[weekIndex]?.items?.map { item -> item.toWeekViewEvent() }
			?: run {
				weeklyTimetableItems[weekIndex] =
					WeeklyTimetableItems()
				displayedElement.value?.let { displayedElement ->
					scope.launch {
						loading.value++
						loadWeeklyTimetableItems(
							timetableLoader,
							startDate,
							endDate,
							displayedElement,
							onItemsChanged = { items ->
								weeklyTimetableItems[weekIndex] =
									items
								weekView.value?.notifyDataSetChanged()
							}
						)
						loading.value--
					}
				}
				emptyList()
			}
	}

	fun onScroll(newFirstVisibleDay: LocalDate) {
		currentWeekIndex.value = convertLocalDateToWeekIndex(newFirstVisibleDay)
		lastRefreshTimestamp.value = weeklyTimetableItems[currentWeekIndex.value]?.lastUpdated ?: 0
	}

	private fun restoreWeekViewScrollPosition() {
		if (currentWeekIndex.value % 100 > 0)
			weekView.value?.goToDate(convertWeekIndexToDateTime(currentWeekIndex.value))
	}*/*/

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberMainDrawerState(
	user: User,
	contextActivity: BaseComposeActivity,
	coroutineScope: CoroutineScope = rememberCoroutineScope(),
	drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
	bookmarkDeleteDialog: MutableState<TimetableBookmark?> = rememberSaveable { mutableStateOf(null) },
) = remember(user.id) {
	MainDrawerState(
		user = user,
		contextActivity = contextActivity,
		scope = coroutineScope,
		drawerState = drawerState,
		bookmarkDeleteDialog = bookmarkDeleteDialog,
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberNewMainAppState(
	user: User,
	contextActivity: BaseComposeActivity,
	preferences: DataStorePreferences,
	coroutineScope: CoroutineScope = rememberCoroutineScope(),
	mainDrawerState: MainDrawerState = rememberMainDrawerState(user, contextActivity),
	weekViewPreferences: NewMainAppState.WeekViewPreferences = rememberWeekViewPreferences(
		contextActivity.dataStorePreferences, user
	),
	colorScheme: ColorScheme = MaterialTheme.colorScheme,
) = remember(user) {
	NewMainAppState(
		user = user,
		contextActivity = contextActivity,
		preferences = preferences,
		scope = coroutineScope,
		mainDrawerState = mainDrawerState,
		weekViewPreferences = weekViewPreferences,
		colorScheme = colorScheme,
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberMainAppState(
	user: User,
	contextActivity: BaseComposeActivity,
	//customThemeColor: Color?,
	preferences: DataStorePreferences,
	globalPreferences: DataStore<Preferences>,
	/*weekViewSwipeRefresh: MutableState<WeekViewSwipeRefreshLayout?> = remember { mutableStateOf(null) },
	weekView: MutableState<WeekView<TimegridItem>?> = remember { mutableStateOf(null) },*/
	coroutineScope: CoroutineScope = rememberCoroutineScope(),
	colorScheme: ColorScheme = MaterialTheme.colorScheme,
	//currentDensity: Density = LocalDensity.current,
	personalTimetable: Pair<PeriodElement?, String?>? = getPersonalTimetableElement(
		user, contextActivity
	),
	defaultDisplayedName: String = user.getDisplayedName(),// stringResource(id = R.string.app_name),
	drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
	drawerGestures: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
	/*loading: MutableState<Int> = rememberSaveable { mutableStateOf(0) },
	currentWeekIndex: MutableState<Int> = rememberSaveable { mutableStateOf(0) },*/
	lastRefreshTimestamp: MutableState<Long> = rememberSaveable { mutableStateOf(0L) },
	// TODO: Find another way of saving timetableItemDetailsDialog that doesn't require saving an entire Pair of List of PeriodData's.
	//  Currently the dialog will close after state change (i.e. rotation).
	/*timetableItemDetailsDialog: MutableState<Pair<List<PeriodData>, Int>?> = remember {
		mutableStateOf(
			null
		)
	},
	showDatePicker: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },*/
	profileManagementDialog: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
	bookmarkDeleteDialog: MutableState<TimetableBookmark?> = rememberSaveable { mutableStateOf(null) },
	weekViewPreferences: NewMainAppState.WeekViewPreferences = rememberWeekViewPreferences(
		contextActivity.dataStorePreferences, user
	),
	weekViewEvents: SnapshotStateMap<LocalDate, List<Event>> = mutableStateMapOf<LocalDate, List<Event>>(),
	weekViewPage: MutableState<Int> = rememberSaveable { mutableStateOf(0) },
) = remember(user) {
	MainAppState(
		user = user,
		contextActivity = contextActivity,
//		weekViewSwipeRefresh = weekViewSwipeRefresh,
//		weekView = weekView,
//		context = context,
		preferences = preferences,
		globalPreferences = globalPreferences,
		scope = coroutineScope,
		colorScheme = colorScheme,
//		currentDensity = currentDensity,
		personalTimetable = personalTimetable,
		defaultDisplayedName = defaultDisplayedName,
		drawerState = drawerState,
		drawerGestureState = drawerGestures,
//		loading = loading,
//		currentWeekIndex = currentWeekIndex,
		lastRefreshTimestamp = lastRefreshTimestamp,
//		timetableItemDetailsDialog = timetableItemDetailsDialog,
//		showDatePicker = showDatePicker,
		profileManagementDialog = profileManagementDialog,
		bookmarkDeleteDialog = bookmarkDeleteDialog,
		//weekViewPreferences = weekViewPreferences,
		weekViewEvents = weekViewEvents,
		weekViewPage = weekViewPage
	)
}

fun buildHourList(
	user: User, range: Pair<Int, Int>?, rangeIndexReset: Boolean
): List<WeekViewHour> {
	val hourList = mutableListOf<WeekViewHour>()

	user.timeGrid.days.maxByOrNull { it.units.size }?.units?.forEachIndexed { index, hour ->
		// Check if outside configured range
		if (range?.let { index < it.first - 1 || index >= it.second } == true) return@forEachIndexed

		val startTime = hour.startTime.toLocalTime()
		val endTime = hour.endTime.toLocalTime()

		// If label is empty, fill it according to preferences
		val label = hour.label.ifEmpty {
			if (rangeIndexReset) (index + 1).toString()
			else ((range?.first ?: 1) + index).toString()
		}

		hourList.add(
			WeekViewHour(
				LocalTime(startTime.hour, startTime.minute),
				LocalTime(endTime.hour, endTime.minute),
				label
			)
		)
	}

	return hourList
}

@Composable
fun rememberWeekViewPreferences(
	preferences: DataStorePreferences,
	user: User,
	hourList: State<List<WeekViewHour>> = combine(
		preferences.timetableRange.getValueFlow(),
		preferences.timetableRangeIndexReset.getValueFlow()
	) { range, rangeIndexReset ->
		buildHourList(
			user, range.convertRangeToPair(), rangeIndexReset
		)
	}.collectAsState(initial = emptyList()),
	dividerWidth: Float = Stroke.HairlineWidth,
	dividerColor: Color = MaterialTheme.colorScheme.outline,
	indicatorColor: State<Int> = preferences.marker.getState(),
	backgroundPast: State<Int> = preferences.backgroundPast.getState(),
	backgroundFuture: State<Int> = preferences.backgroundFuture.getState(),
	backgroundRegular: State<Int> = preferences.backgroundRegular.getState(),
	backgroundRegularPast: State<Int> = preferences.backgroundRegularPast.getState(),
	backgroundExam: State<Int> = preferences.backgroundExam.getState(),
	backgroundExamPast: State<Int> = preferences.backgroundExamPast.getState(),
	backgroundCancelled: State<Int> = preferences.backgroundCancelled.getState(),
	backgroundCancelledPast: State<Int> = preferences.backgroundCancelledPast.getState(),
	backgroundIrregular: State<Int> = preferences.backgroundIrregular.getState(),
	backgroundIrregularPast: State<Int> = preferences.backgroundIrregularPast.getState(),
	weekLength: State<Int> = preferences.weekCustomRange.getValueFlow()
		.transform<Set<String>, Int> {
			it.size.zeroToNull ?: user.timeGrid.days.size
		}.collectAsState(initial = 5),
	weekStartOffset: State<Int> = preferences.weekCustomRange.getValueFlow()
		.transform<Set<String>, Int> {
			it.map { day -> Weekday.valueOf(day) }.minOrNull()?.ordinal
				?: DateTimeFormat.forPattern("E")
					.withLocale(Locale.ENGLISH) // TODO: Correct locale?
					.parseDateTime(user.timeGrid.days[0].day).dayOfWeek
		}.collectAsState(initial = DateTimeConstants.MONDAY),
) = remember {
	NewMainAppState.WeekViewPreferences(
		hourList = hourList,
		dividerWidth = dividerWidth,
		colorScheme = WeekViewColorScheme(
			dividerColor = dividerColor,
			pastBackgroundColor = Color(backgroundPast.value),
			futureBackgroundColor = Color(backgroundFuture.value),
			indicatorColor = Color(indicatorColor.value)
		),
		backgroundRegular = backgroundRegular,
		backgroundRegularPast = backgroundRegularPast,
		backgroundExam = backgroundExam,
		backgroundExamPast = backgroundExamPast,
		backgroundCancelled = backgroundCancelled,
		backgroundCancelledPast = backgroundCancelledPast,
		backgroundIrregular = backgroundIrregular,
		backgroundIrregularPast = backgroundIrregularPast,
		weekLength = weekLength,
		weekStartOffset = weekStartOffset,
	)
}

private fun getPersonalTimetableElement(
	user: User, context: Context
): Pair<PeriodElement?, String?>? {
	return user.userData.elemType?.let { type ->
		PeriodElement(
			type = type,
			id = user.userData.elemId,
			orgId = user.userData.elemId,
		) to user.getDisplayedName(context)
	}
}

private val Int.zeroToNull: Int?
	get() = if (this != 0) this else null
