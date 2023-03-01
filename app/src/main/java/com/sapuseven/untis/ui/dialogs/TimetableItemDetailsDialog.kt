package com.sapuseven.untis.ui.dialogs

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import com.sapuseven.untis.R
import com.sapuseven.untis.activities.BaseComposeActivity
import com.sapuseven.untis.data.connectivity.UntisApiConstants
import com.sapuseven.untis.data.connectivity.UntisApiConstants.CAN_READ_LESSON_TOPIC
import com.sapuseven.untis.data.connectivity.UntisApiConstants.CAN_READ_STUDENT_ABSENCE
import com.sapuseven.untis.data.connectivity.UntisApiConstants.CAN_WRITE_LESSON_TOPIC
import com.sapuseven.untis.data.connectivity.UntisApiConstants.CAN_WRITE_STUDENT_ABSENCE
import com.sapuseven.untis.data.connectivity.UntisAuthentication
import com.sapuseven.untis.data.connectivity.UntisRequest
import com.sapuseven.untis.data.databases.entities.User
import com.sapuseven.untis.data.timetable.PeriodData
import com.sapuseven.untis.helpers.DateTimeUtils
import com.sapuseven.untis.helpers.SerializationUtils
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.models.UntisAbsence
import com.sapuseven.untis.models.untis.UntisAttachment
import com.sapuseven.untis.models.untis.UntisDateTime
import com.sapuseven.untis.models.untis.UntisError
import com.sapuseven.untis.models.untis.UntisTime
import com.sapuseven.untis.models.untis.params.*
import com.sapuseven.untis.models.untis.response.*
import com.sapuseven.untis.models.untis.timetable.Period
import com.sapuseven.untis.models.untis.timetable.PeriodElement
import com.sapuseven.untis.ui.animations.fullscreenDialogAnimationEnter
import com.sapuseven.untis.ui.animations.fullscreenDialogAnimationExit
import com.sapuseven.untis.ui.common.AppScaffold
import com.sapuseven.untis.ui.common.SmallCircularProgressIndicator
import com.sapuseven.untis.ui.common.VerticalScrollColumn
import com.sapuseven.untis.ui.common.conditional
import com.sapuseven.untis.ui.functional.bottomInsets
import com.sapuseven.untis.ui.functional.insetsPaddingValues
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat

