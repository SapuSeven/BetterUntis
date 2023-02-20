package com.sapuseven.untis.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.ColorUtils
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.sapuseven.untis.R
import com.sapuseven.untis.activities.SettingsActivity.Companion.EXTRA_STRING_PREFERENCE_HIGHLIGHT
import com.sapuseven.untis.activities.SettingsActivity.Companion.EXTRA_STRING_PREFERENCE_ROUTE
import com.sapuseven.untis.activities.main.DrawerItems
import com.sapuseven.untis.activities.main.DrawerText
import com.sapuseven.untis.data.databases.entities.User
import com.sapuseven.untis.data.timetable.PeriodData
import com.sapuseven.untis.data.timetable.TimegridItem
import com.sapuseven.untis.helpers.DateTimeUtils
import com.sapuseven.untis.helpers.config.globalDataStore
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.helpers.timetable.TimetableLoader
import com.sapuseven.untis.models.TimetableBookmark
import com.sapuseven.untis.models.untis.UntisDate
import com.sapuseven.untis.models.untis.timetable.PeriodElement
import com.sapuseven.untis.preferences.DataStorePreferences
import com.sapuseven.untis.preferences.dataStorePreferences
import com.sapuseven.untis.ui.animations.fullscreenDialogAnimationEnter
import com.sapuseven.untis.ui.animations.fullscreenDialogAnimationExit
import com.sapuseven.untis.ui.common.*
import com.sapuseven.untis.ui.dialogs.DatePickerDialog
import com.sapuseven.untis.ui.dialogs.ElementPickerDialogFullscreen
import com.sapuseven.untis.ui.dialogs.ProfileManagementDialog
import com.sapuseven.untis.ui.dialogs.TimetableItemDetailsDialog
import com.sapuseven.untis.ui.functional.BackPressConfirm
import com.sapuseven.untis.ui.functional.bottomInsets
import com.sapuseven.untis.ui.functional.insetsPaddingValues
import com.sapuseven.untis.ui.preferences.convertRangeToPair
import com.sapuseven.untis.ui.preferences.decodeStoredTimetableValue
import com.sapuseven.untis.ui.weekview.WeekViewCompose
import com.sapuseven.untis.views.WeekViewSwipeRefreshLayout
import com.sapuseven.untis.views.weekview.HolidayChip
import com.sapuseven.untis.views.weekview.WeekView
import com.sapuseven.untis.views.weekview.WeekViewDisplayable
import com.sapuseven.untis.views.weekview.listeners.EventClickListener
import com.sapuseven.untis.views.weekview.listeners.ScaleListener
import com.sapuseven.untis.views.weekview.listeners.ScrollListener
import com.sapuseven.untis.views.weekview.listeners.TopLeftCornerClickListener
import com.sapuseven.untis.views.weekview.loaders.WeekViewLoader
import io.sentry.Breadcrumb
import io.sentry.Sentry
import io.sentry.SentryLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import org.joda.time.*
import org.joda.time.format.DateTimeFormat
import java.lang.ref.WeakReference
import java.util.*
import kotlin.math.max
import kotlin.math.roundToInt

class MainActivity : BaseComposeActivity() {
	companion object {
		const val MESSENGER_PACKAGE_NAME = "com.untis.chat"

		const val EXTRA_STRING_PERIOD_ELEMENT = "com.sapuseven.untis.activities.main.element"
	}

	private val weekViewRefreshHandler = Handler(Looper.getMainLooper())

	// TODO
	/*private val weekViewUpdate = object : Runnable {
		override fun run() {
			weekView.invalidate()
			weekViewRefreshHandler.postDelayed(this, 60 * 1000)
		}
	}*/

