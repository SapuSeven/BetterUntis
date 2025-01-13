package com.sapuseven.untis.ui.pages.infocenter

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sapuseven.untis.R
import com.sapuseven.untis.data.repository.LocalMasterDataRepository
import com.sapuseven.untis.preferences.DataStorePreferences
import com.sapuseven.untis.ui.common.AppScaffold
import com.sapuseven.untis.ui.common.NavigationBarInset
import com.sapuseven.untis.ui.common.VerticalScrollColumn
import com.sapuseven.untis.ui.navigation.AppRoutes
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoCenter(
	bottomNavController: NavHostController = rememberNavController(),
	viewModel: InfoCenterViewModel = hiltViewModel()
) {
	AppScaffold(
		topBar = {
			CenterAlignedTopAppBar(
				title = {
					Text(stringResource(id = R.string.activity_title_info_center))
				},
				navigationIcon = {
					IconButton(onClick = { viewModel.goBack() }) {
						Icon(
							imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
							contentDescription = stringResource(id = R.string.all_back)
						)
					}
				},
				actions = {
					/*if (viewModel.selectedItem.value == ID_ABSENCES) {
						IconButton(
							onClick = {
								state.showAbsenceFilter.value = true
							}
						) {
							Icon(painter = painterResource(id = R.drawable.all_filter), contentDescription = null)
						}
					}*/
				}
			)
		}
	) { innerPadding ->
		Column(
			modifier = Modifier
				.padding(innerPadding)
				.fillMaxSize()
		) {
			// Initially load all items
			/*LaunchedEffect(Unit) {
				if (state.messages.value == null)
					coroutineScope.launch {
						state.loadMessages()/*?.also {
											preferences["preference_last_messages_count"] = it.size
											preferences["preference_last_messages_date"] =
												SimpleDateFormat(
													"dd-MM-yyyy",
													Locale.US
												).format(Calendar.getInstance().time)
										}*/
					}

				if (state.events.value == null)
					coroutineScope.launch {
						state.loadEvents()
					}

				if (state.absences.value == null)
					coroutineScope.launch {
						state.loadAbsences()
					}

				if (state.officeHours.value == null)
					coroutineScope.launch {
						state.loadOfficeHours()
					}
			}*/

			CompositionLocalProvider(LocalMasterDataRepository provides viewModel.masterDataRepository) {
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.weight(1f)
				) {
					NavHost(
						navController = bottomNavController,
						startDestination = AppRoutes.InfoCenter.Messages
					) {
						infoCenterNav(viewModel = viewModel)
					}
				}
			}

			val currentRoute by bottomNavController.currentBackStackEntryAsState()

			fun <T : Any> isCurrentRoute(route: T) = currentRoute?.destination?.route == route::class.qualifiedName

			fun <T : Any> navigate(route: T): () -> Unit = {
				if (!isCurrentRoute(route))
					bottomNavController.navigate(route) {
						bottomNavController.graph.startDestinationRoute?.let { route ->
							popUpTo(route)
						}
						launchSingleTop = true
					}
			}

			NavigationBarInset {
				NavigationBarItem(
					icon = {
						Icon(
							painterResource(id = R.drawable.infocenter_messages),
							contentDescription = null
						)
					},
					label = { Text(stringResource(id = R.string.menu_infocenter_messagesofday)) },
					selected = isCurrentRoute(AppRoutes.InfoCenter.Messages),
					onClick = navigate(AppRoutes.InfoCenter.Messages)
				)

				NavigationBarItem(
					icon = {
						Icon(
							painterResource(id = R.drawable.infocenter_events),
							contentDescription = null
						)
					},
					label = { Text(stringResource(id = R.string.menu_infocenter_events)) },
					selected = isCurrentRoute(AppRoutes.InfoCenter.Events),
					onClick = navigate(AppRoutes.InfoCenter.Events)
				)

				if (viewModel.shouldShowAbsences)
					NavigationBarItem(
						icon = {
							Icon(
								painterResource(id = R.drawable.infocenter_absences),
								contentDescription = null
							)
						},
						label = { Text(stringResource(id = R.string.menu_infocenter_absences)) },
						selected = isCurrentRoute(AppRoutes.InfoCenter.Absences),
						onClick = navigate(AppRoutes.InfoCenter.Absences)
					)

				if (viewModel.shouldShowOfficeHours)
					NavigationBarItem(
						icon = {
							Icon(
								painterResource(id = R.drawable.infocenter_contact),
								contentDescription = null
							)
						},
						label = { Text(stringResource(id = R.string.menu_infocenter_officehours)) },
						selected = isCurrentRoute(AppRoutes.InfoCenter.OfficeHours),
						onClick = navigate(AppRoutes.InfoCenter.OfficeHours)
					)
			}
		}
	}

	/*AnimatedVisibility(
		visible = viewModel.showAbsenceFilter.value,
		enter = fullscreenDialogAnimationEnter(),
		exit = fullscreenDialogAnimationExit()
	) {
		AbsenceFilterDialog(viewModel.preferences) {
			viewModel.showAbsenceFilter.value = false
		}
	}*/
}

