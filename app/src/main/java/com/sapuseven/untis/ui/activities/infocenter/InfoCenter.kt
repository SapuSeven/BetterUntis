package com.sapuseven.untis.ui.activities.infocenter

import android.widget.TextView
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import com.sapuseven.untis.R
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.models.UntisAbsence
import com.sapuseven.untis.models.UntisMessage
import com.sapuseven.untis.models.UntisOfficeHour
import com.sapuseven.untis.models.untis.UntisAttachment
import com.sapuseven.untis.preferences.DataStorePreferences
import com.sapuseven.untis.ui.activities.infocenter.InfoCenterState.Companion.ID_ABSENCES
import com.sapuseven.untis.ui.activities.infocenter.InfoCenterState.Companion.ID_EVENTS
import com.sapuseven.untis.ui.activities.infocenter.InfoCenterState.Companion.ID_MESSAGES
import com.sapuseven.untis.ui.activities.infocenter.InfoCenterState.Companion.ID_OFFICEHOURS
import com.sapuseven.untis.ui.common.AppScaffold
import com.sapuseven.untis.ui.animations.fullscreenDialogAnimationEnter
import com.sapuseven.untis.ui.animations.fullscreenDialogAnimationExit
import com.sapuseven.untis.ui.common.NavigationBarInset
import com.sapuseven.untis.ui.common.VerticalScrollColumn
import com.sapuseven.untis.ui.dialogs.AttachmentsDialog
import com.sapuseven.untis.ui.preferences.ListPreference
import com.sapuseven.untis.ui.preferences.SwitchPreference
import kotlinx.coroutines.launch
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun InfoCenter(state: InfoCenterState) {
	AppScaffold(
		topBar = {
			CenterAlignedTopAppBar(
				title = {
					Text(stringResource(id = R.string.activity_title_info_center))
				},
				navigationIcon = {
					IconButton(onClick = { state.onBackClick() }) {
						Icon(
							imageVector = Icons.Outlined.ArrowBack,
							contentDescription = stringResource(id = R.string.all_back)
						)
					}
				},
				actions = {
					if (state.selectedItem.value == ID_ABSENCES) {
						IconButton(
							onClick = {
								state.showAbsenceFilter.value = true
							}
						) {
							Icon(painter = painterResource(id = R.drawable.all_filter), contentDescription = null)
						}
					}
				}
			)
		}
	) { innerPadding ->
		val coroutineScope = rememberCoroutineScope()

		Column(
			modifier = Modifier
				.padding(innerPadding)
				.fillMaxSize()
		) {
			// Initially load all items
			LaunchedEffect(Unit) {
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
			}

			Box(
				contentAlignment = Alignment.Center,
				modifier = Modifier
					.fillMaxWidth()
					.weight(1f)
			) {
				when (state.selectedItem.value) {
					ID_MESSAGES -> MessageList(state.messageList, state.messagesLoading.value)
					ID_EVENTS -> EventList(state.eventList, state.eventsLoading.value)
					ID_ABSENCES -> AbsenceList(state.absenceList, state.absencesLoading.value)
					ID_OFFICEHOURS -> OfficeHourList(state.officeHourList, state.officeHoursLoading.value)
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
					selected = state.isItemSelected(ID_MESSAGES),
					onClick = state.onItemSelect(ID_MESSAGES)
				)

				NavigationBarItem(
					icon = {
						Icon(
							painterResource(id = R.drawable.infocenter_events),
							contentDescription = null
						)
					},
					label = { Text(stringResource(id = R.string.menu_infocenter_events)) },
					selected = state.isItemSelected(ID_EVENTS),
					onClick = state.onItemSelect(ID_EVENTS)
				)

				if (state.shouldShowAbsences)
					NavigationBarItem(
						icon = {
							Icon(
								painterResource(id = R.drawable.infocenter_absences),
								contentDescription = null
							)
						},
						label = { Text(stringResource(id = R.string.menu_infocenter_absences)) },
						selected = state.isItemSelected(ID_ABSENCES),
						onClick = state.onItemSelect(ID_ABSENCES)
					)

				if (state.shouldShowOfficeHours)
					NavigationBarItem(
						icon = {
							Icon(
								painterResource(id = R.drawable.infocenter_contact),
								contentDescription = null
							)
						},
						label = { Text(stringResource(id = R.string.menu_infocenter_officehours)) },
						selected = state.isItemSelected(ID_OFFICEHOURS),
						onClick = state.onItemSelect(ID_OFFICEHOURS)
					)
			}
		}
	}

	AnimatedVisibility(
		visible = state.showAbsenceFilter.value,
		enter = fullscreenDialogAnimationEnter(),
		exit = fullscreenDialogAnimationExit()
	) {
		AbsenceFilterDialog(state.preferences) {
			state.showAbsenceFilter.value = false
		}
	}
}