	private val loginLauncher =
		registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
			if (it.resultCode == Activity.RESULT_OK)
				recreate() // TODO: Look at it.data for potential actions (e.g. show a specific timetable)
			else
				finish()
		}

	@OptIn(ExperimentalMaterial3Api::class)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			AppTheme(navBarInset = false) {
				withUser(
					invalidContent = { login() }
				) { user ->
					val state =
						rememberMainAppState(
							user = user,
							contextActivity = this,
							customThemeColor = customThemeColor,
							timetableDatabaseInterface = timetableDatabaseInterface,
							preferences = dataStorePreferences,
							globalPreferences = globalDataStore,
							colorScheme = MaterialTheme.colorScheme//!! // Can't be null, AppTheme content isn't rendered if colorScheme is null
						)
					state.loadPrefs(dataStorePreferences)

					MainApp(state)
				}
			}
		}
	}

	private fun login() {
		loginLauncher.launch(Intent(this, LoginActivity::class.java).apply {
			putUserIdExtra(this)
			putBackgroundColorExtra(this)
		})
	}

}

/*@Composable
private fun WeekViewCompose(state: MainAppState) {
	var weekViewGlobal by remember { mutableStateOf(state.weekView.value) }
	state.loadWeekViewPreferences(weekViewGlobal, state.preferences)

	AndroidView(
		factory = { context ->
			if (weekViewGlobal == null) { // Create weekView if it doesn't already exist
				WeekView<TimegridItem>(context).also {
					weekViewGlobal = it
					state.weekView.value = it
				}
			}

			state.weekViewSwipeRefresh.value ?: WeekViewSwipeRefreshLayout(context).apply {
				state.weekViewSwipeRefresh.value = this
				addView(weekViewGlobal)
			}
		},
		update = {
			state.weekView.value = weekViewGlobal
			state.userDatabase.userDao().getByIdWithData(state.user.id)?.holidays?.let {
				state.weekView.value?.addHolidays(it.map { holiday ->
					HolidayChip(
						text = holiday.longName,
						startDate = holiday.startDate,
						endDate = holiday.endDate
					)
				})
			}
			state.updateViews(it)
		},
		modifier = Modifier
			.fillMaxSize()
			.disabled(state.isAnonymous)
	)
}*/

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Drawer(
	state: MainAppState,
	onShowTimetable: (Pair<PeriodElement?, String?>?) -> Unit,
	content: @Composable () -> Unit
) {
	val scope = rememberCoroutineScope()
	val drawerScrollState = rememberScrollState()
	var bookmarkDeleteDialog by remember { mutableStateOf<TimetableBookmark?>(null) }

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
		snapshotFlow { state.drawerState.isOpen }
			.distinctUntilChanged()
			.drop(1)
			.collect {
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
					selected = state.isPersonalTimetable,
					onClick = {
						state.closeDrawer()
						onShowTimetable(state.personalTimetable)
					},
					modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
				)

				var isBookmarkSelected = false
				state.user.bookmarks.forEach { bookmark ->
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
								),
								contentDescription = null
							)
						},
						badge = {
							IconButton(
								onClick = { bookmarkDeleteDialog = bookmark }
							) {
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
							painterResource(id = R.drawable.all_add),
							contentDescription = null
						)
					},
					label = { Text(stringResource(id = R.string.maindrawer_bookmarks_add)) },
					selected = false,
					onClick = {
						state.closeDrawer()
						bookmarksElementPicker = TimetableDatabaseInterface.Type.CLASS
						//selectedItem.value = item
					},
					modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
				)

				DrawerText(stringResource(id = R.string.nav_all_timetables))

				DrawerItems(
					isMessengerAvailable = state.isMessengerAvailable,
					disableTypeSelection = state.isPersonalTimetable || isBookmarkSelected,
					displayedElement = state.displayedElement.value,
					onTimetableClick = { item ->
						state.closeDrawer()
						showElementPicker = item.elementType
					},
					onShortcutClick = { item ->
						Log.i("Sentry", "Drawer onClick: ${item}")
						Breadcrumb().apply {
							category = "ui.drawer.click"
							level = SentryLevel.INFO
							setData("id", item.id)
							setData("label", item.label)
							Sentry.addBreadcrumb(this)
						}

						state.closeDrawer()
						if (item.target == null) {
							try {
								state.context.startActivity(
									state.context.packageManager.getLaunchIntentForPackage(
										MainActivity.MESSENGER_PACKAGE_NAME
									)
								)
							} catch (e: Exception) {
								try {
									state.context.startActivity(
										Intent(
											Intent.ACTION_VIEW,
											Uri.parse("market://details?id=${MainActivity.MESSENGER_PACKAGE_NAME}")
										)
									)
								} catch (e: Exception) {
									state.context.startActivity(
										Intent(
											Intent.ACTION_VIEW,
											Uri.parse("https://play.google.com/store/apps/details?id=${MainActivity.MESSENGER_PACKAGE_NAME}")
										)
									)
								}
							}
						} else {
							shortcutLauncher.launch(
								Intent(
									state.context,
									item.target
								).apply {
									state.contextActivity.putUserIdExtra(this, state.user.id)
									state.contextActivity.putBackgroundColorExtra(this)
								}
							)
						}
					}
				)
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
		val context = LocalContext.current

		ElementPickerDialogFullscreen(
			title = { Text(stringResource(id = R.string.maindrawer_bookmarks_add)) },
			timetableDatabaseInterface = state.timetableDatabaseInterface,
			hideTypeSelectionPersonal = true,
			onDismiss = { bookmarksElementPicker = null },
			onSelect = { item ->
				item?.let {
					val newBookmark = TimetableBookmark(
						elementId = it.id,
						elementType = TimetableDatabaseInterface.Type.valueOf(it.type).name,
						displayName = state.timetableDatabaseInterface.getLongName(it)
					)

					if (state.user.bookmarks.contains(newBookmark))
						Toast
							.makeText(
								context,
								"Bookmark already exists",
								Toast.LENGTH_LONG
							) // TODO: Extract string resource
							.show()
					else {
						state.user.bookmarks = state.user.bookmarks.plus(newBookmark)
						state.userDatabase.userDao().update(state.user)
						onShowTimetable(
							item to state.timetableDatabaseInterface.getLongName(it)
						)
					}
				}

			},
			initialType = bookmarksElementPicker
		)
	}

	bookmarkDeleteDialog?.let { bookmark ->
		AlertDialog(
			text = { Text(stringResource(id = R.string.main_dialog_delete_bookmark)) },
			onDismissRequest = { bookmarkDeleteDialog = null },
			confirmButton = {
				TextButton(
					onClick = {
						state.user.bookmarks = state.user.bookmarks.minus(bookmark)
						state.userDatabase.userDao().update(state.user)
						bookmarkDeleteDialog = null
					}) {
					Text(stringResource(id = R.string.all_delete))
				}
			},
			dismissButton = {
				TextButton(
					onClick = { bookmarkDeleteDialog = null }) {
					Text(stringResource(id = R.string.all_cancel))
				}
			}
		)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseComposeActivity.MainApp(state: MainAppState) {
	val snackbarHostState = remember { SnackbarHostState() }
	if (state.preferences.doubleTapToExit.getState().value)
		BackPressConfirm(snackbarHostState)

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
							users = state.userDatabase.userDao().getAll(),
							currentSelectionId = state.user.id,
							showProfileActions = true,
							onSelectionChange = {
								state.contextActivity.setUser(it, true)
							},
							onActionEdit = {
								state.profileManagementDialog.value = true
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
				WeekViewCompose(/*state*/)

				val timeColumnWidth = with(LocalDensity.current) {
					state.weekView.value?.config?.timeColumnWidth?.toDp()
						?: 48.dp
				}
				Text(
					text = state.lastRefreshText(),
					modifier = Modifier
						.align(Alignment.BottomStart)
						.padding(start = timeColumnWidth + 8.dp, bottom = 8.dp)
						.bottomInsets()
						.disabled(state.isAnonymous)
				)

				Text(
					text = state.currentWeekIndex.value.toString(),
					modifier = Modifier
						.align(Alignment.BottomEnd)
						.padding(end = 8.dp, bottom = 8.dp)
						.bottomInsets()
				)

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

				if (state.isLoading)
					CircularProgressIndicator(
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
		visible = state.timetableItemDetailsDialog.value != null,
		enter = fullscreenDialogAnimationEnter(),
		exit = fullscreenDialogAnimationExit()
	) {
		// TODO: Incorrect insets
		TimetableItemDetailsDialog(
			timegridItems = remember {
				state.timetableItemDetailsDialog.value?.first ?: emptyList()
			},
			initialPage = remember {
				state.timetableItemDetailsDialog.value?.second ?: 0
			},
			user = state.user,
			timetableDatabaseInterface = state.timetableDatabaseInterface,
			onDismiss = {
				state.timetableItemDetailsDialog.value = null
				it?.let { state.displayElement(it) }
			}
		)
	}

	AnimatedVisibility(
		visible = state.profileManagementDialog.value,
		enter = fullscreenDialogAnimationEnter(),
		exit = fullscreenDialogAnimationExit()
	) {
		state.contextActivity.ProfileManagementDialog(
			onDismiss = {
				state.profileManagementDialog.value = false
			}
		)
	}

	if (state.showDatePicker.value)
		DatePickerDialog(
			initialSelection = state.lastSelectedDate,
			onDismiss = { state.showDatePicker.value = false }
		) {
			state.showDatePicker.value = false
			state.lastSelectedDate = it
			state.weekView.value?.goToDate(it.toDateTime(LocalTime.now()))
		}
}

class MainAppState @OptIn(ExperimentalMaterial3Api::class) constructor(
	val user: User,
	val contextActivity: BaseComposeActivity,
	val timetableDatabaseInterface: TimetableDatabaseInterface,
	val weekViewSwipeRefresh: MutableState<WeekViewSwipeRefreshLayout?>,
	val weekView: MutableState<WeekView<TimegridItem>?>,
	val context: Context,
	val scope: CoroutineScope,
	val colorScheme: ColorScheme,
	val currentDensity: Density,
	val preferences: DataStorePreferences,
	val globalPreferences: DataStore<Preferences>,
	var personalTimetable: Pair<PeriodElement?, String?>?,
	val defaultDisplayedName: String,
	val drawerState: DrawerState,
	var drawerGestureState: MutableState<Boolean>,
	val loading: MutableState<Int>,
	val currentWeekIndex: MutableState<Int>,
	val lastRefreshTimestamp: MutableState<Long>,
	val weeklyTimetableItems: SnapshotStateMap<Int, WeeklyTimetableItems?>,
	val timetableLoader: TimetableLoader,
	val timetableItemDetailsDialog: MutableState<Pair<List<PeriodData>, Int>?>,
	val showDatePicker: MutableState<Boolean>,
	val profileManagementDialog: MutableState<Boolean>
) {
	companion object {
		private const val MINUTE_MILLIS: Int = 60 * 1000
		private const val HOUR_MILLIS: Int = 60 * MINUTE_MILLIS
		private const val DAY_MILLIS: Int = 24 * HOUR_MILLIS

		private const val UNTIS_DEFAULT_COLOR = "#f49f25"

		private val DATASTORE_KEY_WEEKVIEW_SCALE = intPreferencesKey("weekView.hourHeight")
	}

	val userDatabase = contextActivity.userDatabase

	private var drawerGestures by drawerGestureState

	var lastSelectedDate: LocalDate = LocalDate.now()

	var displayedElement: MutableState<PeriodElement?> = mutableStateOf(personalTimetable?.first)
	var displayedName: MutableState<String> =
		mutableStateOf(personalTimetable?.second ?: defaultDisplayedName)

	val isPersonalTimetable: Boolean
		get() = displayedElement.value == personalTimetable?.first

	val isAnonymous: Boolean
		get() = personalTimetable != null && displayedElement.value == null

	val isLoading: Boolean
		get() = loading.value > 0

	private var isRefreshing: Boolean
		get() = weekViewSwipeRefresh.value?.isRefreshing ?: false
		set(value) {
			weekViewSwipeRefresh.value?.isRefreshing = value
		}

	private var shouldUpdateWeekView = true

	val isMessengerAvailable: Boolean
		get() {
			for (item in this.weeklyTimetableItems.values) {
				if (item != null) {
					for (it in item.items) {
						if (it.data?.periodData?.element?.messengerChannel != null) {
							return true
						}
						break
					}
				}

			}
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

		weeklyTimetableItems.clear()
		weekView.value?.notifyDataSetChanged()
	}

	@Composable
	fun lastRefreshText() = stringResource(
		id = R.string.main_last_refreshed,
		if (lastRefreshTimestamp.value > 0L)
			formatTimeDiff(Instant.now().millis - lastRefreshTimestamp.value)
		else
			stringResource(id = R.string.main_last_refreshed_never)
	)

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
				R.plurals.main_time_diff_hours,
				((diff / HOUR_MILLIS).toInt()),
				diff / HOUR_MILLIS
			)
			else -> pluralStringResource(
				R.plurals.main_time_diff_days,
				((diff / DAY_MILLIS).toInt()),
				diff / DAY_MILLIS
			)
		}
	}

	private fun Int.darken(ratio: Float) = ColorUtils.blendARGB(this, Color.Black.toArgb(), ratio)

	data class WeeklyTimetableItems(
		var items: List<TimegridItem> = emptyList(),
		var lastUpdated: Long = 0,
		var dateRange: Pair<UntisDate, UntisDate>? = null
	)

	private suspend fun prepareItems(
		items: List<TimegridItem>
	): List<TimegridItem> {
		val newItems = mergeItems(items.mapNotNull { item ->
			if (item.periodData.isCancelled() && preferences.timetableHideCancelled.getValue())
				return@mapNotNull null

			if (preferences.timetableSubstitutionsIrregular.getValue()) {
				item.periodData.apply {
					forceIrregular =
						classes.find { it.id != it.orgId } != null
								|| teachers.find { it.id != it.orgId } != null
								|| subjects.find { it.id != it.orgId } != null
								|| rooms.find { it.id != it.orgId } != null
								|| preferences.timetableBackgroundIrregular.getValue()
								&& item.periodData.element.backColor != UNTIS_DEFAULT_COLOR
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

			if (thisUnitStartIndex != -1 && thisUnitEndIndex != -1)
				itemGrid[day][thisUnitStartIndex].add(item)
			else
				leftover.add(item)
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
		val regularColor = preferences.backgroundRegular.getValue()
		val regularPastColor = preferences.backgroundRegularPast.getValue()
		val examColor = preferences.backgroundExam.getValue()
		val examPastColor = preferences.backgroundExamPast.getValue()
		val cancelledColor = preferences.backgroundCancelled.getValue()
		val cancelledPastColor = preferences.backgroundCancelledPast.getValue()
		val irregularColor = preferences.backgroundIrregular.getValue()
		val irregularPastColor = preferences.backgroundIrregularPast.getValue()

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

	private suspend fun loadWeeklyTimetableItems(
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

	private fun onRefresh() {
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
	}

	fun loadPrefs(dataStorePreferences: DataStorePreferences) {
		val personalTimetableFlow = dataStorePreferences.timetablePersonalTimetable.getValueFlow()

		scope.launch {
			personalTimetableFlow.collect { customTimetable ->
				if (user.anonymous || customTimetable != "") {
					val element = decodeStoredTimetableValue(customTimetable)
					val previousElement = personalTimetable?.first
					personalTimetable =
						element to element?.let { timetableDatabaseInterface.getLongName(it) }

					if (element != previousElement)
						displayElement(personalTimetable?.first, personalTimetable?.second)
				}
			}
		}
	}

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
					timetableRange.combine(timetableRangeIndexReset) { range, rangeIndexReset ->
						weekView.setupHours(
							range.convertRangeToPair(),
							rangeIndexReset,
							navBarHeight
						)
					}.collect()
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
						weeklyTimetableItems.map {
							it.key to it.value?.let { value -> colorItems(value.items) }
						}
						weekView.notifyDataSetChanged()
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

	private fun <T> WeekView<T>.setupHours(
		range: Pair<Int, Int>?,
		rangeIndexReset: Boolean,
		additionalSpaceBelow: Int = 0
	) {
		val lines = MutableList(0) { 0 }
		val labels = MutableList(0) { "" }

		user.timeGrid.days.maxByOrNull { it.units.size }?.units?.forEachIndexed { index, hour ->
			if (range?.let { index < it.first - 1 || index >= it.second } == true) return@forEachIndexed

			val startTime =
				hour.startTime.toLocalTime()
					.toString(DateTimeUtils.shortDisplayableTime())
			val endTime = hour.endTime.toLocalTime()
				.toString(DateTimeUtils.shortDisplayableTime())

			val startTimeParts = startTime.split(":")
			val endTimeParts = endTime.split(":")

			val startTimeInt =
				startTimeParts[0].toInt() * 60 + startTimeParts[1].toInt()
			val endTimeInt =
				endTimeParts[0].toInt() * 60 + endTimeParts[1].toInt()

			lines.add(startTimeInt)
			lines.add(endTimeInt)
			labels.add(hour.label)
		}

		if (!rangeIndexReset)
			hourIndexOffset = (range?.first ?: 1) - 1
		hourLines = lines.toIntArray()
		hourLabels = labels.toTypedArray().let { hourLabelArray ->
			if (hourLabelArray.joinToString("") == "") IntArray(
				labels.size,
				fun(idx: Int): Int { return idx + 1 }).map { it.toString() }
				.toTypedArray()
			else hourLabelArray
		}
		startTime = lines.first()
		endTime = lines.last()
		endTimeOffset = additionalSpaceBelow
	}

	@SuppressLint("ClickableViewAccessibility")
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
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberMainAppState(
	user: User,
	contextActivity: BaseComposeActivity,
	customThemeColor: Color?,
	timetableDatabaseInterface: TimetableDatabaseInterface,
	preferences: DataStorePreferences,
	globalPreferences: DataStore<Preferences>,
	weekViewSwipeRefresh: MutableState<WeekViewSwipeRefreshLayout?> = remember { mutableStateOf(null) },
	weekView: MutableState<WeekView<TimegridItem>?> = remember { mutableStateOf(null) },
	context: Context = LocalContext.current,
	coroutineScope: CoroutineScope = rememberCoroutineScope(),
	colorScheme: ColorScheme = MaterialTheme.colorScheme,
	currentDensity: Density = LocalDensity.current,
	personalTimetable: Pair<PeriodElement?, String?>? = getPersonalTimetableElement(
		user,
		context
	),
	defaultDisplayedName: String = stringResource(id = R.string.app_name),
	drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
	drawerGestures: MutableState<Boolean> = rememberSaveable { mutableStateOf(true) },
	loading: MutableState<Int> = rememberSaveable { mutableStateOf(0) },
	currentWeekIndex: MutableState<Int> = rememberSaveable { mutableStateOf(0) },
	lastRefreshTimestamp: MutableState<Long> = rememberSaveable { mutableStateOf(0L) },
	weeklyTimetableItems: SnapshotStateMap<Int, MainAppState.WeeklyTimetableItems?> = remember { mutableStateMapOf() },
	timetableLoader: TimetableLoader = TimetableLoader(
		context = WeakReference(context),
		user = user,
		timetableDatabaseInterface = timetableDatabaseInterface
	),
	// TODO: Find another way of saving timetableItemDetailsDialog that doesn't require saving an entire Pair of List of PeriodData's.
	//  Currently the dialog will close after state change (i.e. rotation).
	timetableItemDetailsDialog: MutableState<Pair<List<PeriodData>, Int>?> = remember {
		mutableStateOf(
			null
		)
	},
	showDatePicker: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
	profileManagementDialog: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
) = remember(user, customThemeColor, colorScheme) {
	MainAppState(
		user = user,
		contextActivity = contextActivity,
		timetableDatabaseInterface = timetableDatabaseInterface,
		weekViewSwipeRefresh = weekViewSwipeRefresh,
		weekView = weekView,
		context = context,
		preferences = preferences,
		globalPreferences = globalPreferences,
		scope = coroutineScope,
		colorScheme = colorScheme,
		currentDensity = currentDensity,
		personalTimetable = personalTimetable,
		defaultDisplayedName = defaultDisplayedName,
		drawerState = drawerState,
		drawerGestureState = drawerGestures,
		loading = loading,
		currentWeekIndex = currentWeekIndex,
		lastRefreshTimestamp = lastRefreshTimestamp,
		weeklyTimetableItems = weeklyTimetableItems,
		timetableLoader = timetableLoader,
		timetableItemDetailsDialog = timetableItemDetailsDialog,
		showDatePicker = showDatePicker,
		profileManagementDialog = profileManagementDialog
	)
}

private fun getPersonalTimetableElement(
	user: User,
	context: Context
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
