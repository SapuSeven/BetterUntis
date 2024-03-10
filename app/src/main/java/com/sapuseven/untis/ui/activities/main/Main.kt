package com.sapuseven.untis.ui.activities.main

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sapuseven.untis.BuildConfig
import com.sapuseven.untis.R
import com.sapuseven.untis.activities.MainActivity
import com.sapuseven.untis.activities.MainDrawerState
import com.sapuseven.untis.activities.main.DrawerItems
import com.sapuseven.untis.activities.main.DrawerText
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.models.untis.timetable.PeriodElement
import com.sapuseven.untis.ui.animations.fullscreenDialogAnimationEnter
import com.sapuseven.untis.ui.animations.fullscreenDialogAnimationExit
import com.sapuseven.untis.ui.common.AppScaffold
import com.sapuseven.untis.ui.common.DebugDesclaimerAction
import com.sapuseven.untis.ui.common.ProfileSelectorAction
import com.sapuseven.untis.ui.common.ReportsInfoBottomSheet
import com.sapuseven.untis.ui.common.disabled
import com.sapuseven.untis.ui.dialogs.ElementPickerDialogFullscreen
import com.sapuseven.untis.ui.dialogs.FeedbackDialog
import com.sapuseven.untis.ui.dialogs.ProfileManagementDialog
import com.sapuseven.untis.ui.functional.bottomInsets
import com.sapuseven.untis.ui.functional.insetsPaddingValues
import com.sapuseven.untis.ui.weekview.WeekViewCompose
import com.sapuseven.untis.ui.weekview.startDateForPageIndex
import io.sentry.Breadcrumb
import io.sentry.Sentry
import io.sentry.SentryLevel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Main(
	viewModel: MainViewModel = viewModel()
) {
	/*Drawer(
		state = state.mainDrawerState,
		onShowTimetable = {
			it.let { element ->
				state.setDisplayedElement(element?.first, element?.second)
			}
		}
	) {*/
	val activeUser = viewModel.activeUser.collectAsState()

		AppScaffold(
			topBar = {
				CenterAlignedTopAppBar(
					title = { Text(viewModel.displayedName) },
					navigationIcon = {
						IconButton(onClick = {
							//viewModel.openDrawer()
						}) {
							Icon(
								imageVector = Icons.Outlined.Menu,
								contentDescription = stringResource(id = R.string.main_drawer_open)
							)
						}
					},
					actions = {
						if (BuildConfig.DEBUG)
							DebugDesclaimerAction()

						ProfileSelectorAction(
							users = viewModel.userList,
							currentSelection = activeUser.value,
							showProfileActions = true,
							onSelectionChange = {
								viewModel.switchUser(it)
							},
							onActionEdit = {
								viewModel.editUsers()
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

				Column {
					Text("Current user: ${activeUser.value?.id}")
					Button(onClick = { viewModel.toggleTheme() }) {
						Text(text = "Toggle theme")
					}
				}

				/*WeekViewCompose(
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

				IconButton(
					modifier = Modifier
						.align(Alignment.BottomEnd)
						.padding(end = 8.dp)
						.bottomInsets(),
					onClick = {
						state.showFeedback()
					}
				) {
					Icon(painter = painterResource(R.drawable.all_feedback), contentDescription = "Give feedback")
				}

				if (state.feedbackDialog)
					FeedbackDialog(
						onDismiss = { state.feedbackDialog = false }
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
							onClick = state.onAnonymousSettingsClick,
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
					)*/
			}

			ReportsInfoBottomSheet()
		}
	//}

	AnimatedVisibility(
		visible = viewModel.profileManagementDialog,
		enter = fullscreenDialogAnimationEnter(),
		exit = fullscreenDialogAnimationExit()
	) {
		ProfileManagementDialog(
			viewModel = viewModel,
			onDismiss = {
				viewModel.profileManagementDialog = false
			}
		)
	}
}

/*@OptIn(ExperimentalMaterial3Api::class)
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
								),
								contentDescription = null
							)
						},
						badge = {
							IconButton(
								onClick = { state.bookmarkDeleteDialog.value = bookmark }
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
					},
					modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
				)

				DrawerText(stringResource(id = R.string.nav_all_timetables))

				DrawerItems(
					isMessengerAvailable = state.isMessengerAvailable(),
					disableTypeSelection = state.isPersonalTimetableDisplayed() || isBookmarkSelected,
					displayedElement = state.displayedElement.value,
					onTimetableClick = { item ->
						state.closeDrawer()
						showElementPicker = item.elementType
					},
					onShortcutClick = { item ->
						state.onShortcutItemClick(item, shortcutLauncher)
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
		ElementPickerDialogFullscreen(
			title = { Text(stringResource(id = R.string.maindrawer_bookmarks_add)) },
			timetableDatabaseInterface = state.timetableDatabaseInterface,
			hideTypeSelectionPersonal = true,
			onDismiss = { bookmarksElementPicker = null },
			onSelect = { item ->
				item?.let {
					if (state.createBookmark(item))
						onShowTimetable(item to state.timetableDatabaseInterface.getLongName(it))
				}
			},
			initialType = bookmarksElementPicker
		)
	}

	state.bookmarkDeleteDialog.value?.let { bookmark ->
		AlertDialog(
			text = { Text(stringResource(id = R.string.main_dialog_delete_bookmark)) },
			onDismissRequest = { state.bookmarkDeleteDialog.value = null },
			confirmButton = {
				TextButton(
					onClick = {
						state.removeBookmark(bookmark)
					}) {
					Text(stringResource(id = R.string.all_delete))
				}
			},
			dismissButton = {
				TextButton(
					onClick = { state.bookmarkDeleteDialog.value = null }) {
					Text(stringResource(id = R.string.all_cancel))
				}
			}
		)
	}
}*/
