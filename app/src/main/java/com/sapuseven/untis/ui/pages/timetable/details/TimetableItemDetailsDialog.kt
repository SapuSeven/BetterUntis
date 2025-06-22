package com.sapuseven.untis.ui.pages.timetable.details

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.sapuseven.untis.BuildConfig
import com.sapuseven.untis.R
import com.sapuseven.untis.api.model.untis.Attachment
import com.sapuseven.untis.api.model.untis.Person
import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import com.sapuseven.untis.api.model.untis.enumeration.PeriodRight
import com.sapuseven.untis.api.model.untis.timetable.PeriodData
import com.sapuseven.untis.api.model.untis.timetable.PeriodElement
import com.sapuseven.untis.data.repository.MasterDataRepository
import com.sapuseven.untis.data.repository.TimetableRepository
import com.sapuseven.untis.models.PeriodItem
import com.sapuseven.untis.ui.common.AppScaffold
import com.sapuseven.untis.ui.common.ClickableUrlText
import com.sapuseven.untis.ui.common.DebugTimetableItemDetailsAction
import com.sapuseven.untis.ui.common.HorizontalPagerIndicator
import com.sapuseven.untis.ui.common.SmallCircularProgressIndicator
import com.sapuseven.untis.ui.dialogs.AttachmentsDialog
import com.sapuseven.untis.ui.dialogs.DynamicHeightAlertDialog
import com.sapuseven.untis.ui.functional.StringResourceDescriptor
import com.sapuseven.untis.ui.functional.bottomInsets
import crocodile8.universal_cache.FromCache
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableItemDetailsDialog(
	periodItems: List<PeriodItem>,
	timetableRepository: TimetableRepository,
	masterDataRepository: MasterDataRepository,
	initialPage: Int = 0,
	onDismiss: (requestedElement: PeriodElement?) -> Unit
) {
	var dismissed by rememberSaveable { mutableStateOf(false) }
	val pagerState = rememberPagerState(initialPage) { periodItems.size }
	val scope = rememberCoroutineScope()
	val error by remember { mutableStateOf<Throwable?>(null) }

	// contains additional details for the period items. Key is periodId (ttId)
	val periodDataMap = remember { mutableStateMapOf<Long, PeriodData?>() }

	// contains a set of all students referenced in the periodData
	var studentData by remember { mutableStateOf<Set<Person>?>(null) }

	val absenceCheckState = rememberAbsenceCheckState(emptySet(), timetableRepository) {
		periodDataMap[it.ttId] = it
	}

	fun dismiss(requestedElement: PeriodElement? = null) {
		onDismiss(requestedElement)
		dismissed = true
	}

	BackHandler(
		enabled = !dismissed,
	) {
		dismiss()
	}

	LaunchedEffect(Unit) {
		val periods = periodItems.map { it.originalPeriod }.toSet()
		Log.d("TimetableItemDetailsDlg", "Fetching period data for ${periods.map { it.id }}")
		timetableRepository.periodDataSource()
			.get(periods, FromCache.IF_FAILED, additionalKey = masterDataRepository.user)
			.catch {
				//error = it
			}
			.collect {
				it.dataByTTId.forEach { (id, periodData) ->
					periodDataMap[id] = periodData
				}
				studentData = it.referencedStudents.toSet()
				absenceCheckState.studentData = studentData ?: emptySet()
			}
	}

	AppScaffold(
		topBar = {
			CenterAlignedTopAppBar(
				title = { Text(absenceCheckState.detailedPerson?.fullName() ?: stringResource(id = R.string.all_lesson_details)) },
				navigationIcon = {
					IconButton(onClick = {
						if (absenceCheckState.visible)
							absenceCheckState.hide()
						else
							dismiss()
					}) {
						Icon(
							imageVector = Icons.Outlined.Close,
							contentDescription = stringResource(id = R.string.all_close)
						)
					}
				},
				actions = {
					if (BuildConfig.DEBUG)
						DebugTimetableItemDetailsAction(periodItems, periodDataMap)
				}
			)
		},
		floatingActionButton = {
			var loading by rememberSaveable { mutableStateOf(false) }

			AnimatedVisibility(
				visible = absenceCheckState.visible,
				enter = scaleIn(),
				exit = scaleOut()
			) {
				FloatingActionButton(
					modifier = Modifier.bottomInsets(),
					containerColor = MaterialTheme.colorScheme.primary,
					contentColor = MaterialTheme.colorScheme.onPrimary,
					onClick = {
						scope.launch {
							loading = true
							absenceCheckState.detailedPerson?.let {
								absenceCheckState.createAbsence(it.id)
								absenceCheckState.hideDetailed()
							} ?: run {
								absenceCheckState.submitAbsencesChecked()
								absenceCheckState.hide()
							}
							loading = false
						}
					}
				) {
					if (loading)
						SmallCircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
					else if (absenceCheckState.detailedPerson != null)
						Icon(
							painter = painterResource(id = R.drawable.all_check),
							contentDescription = stringResource(R.string.all_dialog_absences_save_detailed)
						)
					else
						Icon(
							painter = painterResource(id = R.drawable.all_save),
							contentDescription = stringResource(R.string.all_dialog_absences_save)
						)
				}
			}
		}
	) { innerPadding ->
		Box(
			modifier = Modifier
				.padding(innerPadding)
		) {
			Column(
				horizontalAlignment = Alignment.CenterHorizontally,
				modifier = Modifier
					.fillMaxSize()
			) {
				HorizontalPager(
					state = pagerState,
					modifier = Modifier
						.weight(1f)
				) { page ->
					periodItems[page].also { periodItem ->
						TimetableItemDetailsDialogPage(
							masterDataRepository,
							timetableRepository,
							periodItem,
							periodDataMap[periodItem.originalPeriod.id],
							error = error,
							onElementClick = { dismiss(it) },
							onAbsenceCheck = { periodData ->
								absenceCheckState.show(periodItem.originalPeriod, periodData)
							}
						)
					}
				}

				if (periodItems.size > 1)
					HorizontalPagerIndicator(
						pagerState = pagerState,
						activeColor = MaterialTheme.colorScheme.primary,
						inactiveColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
						modifier = Modifier
							.align(Alignment.CenterHorizontally)
							.padding(16.dp)
							.bottomInsets()
					)
			}

			AbsenceCheck(
				state = absenceCheckState,
				modifier = Modifier
					.fillMaxSize()
					.background(MaterialTheme.colorScheme.surface)
			)
		}
	}
}

