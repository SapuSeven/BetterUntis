package com.sapuseven.untis.feature.timetable

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sapuseven.untis.core.domain.navigation.FeatureRoute
import com.sapuseven.untis.core.domain.navigation.FeatureRouteItem
import com.sapuseven.untis.core.model.timetable.ElementType
import com.sapuseven.untis.core.model.timetable.Period
import com.sapuseven.untis.core.ui.common.UserSelectorAction
import com.sapuseven.untis.core.ui.functional.None
import com.sapuseven.untis.core.ui.functional.bottomInsets
import com.sapuseven.untis.core.ui.functional.insetsPaddingValues
import com.sapuseven.untis.feature.timetable.drawer.NavItemNavigation
import com.sapuseven.untis.feature.timetable.drawer.TimetableDrawer
import com.sapuseven.untis.feature.timetable.weekview.WeekView
import com.sapuseven.untis.feature.timetable.weekview.WeekViewStyle
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
internal fun TimetableScreen(
	viewModel: TimetableViewModel = hiltViewModel(),
	featureRoutes: @Composable FeatureRoute.() -> List<FeatureRouteItem>,
	sharedTransitionScope: SharedTransitionScope,
	animatedVisibilityScope: AnimatedVisibilityScope,
	onNavigate: (Any) -> Unit,
	onUserListClick: () -> Unit,
	onElementClick: (id: Long?, type: ElementType?) -> Unit,
	onPeriodDetails: (id: Long, type: ElementType, timetablePage: Int, periodIds: List<Long>, initialPeriod: Int) -> Unit,
) {
	val scope = rememberCoroutineScope()
	val uiState by viewModel.uiState.collectAsStateWithLifecycle()
	val drawerState = rememberDrawerState(DrawerValue.Closed)

	TimetableDrawer(
		drawerState = drawerState,
		displayedElement = uiState.currentElement,
		personalTimetableSelected = uiState.currentElementIsPersonal,
		navRoutes = FeatureRoute.featureRoutes()
			.map {
				NavItemNavigation(
					painterResource(it.icon),
					stringResource(it.label),
					it.route
				)
			},
		onNavigate = onNavigate,
		onElementPicked = {
			onElementClick(it?.id, it?.type)
		}
	) {
		Scaffold(
			topBar = {
				CenterAlignedTopAppBar(
					colors = TopAppBarDefaults.topAppBarColors()
						.copy(containerColor = MaterialTheme.colorScheme.surfaceContainer),
					title = {
						Text(uiState.title)
					},
					navigationIcon = {
						IconButton(onClick = {
							scope.launch { drawerState.open() }
						}) {
							Icon(
								imageVector = Icons.Outlined.Menu,
								contentDescription = "TODO"// stringResource(id = R.string.main_drawer_open)
							)
						}
					},
					actions = {
						/*if (uiState.isDebug)
							DebugDisclaimerAction(
								viewModel.debugInfoRepository.getColorSchemeDebugInfo(MaterialTheme.colorScheme.toString())
							)*/

						UserSelectorAction(
							users = uiState.userList,
							currentSelection = uiState.user,
							showProfileActions = true,
							onSelectionChange = {
								viewModel.switchUser(it)
							},
							onActionEdit = {
								viewModel.editUsers()
								onUserListClick()
							}
						)
					}
				)
			},
			contentWindowInsets = WindowInsets.None
		) { innerPadding ->
			/*AnimatedVisibility(ready,
				enter = fadeIn(tween()),
				exit = fadeOut(tween())
			) {*/
			Box(
				modifier = Modifier
					.fillMaxSize()
					.padding(innerPadding)
			) {
				val insets = insetsPaddingValues()
				val navBarHeight = remember { insets.calculateBottomPadding() + 48.dp }

				/*FeedbackDialog(
					visible = viewModel.feedbackDialog,
					onDismiss = { viewModel.feedbackDialog = false }
				)*/

				WeekViewStyle(uiState.eventStyle) {
					WeekView(
						events = uiState.pagerState.events,
						holidays = uiState.holidays,
						loading = if (uiState.pagerState.isLoading) true else null,
						weekLogicService = viewModel.weekLogicService,
						onPageChange = { pageOffset ->
							viewModel.onPageChanged(pageOffset)
						},
						onReload = { pageOffset ->
							viewModel.onPageReload(pageOffset)
						},
						onItemClick = { periods, initialPeriod ->
							uiState.currentElement?.let { element ->
								onPeriodDetails(
									element.id,
									element.type,
									uiState.currentPage,
									periods.map(Period::id),
									initialPeriod
								)
							}
						},
						onZoom = { zoomLevel ->
							//TODO viewModel.onZoom(zoomLevel)
						},
						currentTime = uiState.currentTime.toLocalDateTime(TimeZone.currentSystemDefault()),
						startTime = uiState.hourList.firstOrNull()?.startTime ?: LocalTime.fromSecondOfDay(0),
						endTime = uiState.hourList.lastOrNull()?.endTime ?: LocalTime.fromSecondOfDay(0),
						endTimeOffset = navBarHeight,
						//initialScale = weekViewScale,
						//enableZoomGesture = weekViewZoomEnabled,
						hourHeight = /*state.weekViewPreferences.hourHeight ?:*/ 72.dp,
						hourList = uiState.hourList,
						dividerWidth = 4f,
						colorScheme = uiState.colorScheme,
						sharedTransitionScope = sharedTransitionScope,
						animatedVisibilityScope = animatedVisibilityScope,
						modifier = Modifier
							.fillMaxSize()
						//.disabled(disabled = needsPersonalTimetable)
					) { startPadding ->
						// Feedback button
						IconButton(
							modifier = Modifier
								.align(Alignment.BottomEnd)
								.padding(end = 8.dp)
								.bottomInsets(),
							onClick = {
								//viewModel.showFeedback()
							}
						) {
							/*Icon(
								painter = painterResource(R.drawable.all_feedback),
								contentDescription = "Give feedback"
							)*/
						}

						// Custom personal timetable hint
						/*if (needsPersonalTimetable) {
							Column(
								verticalArrangement = Arrangement.Center,
								horizontalAlignment = Alignment.CenterHorizontally,
								modifier = Modifier
									.fillMaxSize()
							) {
								Text(
									text = stringResource(id = R.string.main_anonymous_login_info_text),
									textAlign = TextAlign.Center,
									modifier = Modifier
										.padding(horizontal = 32.dp)
								)

								Button(
									onClick = viewModel.onAnonymousSettingsClick,
									modifier = Modifier
										.padding(top = 16.dp)
								) {
									Text(text = stringResource(id = R.string.main_go_to_settings))
								}
							}
						} else {*/
						// Last refresh text
						Text(
							text = stringResource(
								id = R.string.feature_timetable_last_refreshed,
								formatTimeDiff(uiState.pagerState.lastRefresh[uiState.currentPage]?.let {
									uiState.currentTime.epochSeconds - it.epochSeconds
								})
							),
							modifier = Modifier
								.align(Alignment.BottomStart)
								.padding(start = startPadding + 8.dp, bottom = 8.dp)
								.bottomInsets()
						)
						//}
					}
				}
			}
			//}
		}
	}

	// TODO: Implement a nicer animation (see https://m3.material.io/components/dialogs/guidelines#007536b9-76b1-474a-a152-2f340caaff6f)
	/*
	AnimatedVisibility(
		visible = viewModel.profileManagementDialog,
		enter = fullscreenDialogAnimationEnter(),
		exit = fullscreenDialogAnimationExit()
	) {
		ProfileManagementDialog(
			userRepository = viewModel.userRepository,
			onEdit = {
				viewModel.editUser(it)
			},
			onDismiss = {
				viewModel.profileManagementDialog = false
			}
		)
	}*/
}

@Composable
private fun formatTimeDiff(s: Long?): String {
	val MINUTE = 60
	val HOUR = 60 * MINUTE
	val DAY = 24 * HOUR

	if (s == null) return stringResource(R.string.feature_timetable_last_refreshed_never)

	return when {
		s < MINUTE -> stringResource(R.string.feature_timetable_time_diff_just_now)
		s < HOUR -> pluralStringResource(
			R.plurals.feature_timetable_time_diff_minutes, (s / MINUTE).toInt(), s / MINUTE
		)

		s < DAY -> pluralStringResource(
			R.plurals.feature_timetable_time_diff_hours, (s / HOUR).toInt(), s / HOUR
		)

		else -> pluralStringResource(
			R.plurals.feature_timetable_time_diff_days, (s / DAY).toInt(), s / DAY
		)
	}
}
