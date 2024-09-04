package com.sapuseven.untis.ui.activities.timetable

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sapuseven.untis.BuildConfig
import com.sapuseven.untis.R
import com.sapuseven.untis.activities.MainActivity
import com.sapuseven.untis.activities.main.DrawerItems
import com.sapuseven.untis.activities.main.DrawerText
import com.sapuseven.untis.api.model.untis.masterdata.Klasse
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.models.untis.timetable.PeriodElement
import com.sapuseven.untis.ui.animations.fullscreenDialogAnimationEnter
import com.sapuseven.untis.ui.animations.fullscreenDialogAnimationExit
import com.sapuseven.untis.ui.common.AppScaffold
import com.sapuseven.untis.ui.common.DebugDesclaimerAction
import com.sapuseven.untis.ui.common.ProfileSelectorAction
import com.sapuseven.untis.ui.common.ReportsInfoBottomSheet
import com.sapuseven.untis.ui.dialogs.ProfileManagementDialog
import com.sapuseven.untis.ui.functional.insetsPaddingValues
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Timetable(
	viewModel: TimetableViewModel = hiltViewModel()
) {
	val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
	val scope = rememberCoroutineScope()
	val users = viewModel.allUsersLiveData.observeAsState(listOf())
	val timetableElements = viewModel.elementPickerDelegate.getAllPeriodElements()

	TimetableDrawer(
		drawerState = drawerState,
		//timetableDatabaseInterface = viewModel.getTimetableDatabaseInterface(),
		elementPickerDelegate = viewModel.elementPickerDelegate,
		onShowTimetable = {
			//it.let { element ->
				//state.setDisplayedElement(element?.first, element?.second)
			//}
		}
	) {
		AppScaffold(
			topBar = {
				CenterAlignedTopAppBar(
					title = { Text(viewModel.displayedName) },
					navigationIcon = {
						IconButton(onClick = {
							scope.launch {
								drawerState.open()
							}
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
							users = users.value,
							currentSelection = viewModel.currentUser.value,
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
					Text("Current user: ${viewModel.currentUser.value?.id}")
					Button(onClick = { viewModel.toggleTheme() }) {
						Text(text = "Toggle theme")
					}

					var rooms = viewModel.elementPickerDelegate.allRooms.observeAsState()
					var classes = viewModel.elementPickerDelegate.allClasses.observeAsState()

					Text(text = "${rooms.value?.size ?: "?"} rooms")
					Text(text = "${classes.value?.size ?: "?"} classes")
					Text(text = "${viewModel.elementPickerDelegate.allSubjects.value?.size ?: "?"} subjects")
					Text(text = "${viewModel.elementPickerDelegate.allTeachers.value?.size ?: "?"} teachers")
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
	}

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