@Composable
private fun TimetableItemDetailsDialogPage(
	masterDataRepository: MasterDataRepository,
	timetableRepository: TimetableRepository,
	periodItem: PeriodItem,
	periodData: PeriodData?,
	error: Throwable? = null,
	onElementClick: (element: PeriodElement) -> Unit,
	onAbsenceCheck: (periodData: PeriodData) -> Unit
) {
	val context = LocalContext.current
	val uriHandler = LocalUriHandler.current
	val scope = rememberCoroutineScope()
	val title = periodItem.getLong(ElementType.SUBJECT).let { title ->
		if (periodItem.isCancelled())
			stringResource(R.string.all_lesson_cancelled, title)
		else if (periodItem.isIrregular())
			stringResource(R.string.all_lesson_irregular, title)
		else if (periodItem.isExam())
			stringResource(R.string.all_lesson_exam, title)
		else
			title
	}

	val errorMessage = error?.message?.let { stringResource(id = R.string.all_error_details, it) }

	val time = stringResource(
		R.string.main_dialog_itemdetails_timeformat,
		periodItem.originalPeriod.startDateTime.format(
			DateTimeFormatter.ofLocalizedTime(
				FormatStyle.SHORT
			)
		),
		periodItem.originalPeriod.endDateTime.format(
			DateTimeFormatter.ofLocalizedTime(
				FormatStyle.SHORT
			)
		)
	)

	var errorDialog by rememberSaveable { mutableStateOf<String?>(null) }

	var attachmentsDialog by rememberSaveable {
		mutableStateOf<List<Attachment>?>(
			null
		)
	}

	var lessonTopicEditDialog by rememberSaveable { mutableStateOf<Long?>(null) }

	var lessonTopicNew by rememberSaveable { mutableStateOf<String?>(periodData?.topic?.text) }

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.fillMaxSize()
			.verticalScroll(rememberScrollState())
			.bottomInsets()
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

		HorizontalDivider(
			color = MaterialTheme.colorScheme.outline,
			modifier = Modifier
				.padding(top = 24.dp, bottom = 12.dp)
				.padding(horizontal = 16.dp)
		)

		masterDataRepository.run {
			// Lesson teachers
			TimetableItemDetailsDialogElement(
				elements = periodItem.teachers,
				onElementClick = { onElementClick(it) },
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
				elements = periodItem.classes,
				onElementClick = { onElementClick(it) },
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
				elements = periodItem.rooms,
				onElementClick = { onElementClick(it) },
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
				periodItem.originalPeriod.text.lesson,
				periodItem.originalPeriod.text.substitution,
				periodItem.originalPeriod.text.info
			).forEach {
				if (it.isNotBlank())
					SelectionContainer {
						ListItem(
							headlineContent = { Text(it) },
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
			}

			// Lesson homeworks
			periodItem.originalPeriod.homeWorks?.forEach { homeWork ->
				val endDate = homeWork.endDate

				ListItem(
					headlineContent = {
						ClickableUrlText(homeWork.text) {
							uriHandler.openUri(it)
						}
					},
					supportingContent = {
						Text(
							stringResource(
								id = R.string.homeworks_due_time,
								endDate.format(
									DateTimeFormatter.ofPattern(
										stringResource(R.string.homeworks_due_time_format)
									)
								)
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
					trailingContent = if (homeWork.attachments.isNotEmpty()) {
						{
							IconButton(onClick = {
								attachmentsDialog = homeWork.attachments
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
			periodItem.originalPeriod.exam?.also {
				ListItem(
					headlineContent = {
						Text(it.name ?: stringResource(id = R.string.all_exam))
					},
					supportingContent = it.text?.let { { Text(it) } },
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
			if (periodItem.originalPeriod.isOnlinePeriod == true) {
				ListItem(
					headlineContent = { Text(stringResource(R.string.all_lesson_online)) },
					leadingContent = {
						Icon(
							painter = painterResource(id = R.drawable.all_lesson_online),
							contentDescription = stringResource(id = R.string.all_lesson_info),
							tint = MaterialTheme.colorScheme.onSurface,
							modifier = Modifier.padding(horizontal = 8.dp)
						)
					},
					trailingContent = periodItem.originalPeriod.onlinePeriodLink?.let {
						{
							IconButton(onClick = {
								uriHandler.openUri(it)
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
			if (periodItem.originalPeriod.can(PeriodRight.READ_STUD_ABSENCE))
				ListItemWithPeriodData(
					periodData = periodData,
					error = error,
					headlineContent = {
						Text(stringResource(id = R.string.all_absences))
					},
					supportingContent = {
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
						errorMessage?.let {
							errorDialog = it
						} ?: periodData?.let {
							if (periodItem.originalPeriod.can(PeriodRight.WRITE_STUD_ABSENCE)) {
								onAbsenceCheck(it)
							}
						}
					}
				)

			// Lesson topic
			if (periodItem.originalPeriod.can(PeriodRight.READ_LESSONTOPIC))
				ListItemWithPeriodData(
					periodData = periodData,
					error = error,
					headlineContent = {
						Text(stringResource(id = R.string.all_lessontopic))
					},
					supportingContent = {
						val topic = lessonTopicNew ?: it.topic?.text

						Text(
							if (topic.isNullOrBlank())
								if (periodItem.originalPeriod.can(PeriodRight.WRITE_LESSONTOPIC)
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
						errorMessage?.let {
							errorDialog = it
						} ?: run {
							if (periodItem.originalPeriod.can(PeriodRight.WRITE_LESSONTOPIC)) {
								lessonTopicEditDialog = periodItem.originalPeriod.id
							}
						}
					}
				)
		}

		errorDialog?.let { error ->
			AlertDialog(
				onDismissRequest = { errorDialog = null },
				title = { Text(stringResource(id = R.string.all_error)) },
				text = { Text(error) },
				confirmButton = {
					TextButton(
						onClick = { errorDialog = null }
					) {
						Text(stringResource(id = R.string.all_ok))
					}
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
			var text by rememberSaveable { mutableStateOf(periodData?.topic?.text ?: lessonTopicNew ?: "") }
			var loading by rememberSaveable { mutableStateOf(false) }
			var dialogError by rememberSaveable { mutableStateOf<StringResourceDescriptor?>(null) }

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
								text = dialogError?.stringResource() ?: ""
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
								timetableRepository.postLessonTopic(id, text)
									.onSuccess {
										if (it) {
											lessonTopicNew = text
											lessonTopicEditDialog = null
										} else {
											dialogError = StringResourceDescriptor(R.string.errormessagedictionary_generic)
											loading = false
										}
									}
									.onFailure {
										dialogError = StringResourceDescriptor(R.string.all_api_error_generic, it.message ?: "null")
										loading = false
									}
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

@Composable
private fun ListItemWithPeriodData(
	periodData: PeriodData?,
	error: Throwable?,
	headlineContent: @Composable () -> Unit,
	supportingContent: @Composable (PeriodData) -> Unit,
	leadingContent: @Composable (PeriodData?) -> Unit,
	onClick: () -> Unit
) {
	ListItem(
		headlineContent = headlineContent,
		supportingContent = {
			periodData?.let {
				supportingContent(it)
			} ?: error?.let {
				Text(stringResource(R.string.all_error))
			} ?: Text(stringResource(R.string.loading))
		},
		leadingContent = { leadingContent(periodData) },
		modifier = Modifier
			.clickable {
				onClick()
			}
	)
}

@Composable
private fun MasterDataRepository.TimetableItemDetailsDialogElement(
	elements: Set<PeriodElement>,
	icon: (@Composable () -> Unit)? = null,
	useLongName: Boolean = false,
	onElementClick: (element: PeriodElement) -> Unit
) {
	if (elements.isNotEmpty())
		ListItem(
			headlineContent = {
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
