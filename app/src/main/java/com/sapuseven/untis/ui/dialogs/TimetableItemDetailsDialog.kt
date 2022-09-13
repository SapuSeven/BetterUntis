package com.sapuseven.untis.ui.dialogs

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.sapuseven.untis.R
import com.sapuseven.untis.data.connectivity.UntisApiConstants
import com.sapuseven.untis.data.connectivity.UntisApiConstants.CAN_READ_LESSON_TOPIC
import com.sapuseven.untis.data.connectivity.UntisApiConstants.CAN_READ_STUDENT_ABSENCE
import com.sapuseven.untis.data.connectivity.UntisApiConstants.CAN_WRITE_LESSON_TOPIC
import com.sapuseven.untis.data.connectivity.UntisApiConstants.CAN_WRITE_STUDENT_ABSENCE
import com.sapuseven.untis.data.connectivity.UntisAuthentication
import com.sapuseven.untis.data.connectivity.UntisRequest
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.data.timetable.PeriodData
import com.sapuseven.untis.data.timetable.TimegridItem
import com.sapuseven.untis.helpers.SerializationUtils
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.models.untis.UntisAttachment
import com.sapuseven.untis.models.untis.UntisError
import com.sapuseven.untis.models.untis.params.PeriodDataParams
import com.sapuseven.untis.models.untis.params.SubmitLessonTopicParams
import com.sapuseven.untis.models.untis.response.BaseResponse
import com.sapuseven.untis.models.untis.response.PeriodDataResponse
import com.sapuseven.untis.models.untis.response.PeriodDataResult
import com.sapuseven.untis.models.untis.response.UntisPeriodData
import com.sapuseven.untis.models.untis.timetable.Period
import com.sapuseven.untis.models.untis.timetable.PeriodElement
import com.sapuseven.untis.ui.common.conditional
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import org.joda.time.format.DateTimeFormat

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun TimetableItemDetailsDialog(
	timegridItem: TimegridItem,
	user: UserDatabase.User,
	timetableDatabaseInterface: TimetableDatabaseInterface,
	onDismiss: (requestedElement: PeriodElement?) -> Unit
) {
	var dismissed by remember { mutableStateOf(false) }
	val scope = rememberCoroutineScope()

	fun dismiss(requestedElement: PeriodElement? = null) {
		onDismiss(requestedElement)
		dismissed = true
	}

	BackHandler(
		enabled = !dismissed,
	) {
		dismiss()
	}

	timegridItem.run {
		Scaffold(
			topBar = {
				CenterAlignedTopAppBar(
					title = { Text(stringResource(id = R.string.all_lesson_details)) },
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
			},
		) { innerPadding ->
			val title = periodData.getLong(
				periodData.subjects,
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

			var attachmentsDialog by remember { mutableStateOf<List<UntisAttachment>?>(null) }
			var lessonTopicEditDialog by remember { mutableStateOf<Int?>(null) }

			var lessonTopicNew by remember { mutableStateOf<String?>(null) }

			Column(
				horizontalAlignment = Alignment.CenterHorizontally,
				modifier = Modifier
					.padding(innerPadding)
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
					style = MaterialTheme.typography.headlineSmall
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

					// Lesson absence check
					if (periodData.element.can.contains(CAN_READ_STUDENT_ABSENCE))
						TimetableItemDetailsDialogWithPeriodData(
							period = periodData,
							canEdit = periodData.element.can.contains(CAN_WRITE_STUDENT_ABSENCE),
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
							onClick = {}
						)

					// Lesson topic
					if (periodData.element.can.contains(CAN_READ_LESSON_TOPIC))
						TimetableItemDetailsDialogWithPeriodData(
							period = periodData,
							canEdit = periodData.element.can.contains(CAN_WRITE_LESSON_TOPIC),
							headlineText = {
								Text(stringResource(id = R.string.all_lessontopic))
							},
							supportingText = {
								val topic = lessonTopicNew ?: it.topic?.text

								Text(
									if (topic.isNullOrBlank())
										if (periodData.element.can.contains(CAN_WRITE_LESSON_TOPIC))
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
					var text by remember { mutableStateOf("") }
					var loading by remember { mutableStateOf(false) }
					var error by remember { mutableStateOf<String?>(null) }

					DynamicHeightAlertDialog(
						title = { Text(stringResource(id = R.string.all_lessontopic_edit)) },
						text = {
							Column(
								modifier = Modifier.fillMaxWidth()
							) {
								OutlinedTextField(
									value = text,
									onValueChange = { text = it },
									isError = error != null,
									enabled = !loading,
									label = { Text(stringResource(id = R.string.all_lessontopic)) },
									modifier = Modifier.fillMaxWidth()
								)

								AnimatedVisibility(visible = error != null) {
									Text(
										modifier = Modifier.padding(
											horizontal = 16.dp,
											vertical = 4.dp
										),
										color = MaterialTheme.colorScheme.error,
										style = MaterialTheme.typography.bodyMedium,
										text = error ?: ""
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
										val query = UntisRequest.UntisRequestQuery(user).apply {
											data.method =
												UntisApiConstants.METHOD_SUBMIT_LESSON_TOPIC
											data.params = listOf(
												SubmitLessonTopicParams(
													text,
													id,
													UntisAuthentication.createAuthObject(user)
												)
											)
										}

										UntisRequest().request(query).fold({
											// TODO: Create corresponding data model
											val untisResponse = SerializationUtils.getJSON()
												.decodeFromString<BaseResponse>(it)

											untisResponse.error?.let { e ->
												error = e.message
												loading = false
											} ?: run {
												lessonTopicNew = text
												lessonTopicEditDialog = null
											}
										}, {
											error = it.message
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimetableDatabaseInterface.TimetableItemDetailsDialogWithPeriodData(
	period: PeriodData,
	canEdit: Boolean = false,
	headlineText: @Composable () -> Unit,
	supportingText: @Composable (UntisPeriodData) -> Unit,
	leadingContent: @Composable (UntisPeriodData?) -> Unit,
	onClick: () -> Unit
) {
	var periodData by remember { mutableStateOf<UntisPeriodData?>(null) }
	var error by remember { mutableStateOf<Throwable?>(null) }
	val errorMessage = error?.message?.let { stringResource(id = R.string.all_error_details, it) }

	val context = LocalContext.current

	LaunchedEffect(Unit) {
		this@TimetableItemDetailsDialogWithPeriodData.user?.let { user ->
			loadPeriodData(
				user = user,
				period = period.element
			).fold({
				periodData = it.dataByTTId[period.element.id.toString()]
			}, {
				error = it
			})
		}
	}

	ListItem(
		headlineText = headlineText,
		supportingText = {
			periodData?.let {
				supportingText(it)
			} ?: error?.let {
				Text(stringResource(R.string.all_error))
			} ?: Text(stringResource(R.string.loading))
		},
		leadingContent = { leadingContent(periodData) },
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

private val TimetableDatabaseInterface.user: UserDatabase.User?
	get() = database.getUser(id)

private suspend fun loadPeriodData(
	user: UserDatabase.User,
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

class UntisApiException(error: UntisError?) : Throwable(error?.message)
