package com.sapuseven.untis.ui.pages.timetable

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sapuseven.untis.R
import com.sapuseven.untis.activities.MainActivity
import com.sapuseven.untis.activities.main.DrawerItems
import com.sapuseven.untis.activities.main.DrawerText
import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import com.sapuseven.untis.api.model.untis.timetable.PeriodElement
import com.sapuseven.untis.components.ElementPicker
import com.sapuseven.untis.data.settings.model.TimetableElement
import com.sapuseven.untis.ui.animations.fullscreenDialogAnimationEnter
import com.sapuseven.untis.ui.animations.fullscreenDialogAnimationExit
import com.sapuseven.untis.ui.dialogs.ElementPickerDialogFullscreen
import com.sapuseven.untis.ui.preferences.toPeriodElement
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

@Composable
fun TimetableDrawer(
	viewModel: TimetableDrawerViewModel = hiltViewModel(),
	drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
	elementPicker: ElementPicker,
	displayedElement: PeriodElement? = null,
	onElementPicked: (PeriodElement?) -> Unit,
	content: @Composable () -> Unit
) {
	val scope = rememberCoroutineScope()
	val drawerScrollState = rememberScrollState()

	var showElementPicker by remember {
		mutableStateOf<ElementType?>(
			null
		)
	}

	var bookmarksElementPicker by remember {
		mutableStateOf<ElementType?>(
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
				//onShowTimetable(it to state.timetableDatabaseInterface.getLongName(it))
			}
		}

	/*LaunchedEffect(state.drawerState) {
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
	}*/

	BackHandler(enabled = drawerState.isOpen) {
		scope.launch {
			drawerState.close()
		}
	}

	ModalNavigationDrawer(
		gesturesEnabled = viewModel.enableDrawerGestures,
		drawerState = drawerState,
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
					selected = displayedElement == null,
					onClick = {
						scope.launch {
							drawerState.close()
							onElementPicked(null)
						}
					},
					modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
				)

				val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
				var isBookmarkSelected = bookmarks.mapNotNull(TimetableElement::toPeriodElement).any { bookmark ->
					displayedElement?.equals(bookmark) == true
				}
				bookmarks.mapNotNull(TimetableElement::toPeriodElement).forEach { bookmark ->
					NavigationDrawerItem(
						icon = {
							Icon(
								painter = painterResource(
									id = when (bookmark.type) {
										ElementType.CLASS -> R.drawable.all_classes
										ElementType.TEACHER -> R.drawable.all_teachers
										ElementType.SUBJECT -> R.drawable.all_subject
										ElementType.ROOM -> R.drawable.all_rooms
										else -> R.drawable.all_prefs_personal
									}
								),
								contentDescription = null
							)
						},
						badge = {
							IconButton(
								onClick = { viewModel.showBookmarkDeleteDialog(bookmark) }
							) {
								Icon(
									painter = painterResource(id = R.drawable.all_bookmark_remove),
									contentDescription = "Remove Bookmark"
								) //TODO: Extract String resource
							}
						},
						label = { Text(text = viewModel.getBookmarkDisplayName(bookmark)) },
						selected = displayedElement?.equals(bookmark) == true,
						onClick = {
							scope.launch {
								scope.launch { drawerState.close() }
								viewModel.onBookmarkClick(bookmark);
							}
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
						bookmarksElementPicker = ElementType.CLASS
					},
					modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
				)

				DrawerText(stringResource(id = R.string.nav_all_timetables))

				DrawerItems(
					disableTypeSelection = displayedElement == null || isBookmarkSelected,
					displayedElement = displayedElement,
					onTimetableClick = { item ->
						scope.launch { drawerState.close() }
						showElementPicker = item.elementType
					},
					onNavigationClick = { item ->
						scope.launch { drawerState.close() }
						viewModel.onNavigationItemClick(item)
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
			elementPicker = elementPicker,
			onDismiss = { showElementPicker = null },
			onSelect = { item ->
				item?.let {
					onElementPicked(item)
				} ?: run {
					onElementPicked(null)
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
			title = { /*TODO*/ },
			elementPicker = elementPicker,
			hideTypeSelectionPersonal = true,
			onDismiss = { bookmarksElementPicker = null },
			onSelect = { item ->
				item?.let {
					viewModel.addBookmark(it)
				}
			},
			initialType = bookmarksElementPicker
		)
	}

	viewModel.bookmarkDeleteDialog?.let { bookmark ->
		AlertDialog(
			text = { Text(stringResource(id = R.string.main_dialog_delete_bookmark)) },
			onDismissRequest = { viewModel.dismissBookmarkDeleteDialog() },
			confirmButton = {
				TextButton(
					onClick = {
						viewModel.removeBookmark(bookmark)
					}) {
					Text(stringResource(id = R.string.all_delete))
				}
			},
			dismissButton = {
				TextButton(
					onClick = { viewModel.dismissBookmarkDeleteDialog() }) {
					Text(stringResource(id = R.string.all_cancel))
				}
			}
		)
	}
}
