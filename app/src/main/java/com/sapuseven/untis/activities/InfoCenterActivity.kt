package com.sapuseven.untis.activities

import android.os.Bundle
import android.widget.TextView
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.text.HtmlCompat
import com.sapuseven.untis.R
import com.sapuseven.untis.data.connectivity.UntisApiConstants
import com.sapuseven.untis.data.connectivity.UntisApiConstants.RIGHT_ABSENCES
import com.sapuseven.untis.data.connectivity.UntisApiConstants.RIGHT_OFFICEHOURS
import com.sapuseven.untis.data.connectivity.UntisAuthentication
import com.sapuseven.untis.data.connectivity.UntisRequest
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.helpers.SerializationUtils.getJSON
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.models.*
import com.sapuseven.untis.models.untis.UntisAttachment
import com.sapuseven.untis.models.untis.UntisDate
import com.sapuseven.untis.models.untis.masterdata.SchoolYear
import com.sapuseven.untis.models.untis.params.*
import com.sapuseven.untis.models.untis.response.*
import com.sapuseven.untis.ui.dialogs.AttachmentsDialog
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import java.text.SimpleDateFormat
import java.util.*

class InfoCenterActivity : BaseComposeActivity() {
	private var api: UntisRequest = UntisRequest()

	companion object {
		private const val ID_MESSAGES = 1
		private const val ID_EVENTS = 2
		private const val ID_ABSENCES = 3
		private const val ID_OFFICEHOURS = 4
	}