/*when (state.selectedItem.value) {
	ID_MESSAGES -> MessageList(state.messageList, state.messagesLoading.value)
	ID_EVENTS -> EventList(state.eventList, state.eventsLoading.value)
	ID_ABSENCES -> AbsenceList(state.absenceList, state.absencesLoading.value)
	ID_OFFICEHOURS -> OfficeHourList(state.officeHourList, state.officeHoursLoading.value)
}

@Composable
private fun AbsenceList(absences: List<UntisAbsence>?, loading: Boolean) {
	ItemList(
		items = absences,
		itemRenderer = { AbsenceItem(it) },
		itemsEmptyMessage = R.string.infocenter_absences_empty,
		loading = loading
	)
}

@Composable
private fun OfficeHourList(officeHours: List<UntisOfficeHour>?, loading: Boolean) {
	ItemList(
		items = officeHours,
		itemRenderer = { OfficeHourItem(it) },
		itemsEmptyMessage = R.string.infocenter_officehours_empty,
		loading = loading
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OfficeHourItem(item: UntisOfficeHour) {
	val body = listOf(
		item.displayNameRooms,
		item.phone,
		item.email
	).filter { it?.isNotEmpty() == true }.joinToString("\n")

	ListItem(
		overlineContent = {
			Text(
				formatOfficeHourTime(
					item.startDateTime.toLocalDateTime(),
					item.endDateTime.toLocalDateTime()
				)
			)
		},
		headlineContent = { Text(item.displayNameTeacher) },
		supportingContent = if (body.isNotBlank()) {
			{ Text(body) }
		} else null
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AbsenceItem(item: UntisAbsence) {
	ListItem(
		overlineContent = {
			Text(
				formatAbsenceTime(
					item.startDateTime.toLocalDateTime(),
					item.endDateTime.toLocalDateTime()
				)
			)
		},
		headlineContent = {
			Text(
				if (item.absenceReason.isNotEmpty())
					item.absenceReason.substring(0, 1)
						.uppercase(Locale.getDefault()) + item.absenceReason.substring(1)
				else
					stringResource(R.string.infocenter_absence_unknown_reason)
			)
		},
		supportingContent = if (item.text.isNotBlank()) {
			{ Text(item.text) }
		} else null,
		leadingContent = {
			if (item.excused)
				Icon(
					painter = painterResource(R.drawable.infocenter_absences_excused),
					contentDescription = stringResource(id = R.string.infocenter_absence_excused)
				)
			else
				Icon(
					painter = painterResource(R.drawable.infocenter_absences_unexcused),
					contentDescription = stringResource(id = R.string.infocenter_absence_unexcused)
				)
		}
	)
}*/

@Composable
private fun formatAbsenceTime(
	startDateTime: LocalDateTime,
	endDateTime: LocalDateTime,
): String {
	return stringResource(
		if (startDateTime.dayOfYear == endDateTime.dayOfYear)
			R.string.infocenter_timeformat_sameday
		else
			R.string.infocenter_timeformat,
		startDateTime.toString(DateTimeFormat.mediumDate()),
		startDateTime.toString(DateTimeFormat.shortTime()),
		endDateTime.toString(DateTimeFormat.mediumDate()),
		endDateTime.toString(DateTimeFormat.shortTime())
	)
}

@Composable
private fun formatOfficeHourTime(
	startDateTime: LocalDateTime,
	endDateTime: LocalDateTime,
): String {
	return stringResource(
		if (startDateTime.dayOfYear == endDateTime.dayOfYear)
			R.string.infocenter_timeformat_sameday
		else
			R.string.infocenter_timeformat,
		startDateTime.toString(DateTimeFormat.mediumDate()),
		startDateTime.toString(DateTimeFormat.shortTime()),
		endDateTime.toString(DateTimeFormat.mediumDate()),
		endDateTime.toString(DateTimeFormat.shortTime())
	)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AbsenceFilterDialog(
	preferences: DataStorePreferences,
	onDismiss: () -> Unit
) {
	var dismissed by rememberSaveable { mutableStateOf(false) }
	fun dismiss() {
		dismissed = true
		onDismiss()
	}
	BackHandler(
		enabled = !dismissed,
	) {
		dismiss()
	}

	Scaffold(
		topBar = {
			CenterAlignedTopAppBar(
				title = { Text(text = stringResource(id = R.string.infocenter_absences_filter)) },
				navigationIcon = {
					IconButton(onClick = {
						dismiss()
					}) {
						Icon(
							imageVector = Icons.Outlined.Close,
							contentDescription = stringResource(id = R.string.all_close)
						)
					}
				}
			)
		}
	) { padding ->
		Box(modifier = Modifier.padding(padding)) {
			VerticalScrollColumn {
				// TODO implement protostore
				/*val sortReversed by preferences.infocenterAbsencesSortReverse.getState()
				SwitchPreference(
					title = { Text(text = stringResource(id = R.string.infocenter_absences_filter_only_unexcused)) },
					dataStore = preferences.infocenterAbsencesOnlyUnexcused
				)
				SwitchPreference(
					title = { Text(text = stringResource(id = R.string.infocenter_absences_filter_sort)) },
					summary = {
						if (sortReversed)
							Text(text = stringResource(id = R.string.infocenter_absences_filter_oldest_first))
						else
							Text(text = stringResource(id = R.string.infocenter_absences_filter_newest_first))
					},
					dataStore = preferences.infocenterAbsencesSortReverse
				)
				ListPreference(
					title = { Text(text = stringResource(id = R.string.infocenter_absences_filter_time_ranges)) },
					dataStore = preferences.infocenterAbsencesTimeRange,
					entries = stringArrayResource(id = R.array.infocenter_absences_list_values),
					entryLabels = stringArrayResource(id = R.array.infocenter_absences_list)
				)*/
			}
		}
	}
}