@Composable
private fun <T> ItemList(
	items: List<T>?,
	itemRenderer: @Composable (T) -> Unit,
	@StringRes itemsEmptyMessage: Int,
	loading: Boolean
) {
	if (loading) {
		CircularProgressIndicator()
	} else if (items.isNullOrEmpty()) {
		Text(
			text = stringResource(id = itemsEmptyMessage),
			textAlign = TextAlign.Center,
			modifier = Modifier.fillMaxWidth()
		)
	} else {
		LazyColumn(
			modifier = Modifier.fillMaxSize()
		) {
			items(items) {
				itemRenderer(it)
			}
		}
	}
}

@Composable
private fun MessageList(messages: List<UntisMessage>?, loading: Boolean) {
	var attachmentsDialog by remember { mutableStateOf<List<UntisAttachment>?>(null) }

	ItemList(
		items = messages,
		itemRenderer = { MessageItem(it) { attachments -> attachmentsDialog = attachments } },
		itemsEmptyMessage = R.string.infocenter_messages_empty,
		loading = loading
	)

	attachmentsDialog?.let { attachments ->
		AttachmentsDialog(
			attachments = attachments,
			onDismiss = { attachmentsDialog = null }
		)
	}
}

@Composable
private fun EventList(events: List<EventListItem>?, loading: Boolean) {
	ItemList(
		items = events,
		itemRenderer = { EventItem(it) },
		itemsEmptyMessage = R.string.infocenter_events_empty,
		loading = loading
	)
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
private fun MessageItem(
	item: UntisMessage,
	onShowAttachments: (List<UntisAttachment>) -> Unit
) {
	val textColor = MaterialTheme.colorScheme.onSurfaceVariant

	ListItem(
		headlineContent = { Text(item.subject) },
		supportingContent = {
			AndroidView(
				factory = { context ->
					TextView(context).apply {
						setTextColor(textColor.toArgb())
					}
				},
				update = {
					it.text = HtmlCompat.fromHtml(item.body, HtmlCompat.FROM_HTML_MODE_COMPACT)
				}
			)
		},
		trailingContent = if (item.attachments.isNotEmpty()) {
			{
				IconButton(onClick = {
					onShowAttachments(item.attachments)
				}) {
					Icon(
						painter = painterResource(id = R.drawable.infocenter_attachments),
						contentDescription = stringResource(id = R.string.infocenter_messages_attachments)
					)
				}
			}
		} else null
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventItem(item: EventListItem) {
	if (item.exam != null) {
		val subject = item.timetableDatabaseInterface.getShortName(
			item.exam.subjectId,
			TimetableDatabaseInterface.Type.SUBJECT
		)
		ListItem(
			overlineContent = {
				Text(
					formatExamTime(
						item.exam.startDateTime.toLocalDateTime(),
						item.exam.endDateTime.toLocalDateTime()
					)
				)
			},
			headlineContent = {
				Text(
					if (!item.exam.name.contains(subject)) stringResource(
						R.string.infocenter_events_exam_name_long,
						subject,
						item.exam.name
					) else item.exam.name
				)
			}
		)
	}

	if (item.homework != null) {
		ListItem(
			overlineContent = {
				Text(
					item.homework.endDate.toLocalDate().toString(DateTimeFormat.mediumDate())
				)
			},
			headlineContent = {
				Text(
					item.timetableDatabaseInterface.getLongName(
						item.lessonsById?.get(item.homework.lessonId.toString())?.subjectId
							?: 0, TimetableDatabaseInterface.Type.SUBJECT
					)
				)
			},
			supportingContent = if (item.homework.text.isNotBlank()) {
				{ Text(item.homework.text) }
			} else null
		)
	}
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
}

@Composable
private fun formatExamTime(startDateTime: LocalDateTime, endDateTime: LocalDateTime): String {
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
		Box(modifier = Modifier.padding(padding)){
			VerticalScrollColumn {
				val sortReversed by preferences.infocenterAbsencesSortReverse.getState()
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
				)
			}
		}
	}
}