@OptIn(
	ExperimentalMaterial3Api::class, ExperimentalPagerApi::class,
	ExperimentalAnimationApi::class
)
@Composable
fun BaseComposeActivity.TimetableItemDetailsDialog(
	timegridItems: List<PeriodData>,
	initialPage: Int = 0,
	user: User,
	timetableDatabaseInterface: TimetableDatabaseInterface,
	onDismiss: (requestedElement: PeriodElement?) -> Unit
) {
	var dismissed by rememberSaveable { mutableStateOf(false) }
	val pagerState = rememberPagerState(initialPage)
	val scope = rememberCoroutineScope()
	val context = LocalContext.current

	var absenceCheck by rememberSaveable { mutableStateOf<Triple<Int, UntisDateTime, UntisDateTime>?>(null) }

	var detailedAbsenceCheck by rememberSaveable { mutableStateOf<Pair<Pair<Int, UntisStudent>, Pair<UntisDateTime, UntisDateTime>>?>(null) }
	var studentName by rememberSaveable { mutableStateOf<String?>(null) }

	var untisPeriodData by remember { mutableStateOf<UntisPeriodData?>(null) }
	var untisStudents by rememberSaveable { mutableStateOf<List<UntisStudent>?>(null) }
	var error by remember { mutableStateOf<Throwable?>(null) }
	val errorMessage = error?.message?.let { stringResource(id = R.string.all_error_details, it) }
	val errorMessageGeneric = stringResource(id = R.string.errormessagedictionary_generic)

	fun dismiss(requestedElement: PeriodElement? = null) {
		onDismiss(requestedElement)
		dismissed = true
	}

	BackHandler(
		enabled = !dismissed,
	) {
		dismiss()
	}

	AppScaffold(
		topBar = {
			CenterAlignedTopAppBar(
				title = { Text(studentName ?: stringResource(id = R.string.all_lesson_details)) },
				navigationIcon = {
					IconButton(onClick = {
						if (absenceCheck != null && detailedAbsenceCheck == null)
							absenceCheck = null
						else if (detailedAbsenceCheck != null)
							detailedAbsenceCheck = null
						else
							dismiss()
					}) {
						Icon(
							imageVector = Icons.Outlined.Close,
							contentDescription = stringResource(id = R.string.all_close)
						)
					}
				}
			)
		},
		floatingActionButton = {
			var loading by rememberSaveable { mutableStateOf(false) }

			AnimatedVisibility(
				visible = absenceCheck != null,
				enter = scaleIn(),
				exit = scaleOut()
			) {
				FloatingActionButton(
					modifier = Modifier.bottomInsets(),
					onClick = {
						loading = true

						if (detailedAbsenceCheck != null){
							scope.launch {
								createAbsence(
									user = user,
									ttId = detailedAbsenceCheck?.first?.first ?: -1,
									student = detailedAbsenceCheck?.first?.second!!,
									startDateTime = detailedAbsenceCheck?.second?.first!!.toLocalDateTime(),
									endDateTime = detailedAbsenceCheck?.second?.second!!.toLocalDateTime()
								).fold({
									untisPeriodData = untisPeriodData?.copy(
										absences = untisPeriodData?.absences?.plus(it)
									)
								}, {
									Toast
										.makeText(context, it.message, Toast.LENGTH_LONG)
										.show()
								})
								detailedAbsenceCheck = null
								loading = false
							}
							return@FloatingActionButton
						}


						scope.launch {
							submitAbsencesChecked(
								user,
								absenceCheck?.first ?: -1
							).fold({
								if (it) {
									untisPeriodData = untisPeriodData?.copy(
										absenceChecked = true
									)
									absenceCheck = null
								} else
									Toast
										.makeText(context, errorMessageGeneric, Toast.LENGTH_LONG)
										.show()
							}, {
								Toast
									.makeText(context, it.message, Toast.LENGTH_LONG)
									.show()
							})
							loading = false
						}
					}
				) {
					if (loading)
						SmallCircularProgressIndicator()
					else
						Icon(
							painter = painterResource(id = R.drawable.all_check),
							contentDescription = stringResource(R.string.all_dialog_absences_save)
						)
				}
			}
		}
	) { innerPadding ->
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			modifier = Modifier
				.padding(innerPadding)
				.bottomInsets()
				.fillMaxSize()
		) {
			HorizontalPager(
				count = timegridItems.size,
				state = pagerState,
				modifier = Modifier
					.weight(1f)
			) { page ->
				timegridItems[page].also { periodData ->
					val title = periodData.getLong(
						TimetableDatabaseInterface.Type.SUBJECT
					).let { title ->
						if (periodData.isCancelled())
							stringResource(R.string.all_lesson_cancelled, title)
						else if (periodData.isIrregular())
							stringResource(R.string.all_lesson_irregular, title)
						else if (periodData.isExam())
							stringResource(R.string.all_lesson_exam, title)
						else
							title
					}

					val time = stringResource(
						R.string.main_dialog_itemdetails_timeformat,
						periodData.element.startDateTime.toLocalDateTime()
							.toString(DateTimeFormat.shortTime()),
						periodData.element.endDateTime.toLocalDateTime()
							.toString(DateTimeFormat.shortTime())
					)

					var attachmentsDialog by rememberSaveable {
						mutableStateOf<List<UntisAttachment>?>(
							null
						)
					}

					var lessonTopicEditDialog by rememberSaveable { mutableStateOf<Int?>(null) }

					var lessonTopicNew by rememberSaveable { mutableStateOf<String?>(null) }

					Column(
						horizontalAlignment = Alignment.CenterHorizontally,
						modifier = Modifier
							.fillMaxSize()
							.verticalScroll(rememberScrollState())
					) {
						Icon(
							painter = painterResource(R.drawable.all_subject),
							contentDescription = null,
							tint = MaterialTheme.colorScheme.tertiary,
							modifier = Modifier
								.padding(top = 24.dp, bottom = 8.dp)
								.size(dimensionResource(id = R.dimen.size_header_icon))
						)

						Text(
							text = title,
							style = MaterialTheme.typography.headlineSmall,
							textAlign = TextAlign.Center,
							modifier = Modifier.padding(horizontal = 8.dp)
						)

						Text(
							text = time,
							style = MaterialTheme.typography.labelLarge,
							modifier = Modifier.padding(top = 8.dp)
						)

						Divider(
							color = MaterialTheme.colorScheme.outline,
							modifier = Modifier
								.padding(top = 24.dp, bottom = 12.dp)
								.padding(horizontal = 16.dp)
						)

						timetableDatabaseInterface.run {
							LaunchedEffect(Unit) {
								loadPeriodData(
									user = user,
									period = periodData.element
								).fold({
									untisPeriodData =
										it.dataByTTId[periodData.element.id.toString()]
									untisStudents = it.referencedStudents
								}, {
									error = it
								})
							}

							// Lesson teachers
							TimetableItemDetailsDialogElement(
								elements = periodData.teachers,
								onElementClick = { dismiss(it) },
								useLongName = true,
								icon = {
									Icon(
										painter = painterResource(id = R.drawable.all_teachers),
										contentDescription = stringResource(id = R.string.all_teachers),
										tint = MaterialTheme.colorScheme.onSurface,
										modifier = Modifier.padding(start = 8.dp)
									)
								}
							)

							// Lesson classes
							TimetableItemDetailsDialogElement(
								elements = periodData.classes,
								onElementClick = { dismiss(it) },
								icon = {
									Icon(
										painter = painterResource(id = R.drawable.all_classes),
										contentDescription = stringResource(id = R.string.all_classes),
										tint = MaterialTheme.colorScheme.onSurface,
										modifier = Modifier.padding(start = 8.dp)
									)
								}
							)

							// Lesson rooms
							TimetableItemDetailsDialogElement(
								elements = periodData.rooms,
								onElementClick = { dismiss(it) },
								icon = {
									Icon(
										painter = painterResource(id = R.drawable.all_rooms),
										contentDescription = stringResource(id = R.string.all_rooms),
										tint = MaterialTheme.colorScheme.onSurface,
										modifier = Modifier.padding(start = 8.dp)
									)
								}
							)

							// Lesson info texts
							setOf(
								periodData.element.text.lesson,
								periodData.element.text.substitution,
								periodData.element.text.info
							).forEach {
								if (it.isNotBlank())
									ListItem(
										headlineText = { Text(it) },
										leadingContent = {
											Icon(
												painter = painterResource(id = R.drawable.all_info),
												contentDescription = stringResource(id = R.string.all_lesson_info),
												tint = MaterialTheme.colorScheme.onSurface,
												modifier = Modifier.padding(horizontal = 8.dp)
											)
										}
									)
							}

							// Lesson homeworks
							periodData.element.homeWorks?.forEach {
								val endDate = it.endDate.toLocalDate()

								ListItem(
									headlineText = { Text(it.text) },
									supportingText = {
										Text(
											stringResource(
												id = R.string.homeworks_due_time,
												endDate.toString(stringResource(R.string.homeworks_due_time_format))
											)
										)
									},
									leadingContent = {
										Icon(
											painter = painterResource(id = R.drawable.all_homework),
											contentDescription = stringResource(id = R.string.all_homework),
											tint = MaterialTheme.colorScheme.onSurface,
											modifier = Modifier.padding(horizontal = 8.dp)
										)
									},
									trailingContent = if (it.attachments.isNotEmpty()) {
										{
											IconButton(onClick = {
												attachmentsDialog = it.attachments
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

							// Lesson exam
							periodData.element.exam?.also {
								ListItem(
									headlineText = {
										Text(it.name ?: stringResource(id = R.string.all_exam))
									},
									supportingText = it.text?.let { { Text(it) } },
									leadingContent = {
										Icon(
											painter = painterResource(id = R.drawable.infocenter_exam),
											contentDescription = stringResource(id = R.string.all_exam),
											tint = MaterialTheme.colorScheme.onSurface,
											modifier = Modifier.padding(horizontal = 8.dp)
										)
									}
								)
							}

							// Online lesson
							if (periodData.element.isOnlinePeriod == true) {
								ListItem(
									headlineText = { Text(stringResource(R.string.all_lesson_online)) },
									leadingContent = {
										Icon(
											painter = painterResource(id = R.drawable.all_lesson_online),
											contentDescription = stringResource(id = R.string.all_lesson_info),
											tint = MaterialTheme.colorScheme.onSurface,
											modifier = Modifier.padding(horizontal = 8.dp)
										)
									},
									trailingContent = periodData.element.onlinePeriodLink?.let {
										{
											IconButton(onClick = {
												openUrl(it)
											}) {
												Icon(
													painter = painterResource(id = R.drawable.all_open_in_new),
													contentDescription = stringResource(R.string.all_open_link)
												)
											}
										}
									}
								)
							}

							// Lesson absence check
							if (periodData.element.can.contains(CAN_READ_STUDENT_ABSENCE))
								TimetableItemDetailsDialogWithPeriodData(
									periodData = periodData,
									untisPeriodData = untisPeriodData,
									error = error,
									errorMessage = errorMessage,
									editPermission = CAN_WRITE_STUDENT_ABSENCE,
									headlineText = {
										Text(stringResource(id = R.string.all_absences))
									},
									supportingText = {
										Text(
											stringResource(
												if (it.absenceChecked) R.string.all_dialog_absences_checked
												else R.string.all_dialog_absences_not_checked
											)
										)
									},
									leadingContent = {
										Icon(
											painter = painterResource(
												if (it?.absenceChecked == true)
													R.drawable.all_absences_checked
												else
													R.drawable.all_absences
											),
											contentDescription = stringResource(id = R.string.all_absences),
											tint = MaterialTheme.colorScheme.onSurface,
											modifier = Modifier.padding(horizontal = 8.dp)
										)
									},
									onClick = {
										absenceCheck = periodData.element.let { Triple(it.id, it.startDateTime, it.endDateTime) }
									}
								)

							// Lesson topic
							if (periodData.element.can.contains(CAN_READ_LESSON_TOPIC))
								TimetableItemDetailsDialogWithPeriodData(
									periodData = periodData,
									untisPeriodData = untisPeriodData,
									error = error,
									errorMessage = errorMessage,
									editPermission = CAN_WRITE_LESSON_TOPIC,
									headlineText = {
										Text(stringResource(id = R.string.all_lessontopic))
									},
									supportingText = {
										val topic = lessonTopicNew ?: it.topic?.text

										Text(
											if (topic.isNullOrBlank())
												if (periodData.element.can.contains(
														CAN_WRITE_LESSON_TOPIC
													)
												)
													stringResource(R.string.all_hint_tap_to_edit)
												else
													stringResource(R.string.all_lessontopic_none)
											else
												topic
										)
									},
									leadingContent = {
										Icon(
											painter = painterResource(id = R.drawable.all_lessontopic),
											contentDescription = stringResource(id = R.string.all_lessontopic),
											tint = MaterialTheme.colorScheme.onSurface,
											modifier = Modifier.padding(horizontal = 8.dp)
										)
									},
									onClick = {
										lessonTopicEditDialog = periodData.element.id
									}
								)
						}

						attachmentsDialog?.let { attachments ->
							AttachmentsDialog(
								attachments = attachments,
								onDismiss = { attachmentsDialog = null }
							)
						}

						lessonTopicEditDialog?.let { id ->
							var text by rememberSaveable { mutableStateOf("") }
							var loading by rememberSaveable { mutableStateOf(false) }
							var dialogError by rememberSaveable { mutableStateOf<String?>(null) }

							DynamicHeightAlertDialog(
								title = { Text(stringResource(id = R.string.all_lessontopic_edit)) },
								text = {
									Column(
										modifier = Modifier.fillMaxWidth()
									) {
										OutlinedTextField(
											value = text,
											onValueChange = { text = it },
											isError = dialogError != null,
											enabled = !loading,
											label = { Text(stringResource(id = R.string.all_lessontopic)) },
											modifier = Modifier.fillMaxWidth()
										)

										AnimatedVisibility(visible = dialogError != null) {
											Text(
												modifier = Modifier.padding(
													horizontal = 16.dp,
													vertical = 4.dp
												),
												color = MaterialTheme.colorScheme.error,
												style = MaterialTheme.typography.bodyMedium,
												text = dialogError ?: ""
											)
										}
									}
								},
								onDismissRequest = { lessonTopicEditDialog = null },
								confirmButton = {
									TextButton(
										enabled = !loading,
										onClick = {
											loading = true

											scope.launch {
												submitLessonTopic(user, id, text).fold({
													lessonTopicNew = it
													lessonTopicEditDialog = null
												}, {
													dialogError = it.message
													loading = false
												})
											}
										}) {
										Text(stringResource(id = R.string.all_ok))
									}
								},
								dismissButton = {
									TextButton(
										enabled = !loading,
										onClick = { lessonTopicEditDialog = null }) {
										Text(stringResource(id = R.string.all_cancel))
									}
								}
							)
						}
					}
				}
			}

			if (timegridItems.size > 1)
				HorizontalPagerIndicator(
					pagerState = pagerState,
					activeColor = MaterialTheme.colorScheme.primary,
					inactiveColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
					modifier = Modifier
						.align(Alignment.CenterHorizontally)
						.padding(16.dp),
				)
		}

		AnimatedVisibility(
			visible = absenceCheck != null,
			enter = fullscreenDialogAnimationEnter(),
			exit = fullscreenDialogAnimationExit()
		) {
			BackHandler(
				enabled = absenceCheck != null && detailedAbsenceCheck == null,
			) {
				absenceCheck = null
			}

			LazyColumn(
				modifier = Modifier
					.padding(innerPadding)
					.fillMaxSize()
					.background(MaterialTheme.colorScheme.surface),
				contentPadding = insetsPaddingValues()
			) {
				val students = untisPeriodData?.studentIds?.let { studentIds ->
					studentIds.mapNotNull { studentId -> untisStudents?.find { it.id == studentId } }
				} ?: emptyList()

				items(students) { student ->
					var loading by remember { mutableStateOf(false) }
					val absence =
						untisPeriodData?.absences?.findLast { it.studentId == student.id }

					ListItem(
						headlineText = {
							Text(text = student.fullName())
						},
						supportingText = absence?.let {
							{
								it.text
							}
						},
						leadingContent = {
							if (loading)
								SmallCircularProgressIndicator()
							else if (absence != null)
								Icon(
									painterResource(id = R.drawable.all_cross),
									contentDescription = "Absent"
								)
							else
								Icon(
									painterResource(id = R.drawable.all_check),
									contentDescription = "Present"
								)
						},
						modifier = Modifier.conditional(
							detailedAbsenceCheck == null,
						) {
							this.clickable {
								absence?.let {
									loading = true

									scope.launch {
										deleteAbsence(
											user,
											absence
										).fold({
											if (it)
												untisPeriodData = untisPeriodData?.copy(
													absences = untisPeriodData?.absences?.minus(
														absence
													)
												)
											else
												Toast
													.makeText(context, errorMessageGeneric, Toast.LENGTH_LONG)
													.show()
										}, {
											Toast
												.makeText(context, it.message, Toast.LENGTH_LONG)
												.show()
										})
										loading = false
									}
								} ?: absenceCheck?.let { absenceCheckPeriod ->
									loading = true

									scope.launch {
										createAbsence(
											user,
											absenceCheckPeriod.first,
											student,
											absenceCheckPeriod.second.toLocalDateTime(),
											absenceCheckPeriod.third.toLocalDateTime()
										).fold({
											untisPeriodData = untisPeriodData?.copy(
												absences = untisPeriodData?.absences?.plus(it)
											)
										}, {
											Toast
												.makeText(context, it.message, Toast.LENGTH_LONG)
												.show()
										})
										loading = false
									}
								}
							}
						},
						trailingContent = {
							IconButton(
								onClick = {
									detailedAbsenceCheck = (absenceCheck!!.first to student) to (absenceCheck!!.second to absenceCheck!!.third)
								}
							){
								Icon(painter = painterResource(id = R.drawable.notification_clock), contentDescription = null)
							}
						}
					)
				}
			}
		}

		AnimatedVisibility(
			visible = detailedAbsenceCheck != null,
			enter = fullscreenDialogAnimationEnter(),
			exit = fullscreenDialogAnimationExit()
		) {
			studentName = detailedAbsenceCheck?.first?.second?.fullName()

			val startTimePickerState = rememberTimePickerState(
				initialHour = detailedAbsenceCheck?.second?.first?.toLocalDateTime()?.hourOfDay ?: LocalDateTime.now().hourOfDay,
				initialMinute = detailedAbsenceCheck?.second?.first?.toLocalDateTime()?.minuteOfHour?: LocalDateTime.now().minuteOfHour,
				is24Hour = true
			)
			val endTimePickerState = rememberTimePickerState(
				initialHour = detailedAbsenceCheck?.second?.second?.toLocalDateTime()?.hourOfDay ?: LocalDateTime.now().hourOfDay,
				initialMinute = detailedAbsenceCheck?.second?.second?.toLocalDateTime()?.minuteOfHour?: LocalDateTime.now().minuteOfHour,
				is24Hour = true
			)

			BackHandler(
				enabled = detailedAbsenceCheck != null,
			) {
				detailedAbsenceCheck = null
				studentName = null
			}
			Box(
				modifier = Modifier
					.padding(innerPadding)
					.fillMaxSize()
					.background(MaterialTheme.colorScheme.surface)
			) {
				VerticalScrollColumn {
					var showStartTimePicker by remember { mutableStateOf(false) }
					var showEndTimePicker by remember { mutableStateOf(false) }


					ListItem(
						modifier = Modifier.clickable {
							showStartTimePicker = true
						},
						headlineText = {
							Text(text = "Start")
						},
						trailingContent = {
							Text(
								text = detailedAbsenceCheck?.second?.first?.toLocalDateTime()
									?.toString(DateTimeFormat.forStyle("MS").withLocale(context.resources.configuration.locale))
									?: "",
								style = MaterialTheme.typography.labelLarge
							)
						}
					)

					ListItem(
						modifier = Modifier.clickable {
							showEndTimePicker = true
						},
						headlineText = {
							Text(text = "End")
						},
						trailingContent = {
							Text(
								text = detailedAbsenceCheck?.second?.second?.toLocalDateTime()
									?.toString(DateTimeFormat.forStyle("MS").withLocale(context.resources.configuration.locale))
									?: "",
								style = MaterialTheme.typography.labelLarge
							)
						}
					)

					if (showStartTimePicker){
						TimePickerDialog(
							onConfirm = {
								val time = detailedAbsenceCheck!!.second.first.toLocalDateTime()
									.withTime(startTimePickerState.hour, startTimePickerState.minute, 0, 0)
								detailedAbsenceCheck =
									detailedAbsenceCheck!!.first to (UntisDateTime(time) to detailedAbsenceCheck?.second?.second!!)
								showStartTimePicker = false
							},
							onCancel = {
								showStartTimePicker = false
							}
						) {
							TimePicker(
								state = startTimePickerState
							)
						}
					}
					if (showEndTimePicker) {
						TimePickerDialog(
							onConfirm = {
								val time = detailedAbsenceCheck!!.second.second.toLocalDateTime()
									.withTime(endTimePickerState.hour, endTimePickerState.minute, 0, 0)
								detailedAbsenceCheck =
									detailedAbsenceCheck!!.first to (detailedAbsenceCheck?.second?.first!! to UntisDateTime(
										time
									))
								showEndTimePicker = false
							},
							onCancel = {
								showEndTimePicker = false
							}
						) {
							TimePicker(
								state = endTimePickerState
							)
						}
					}

				}
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimetableItemDetailsDialogWithPeriodData(
	periodData: PeriodData,
	untisPeriodData: UntisPeriodData?,
	error: Throwable?,
	errorMessage: String?,
	editPermission: String? = null,
	headlineText: @Composable () -> Unit,
	supportingText: @Composable (UntisPeriodData) -> Unit,
	leadingContent: @Composable (UntisPeriodData?) -> Unit,
	onClick: () -> Unit
) {
	val context = LocalContext.current
	val canEdit =
		periodData.element.can.contains(editPermission) || untisPeriodData?.can?.contains(
			editPermission
		) == true

	ListItem(
		headlineText = headlineText,
		supportingText = {
			untisPeriodData?.let {
				supportingText(it)
			} ?: error?.let {
				Text(stringResource(R.string.all_error))
			} ?: Text(stringResource(R.string.loading))
		},
		leadingContent = { leadingContent(untisPeriodData) },
		modifier = Modifier
			.conditional(error != null) {
				clickable {
					Toast
						.makeText(
							context,
							errorMessage,
							Toast.LENGTH_LONG
						)
						.show()
				}
			}
			.conditional(error == null && canEdit) {
				clickable {
					onClick()
				}
			}
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimetableDatabaseInterface.TimetableItemDetailsDialogElement(
	elements: Set<PeriodElement>,
	icon: (@Composable () -> Unit)? = null,
	useLongName: Boolean = false,
	onElementClick: (element: PeriodElement) -> Unit
) {
	if (elements.isNotEmpty())
		ListItem(
			headlineText = {
				Row(
					modifier = Modifier.horizontalScroll(rememberScrollState()),
					horizontalArrangement = Arrangement.spacedBy(8.dp)
				) {
					elements.forEach { element ->
						Text(
							text = if (useLongName) getLongName(element) else getShortName(element),
							modifier = Modifier
								.clip(RoundedCornerShape(50))
								.clickable {
									onElementClick(element)
								}
								.padding(8.dp)
						)

						if (element.id != element.orgId)
							element.copy(id = element.orgId).let { orgElement ->
								Text(
									text = if (useLongName) getLongName(orgElement) else getShortName(
										orgElement
									),
									style = LocalTextStyle.current.copy(textDecoration = TextDecoration.LineThrough),
									modifier = Modifier
										.clip(RoundedCornerShape(50))
										.clickable {
											onElementClick(orgElement)
										}
										.padding(8.dp)
								)
							}
					}
				}
			},
			leadingContent = icon
		)
}

private suspend fun loadPeriodData(
    user: User,
    period: Period
): Result<PeriodDataResult> {
	val query = UntisRequest.UntisRequestQuery(user).apply {
		data.method = UntisApiConstants.METHOD_GET_PERIOD_DATA
		data.params = listOf(
			PeriodDataParams(
				listOf(period.id),
				UntisAuthentication.createAuthObject(user)
			)
		)
	}

	val result = UntisRequest().request(query)
	return result.fold({ data ->
		val untisResponse = SerializationUtils.getJSON().decodeFromString<PeriodDataResponse>(data)

		untisResponse.result?.let {
			Result.success(it)
		} ?: Result.failure(UntisApiException(untisResponse.error))
	}, {
		Result.failure(it.exception)
	})
}

private suspend fun createAbsence(
    user: User,
    ttId: Int,
    student: UntisStudent,
    startDateTime: LocalDateTime,
    endDateTime: LocalDateTime
): Result<UntisAbsence> {
	val query =
		UntisRequest.UntisRequestQuery(user).apply {
			data.method = UntisApiConstants.METHOD_CREATE_IMMEDIATE_ABSENCE
			data.params = listOf(
				CreateImmediateAbsenceParams(
					ttId,
					student.id,
					UntisTime(startDateTime.toString(DateTimeUtils.tTimeNoSeconds())),
					UntisTime(endDateTime.toString(DateTimeUtils.tTimeNoSeconds())),
					UntisAuthentication.createAuthObject(user)
				)
			)
		}

	return UntisRequest().request(query).fold({ data ->
		val untisResponse =
			SerializationUtils.getJSON().decodeFromString<CreateImmediateAbsenceResponse>(data)

		untisResponse.result?.let {
			Result.success(it.absences[0])
		} ?: Result.failure(UntisApiException(untisResponse.error))
	}, {
		Result.failure(it)
	})
}

private suspend fun deleteAbsence(
    user: User,
    absence: UntisAbsence
): Result<Boolean> {
	val query =
		UntisRequest.UntisRequestQuery(user).apply {
			data.method = UntisApiConstants.METHOD_DELETE_ABSENCE
			data.params = listOf(
				DeleteAbsenceParams(
					absence.id,
					UntisAuthentication.createAuthObject(user)
				)
			)
		}

	return UntisRequest().request(query).fold({ data ->
		val untisResponse =
			SerializationUtils.getJSON().decodeFromString<DeleteAbsenceResponse>(data)

		untisResponse.result?.let {
			Result.success(it.success)
		} ?: Result.failure(UntisApiException(untisResponse.error))
	}, {
		Result.failure(it)
	})
}

private suspend fun submitAbsencesChecked(
    user: User,
    ttId: Int
): Result<Boolean> {
	val query =
		UntisRequest.UntisRequestQuery(user).apply {
			data.method = UntisApiConstants.METHOD_SUBMIT_ABSENCES_CHECKED
			data.params = listOf(
				AbsencesCheckedParams(
					listOf(ttId),
					UntisAuthentication.createAuthObject(user)
				)
			)
		}

	return UntisRequest().request(query).fold({ data ->
		// TODO: Create corresponding data model
		val untisResponse = SerializationUtils.getJSON().decodeFromString<BaseResponse>(data)

		untisResponse.error?.let {
			Result.failure(UntisApiException(it))
		} ?: Result.success(true)
	}, {
		Result.failure(it)
	})
}

private suspend fun submitLessonTopic(
    user: User,
    ttId: Int,
    lessonTopic: String
): Result<String> {
	val query =
		UntisRequest.UntisRequestQuery(user).apply {
			data.method = UntisApiConstants.METHOD_SUBMIT_LESSON_TOPIC
			data.params = listOf(
				SubmitLessonTopicParams(
					lessonTopic,
					ttId,
					UntisAuthentication.createAuthObject(user)
				)
			)
		}

	return UntisRequest().request(query).fold({ data ->
		// TODO: Create corresponding data model
		val untisResponse = SerializationUtils.getJSON().decodeFromString<BaseResponse>(data)

		untisResponse.error?.let {
			Result.failure(UntisApiException(it))
		} ?: Result.success(lessonTopic)
	}, {
		Result.failure(it.exception)
	})
}

class UntisApiException(error: UntisError?) : Throwable(error?.message)