	@OptIn(ExperimentalMaterial3Api::class)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			AppTheme {
				withUser { user ->
					Scaffold(
						topBar = {
							CenterAlignedTopAppBar(
								title = {
									Text(stringResource(id = R.string.activity_title_info_center))
								},
								navigationIcon = {
									IconButton(onClick = { finish() }) {
										Icon(
											imageVector = Icons.Outlined.ArrowBack,
											contentDescription = stringResource(id = R.string.all_back)
										)
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
							val showOfficeHours = user.userData.rights.contains(RIGHT_OFFICEHOURS)
							val showAbsences = user.userData.rights.contains(RIGHT_ABSENCES)

							var selectedItem by rememberSaveable { mutableStateOf(ID_MESSAGES) }

							var messages by remember { mutableStateOf<List<UntisMessage>?>(null) }
							var officeHours by remember {
								mutableStateOf<List<UntisOfficeHour>?>(
									null
								)
							}
							var events by remember { mutableStateOf<List<EventListItem>?>(null) }
							var absences by remember { mutableStateOf<List<UntisAbsence>?>(null) }

							var messagesLoading by remember { mutableStateOf(true) }
							var eventsLoading by remember { mutableStateOf(true) }
							var absencesLoading by remember { mutableStateOf(true) }
							var officeHoursLoading by remember { mutableStateOf(true) }

							SideEffect {
								coroutineScope.launch {
									messages = loadMessages(user)?.also {
										preferences["preference_last_messages_count"] = it.size
										preferences["preference_last_messages_date"] =
											SimpleDateFormat(
												"dd-MM-yyyy",
												Locale.US
											).format(Calendar.getInstance().time)
									}
									messagesLoading = false
								}

								coroutineScope.launch {
									events = loadEvents(user)
									eventsLoading = false
								}

								coroutineScope.launch {
									if (showAbsences)
										absences = loadAbsences(user)
									absencesLoading = false
								}

								coroutineScope.launch {
									if (showOfficeHours)
										officeHours = loadOfficeHours(user)
									officeHoursLoading = false
								}
							}

							Box(
								contentAlignment = Alignment.Center,
								modifier = Modifier
									.fillMaxWidth()
									.weight(1f)
							) {
								when (selectedItem) {
									ID_MESSAGES -> MessageList(messages, messagesLoading)
									ID_EVENTS -> EventList(events, eventsLoading)
									ID_ABSENCES -> AbsenceList(absences, absencesLoading)
									ID_OFFICEHOURS -> OfficeHourList(
										officeHours,
										officeHoursLoading
									)
								}
							}

							NavigationBar {
								NavigationBarItem(
									icon = {
										Icon(
											painterResource(id = R.drawable.infocenter_messages),
											contentDescription = null
										)
									},
									label = { Text(stringResource(id = R.string.menu_infocenter_messagesofday)) },
									selected = selectedItem == ID_MESSAGES,
									onClick = { selectedItem = ID_MESSAGES }
								)

								NavigationBarItem(
									icon = {
										Icon(
											painterResource(id = R.drawable.infocenter_events),
											contentDescription = null
										)
									},
									label = { Text(stringResource(id = R.string.menu_infocenter_events)) },
									selected = selectedItem == ID_EVENTS,
									onClick = { selectedItem = ID_EVENTS }
								)

								if (showAbsences)
									NavigationBarItem(
										icon = {
											Icon(
												painterResource(id = R.drawable.infocenter_absences),
												contentDescription = null
											)
										},
										label = { Text(stringResource(id = R.string.menu_infocenter_absences)) },
										selected = selectedItem == ID_ABSENCES,
										onClick = { selectedItem = ID_ABSENCES }
									)

								if (showOfficeHours)
									NavigationBarItem(
										icon = {
											Icon(
												painterResource(id = R.drawable.infocenter_contact),
												contentDescription = null
											)
										},
										label = { Text(stringResource(id = R.string.menu_infocenter_officehours)) },
										selected = selectedItem == ID_OFFICEHOURS,
										onClick = { selectedItem = ID_OFFICEHOURS }
									)
							}
						}
					}
				}
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
			headlineText = { Text(item.subject) },
			supportingText = {
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
			val subject = timetableDatabaseInterface.getShortName(
				item.exam.subjectId,
				TimetableDatabaseInterface.Type.SUBJECT
			)

			ListItem(
				overlineText = {
					Text(
						formatExamTime(
							item.exam.startDateTime.toLocalDateTime(),
							item.exam.endDateTime.toLocalDateTime()
						)
					)
				},
				headlineText = {
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
				overlineText = {
					Text(
						item.homework.endDate.toLocalDate().toString(DateTimeFormat.mediumDate())
					)
				},
				headlineText = {
					Text(
						timetableDatabaseInterface.getLongName(
							item.lessonsById?.get(item.homework.lessonId.toString())?.subjectId
								?: 0, TimetableDatabaseInterface.Type.SUBJECT
						)
					)
				},
				supportingText = if (item.homework.text.isNotBlank()) {
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
			overlineText = {
				Text(
					formatOfficeHourTime(
						item.startDateTime.toLocalDateTime(),
						item.endDateTime.toLocalDateTime()
					)
				)
			},
			headlineText = { Text(item.displayNameTeacher) },
			supportingText = if (body.isNotBlank()) {
				{ Text(body) }
			} else null
		)
	}

	@OptIn(ExperimentalMaterial3Api::class)
	@Composable
	private fun AbsenceItem(item: UntisAbsence) {
		ListItem(
			overlineText = {
				Text(
					formatAbsenceTime(
						item.startDateTime.toLocalDateTime(),
						item.endDateTime.toLocalDateTime()
					)
				)
			},
			headlineText = {
				Text(
					if (item.absenceReason.isNotEmpty())
						item.absenceReason.substring(0, 1)
							.uppercase(Locale.getDefault()) + item.absenceReason.substring(1)
					else
						stringResource(R.string.infocenter_absence_unknown_reason)
				)
			},
			supportingText = if (item.text.isNotBlank()) {
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
		endDateTime: LocalDateTime
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
		endDateTime: LocalDateTime
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

	private suspend fun loadMessages(user: UserDatabase.User): List<UntisMessage>? {
		val query = UntisRequest.UntisRequestQuery(user)

		query.data.method = UntisApiConstants.METHOD_GET_MESSAGES
		query.proxyHost = preferences["preference_connectivity_proxy_host", null]
		query.data.params = listOf(
			MessageParams(
				UntisDate.fromLocalDate(LocalDate.now()),
				auth = UntisAuthentication.createAuthObject(user)
			)
		)

		val result = api.request(query)
		return result.fold({ data ->
			val untisResponse = getJSON().decodeFromString<MessageResponse>(data)

			untisResponse.result?.messages
		}, { null /* TODO: Show error */ })
	}

	private suspend fun loadEvents(user: UserDatabase.User): List<EventListItem> {
		val events = mutableListOf<EventListItem>()
		loadExams(user)?.let { events.addAll(it) }
		loadHomeworks(user)?.let { events.addAll(it) }
		return events.toList().sortedBy {
			it.exam?.startDateTime?.toString() ?: it.homework?.endDate?.toString()
		}
	}

	private suspend fun loadExams(user: UserDatabase.User): List<EventListItem>? {
		val schoolYears = userDatabase.getAdditionalUserData<SchoolYear>(
			user.id,
			SchoolYear()
		)?.values?.toList()
			?: emptyList()
		getCurrentYear(schoolYears)?.endDate?.let { currentSchoolYearEndDate ->
			val query = UntisRequest.UntisRequestQuery(user)

			query.data.method = UntisApiConstants.METHOD_GET_EXAMS
			query.proxyHost = preferences["preference_connectivity_proxy_host", null]
			query.data.params = listOf(
				ExamParams(
					user.userData.elemId,
					user.userData.elemType ?: "",
					UntisDate.fromLocalDate(LocalDate.now()),
					UntisDate(currentSchoolYearEndDate),
					auth = UntisAuthentication.createAuthObject(user)
				)
			)

			val result = api.request(query)
			return result.fold({ data ->
				val untisResponse = getJSON().decodeFromString<ExamResponse>(data)

				untisResponse.result?.exams?.map { EventListItem(exam = it) }
			}, { null /* TODO: Show error */ })
		}
		return null
	}

	private suspend fun loadHomeworks(user: UserDatabase.User): List<EventListItem>? {
		val schoolYears = userDatabase.getAdditionalUserData<SchoolYear>(
			user.id,
			SchoolYear()
		)?.values?.toList()
			?: emptyList()
		getCurrentYear(schoolYears)?.endDate?.let { currentSchoolYearEndDate ->
			val query = UntisRequest.UntisRequestQuery(user)

			query.data.method = UntisApiConstants.METHOD_GET_HOMEWORKS
			query.proxyHost = preferences["preference_connectivity_proxy_host", null]
			query.data.params = listOf(
				HomeworkParams(
					user.userData.elemId,
					user.userData.elemType ?: "",
					UntisDate.fromLocalDate(LocalDate.now()),
					UntisDate(currentSchoolYearEndDate),
					auth = UntisAuthentication.createAuthObject(user)
				)
			)

			val result = api.request(query)
			return result.fold({ data ->
				val untisResponse = getJSON().decodeFromString<HomeworkResponse>(data)

				untisResponse.result?.homeWorks?.map {
					EventListItem(
						homework = it,
						lessonsById = untisResponse.result.lessonsById
					)
				}
			}, { null /* TODO: Show error */ })
		}
		return null
	}

	private suspend fun loadAbsences(user: UserDatabase.User): List<UntisAbsence>? {
		val query = UntisRequest.UntisRequestQuery(user)

		query.data.method = UntisApiConstants.METHOD_GET_ABSENCES
		query.proxyHost = preferences["preference_connectivity_proxy_host", null]
		query.data.params = listOf(
			AbsenceParams(
				UntisDate.fromLocalDate(LocalDate.now().minusYears(1)),
				UntisDate.fromLocalDate(LocalDate.now().plusMonths(1)),
				includeExcused = true,
				includeUnExcused = true,
				auth = UntisAuthentication.createAuthObject(user)
			)
		)

		val result = api.request(query)
		return result.fold({ data ->
			val untisResponse = getJSON().decodeFromString<AbsenceResponse>(data)

			untisResponse.result?.absences?.sortedBy { it.excused }
		}, { null /* TODO: Show error */ })
	}

	private suspend fun loadOfficeHours(user: UserDatabase.User): List<UntisOfficeHour>? {
		val query = UntisRequest.UntisRequestQuery(user)

		query.data.method = UntisApiConstants.METHOD_GET_OFFICEHOURS
		query.proxyHost = preferences["preference_connectivity_proxy_host", null]
		query.data.params = listOf(
			OfficeHoursParams(
				-1,
				UntisDate.fromLocalDate(LocalDate.now()),
				auth = UntisAuthentication.createAuthObject(user)
			)
		)

		val result = api.request(query)
		return result.fold({ data ->
			val untisResponse = getJSON().decodeFromString<OfficeHoursResponse>(data)

			untisResponse.result?.officeHours
		}, { null /* TODO: Show error */ })
	}

	private fun getCurrentYear(schoolYears: List<SchoolYear>): SchoolYear? {
		return schoolYears.find {
			val now = LocalDate.now()
			now.isAfter(LocalDate(it.startDate)) && now.isBefore(LocalDate(it.endDate))
		}
	}

	class EventListItem private constructor(
		@Suppress("unused") private val init: Nothing? = null, // Dummy parameter to avoid infinite constructor loops below
		val exam: UntisExam? = null,
		val homework: UntisHomework? = null,
		val lessonsById: Map<String, UntisHomeworkLesson>? = null
	) {
		constructor(exam: UntisExam) : this(
			init = null,
			exam = exam
		)

		constructor(homework: UntisHomework, lessonsById: Map<String, UntisHomeworkLesson>) : this(
			init = null,
			homework = homework,
			lessonsById = lessonsById
		)
	}
}
