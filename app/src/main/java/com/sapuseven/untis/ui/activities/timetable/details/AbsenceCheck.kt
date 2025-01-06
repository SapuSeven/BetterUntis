package com.sapuseven.untis.ui.activities.timetable.details

import android.os.Parcelable
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.sapuseven.untis.R
import com.sapuseven.untis.api.model.untis.Person
import com.sapuseven.untis.api.model.untis.timetable.Period
import com.sapuseven.untis.api.model.untis.timetable.PeriodData
import com.sapuseven.untis.data.repository.TimetableRepository
import com.sapuseven.untis.ui.animations.fullscreenDialogAnimationEnter
import com.sapuseven.untis.ui.animations.fullscreenDialogAnimationExit
import com.sapuseven.untis.ui.common.SmallCircularProgressIndicator
import com.sapuseven.untis.ui.dialogs.TimePickerDialog
import com.sapuseven.untis.ui.functional.insetsPaddingValues
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class AbsenceCheckState(
	var studentData: Set<Person>,
	val timetableRepository: TimetableRepository,
	val scope: CoroutineScope,
	private val onPeriodDataUpdate: (PeriodData) -> Unit
) {
	private var _visible by mutableStateOf(false)
	val visible: Boolean
		get() = _visible

	private var _period: Period? by mutableStateOf(null)

	private var _periodData: PeriodData? by mutableStateOf(null)
	val periodData: PeriodData?
		get() = _periodData

	private var _detailedPerson: Person? by mutableStateOf(null)
	val detailedPerson: Person?
		get() = _detailedPerson

	internal var newAbsenceStartDateTime: LocalDateTime? by mutableStateOf(null)
	internal var newAbsenceEndDateTime: LocalDateTime? by mutableStateOf(null)

	fun show(period: Period, periodData: PeriodData) {
		_period = period
		_periodData = periodData
		_visible = true
	}

	fun hide() {
		hideDetailed()
		_visible = false
	}

	fun showDetailed(student: Person) {
		newAbsenceStartDateTime = _period?.startDateTime
		newAbsenceEndDateTime = _period?.endDateTime
		_detailedPerson = student
	}

	fun hideDetailed() {
		_detailedPerson = null
	}

	private fun updatePeriodData(periodData: PeriodData?) {
		periodData?.let {
			_periodData = it
			onPeriodDataUpdate(it)
		}
	}

	suspend fun createAbsence(
		studentId: Long,
		periodId: Long = _period!!.id,
		startTime: LocalTime = _period!!.startDateTime.toLocalTime(),
		endTime: LocalTime = _period!!.endDateTime.toLocalTime()
	) {
		timetableRepository.postAbsence(periodId, studentId, startTime, endTime).onSuccess { newAbsences ->
			updatePeriodData(_periodData?.let { it.copy(absences = (it.absences ?: emptyList()).plus(newAbsences)) })
		}
	}

	suspend fun deleteAbsence(absenceId: Long) {
		timetableRepository.deleteAbsence(absenceId).onSuccess {
			updatePeriodData(_periodData?.let { it.copy(absences = (it.absences ?: emptyList()).filterNot { it.id == absenceId }) })
		}
	}

	suspend fun submitAbsencesChecked() {
		timetableRepository.postAbsencesChecked(setOf(_period!!.id)).onSuccess {
			updatePeriodData(_periodData?.copy(absenceChecked = true))
		}
	}
}

@Composable
fun rememberAbsenceCheckState(
	studentData: Set<Person>,
	timetableRepository: TimetableRepository,
	scope: CoroutineScope = rememberCoroutineScope(),
	onPeriodDataUpdate: (PeriodData) -> Unit
): AbsenceCheckState {
	// TODO: Add rememberSavable support and use that one instead
	return remember {
		AbsenceCheckState(
			studentData = studentData,
			timetableRepository = timetableRepository,
			scope = scope,
			onPeriodDataUpdate = onPeriodDataUpdate
		)
	}
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun AbsenceCheck(
	state: AbsenceCheckState,
	modifier: Modifier = Modifier
) {
	BackHandler(
		enabled = state.visible,
	) {
		state.hide()
	}

	AnimatedVisibility(
		visible = state.visible,
		enter = fullscreenDialogAnimationEnter(),
		exit = fullscreenDialogAnimationExit()
	) {
		LazyColumn(
			modifier = modifier,
			contentPadding = insetsPaddingValues()
		) {
			val students = state.periodData!!.studentIds?.let { studentIds ->
				studentIds
					.mapNotNull { studentId -> state.studentData.find { it.id == studentId } }
					.sortedBy { it.fullName() }
			} ?: emptyList()

			items(students) { student ->
				var loading by remember { mutableStateOf(false) }
				val existingAbsence = state.periodData!!.absences?.findLast { it.studentId == student.id }

				ListItem(
					headlineContent = {
						Text(text = student.fullName())
					},
					supportingContent = existingAbsence?.let {
						{
							Text(text = it.text)
						}
					},
					leadingContent = {
						if (loading)
							SmallCircularProgressIndicator()
						else if (existingAbsence != null)
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
					modifier = Modifier.combinedClickable(
						onClick = {
							state.scope.launch {
								existingAbsence?.let {
									loading = true
									state.deleteAbsence(it.id)
									loading = false
								} ?: let {
									loading = true
									state.createAbsence(student.id)
									loading = false
								}
							}
						},
						onLongClick = {
							state.showDetailed(student)
						}
					),
					/*trailingContent = {
						IconButton(
							onClick = {
								params?.let {
									detailedAbsenceCheck =
										(it.periodDataId to student) to (it.startDateTime to it.endDateTime)
								}
							}
						) {
							Icon(
								painter = painterResource(id = R.drawable.notification_clock),
								contentDescription = null
							)
						}
					}*/
				)
			}
		}
	}

	AnimatedVisibility(
		visible = state.detailedPerson != null,
		enter = fullscreenDialogAnimationEnter(),
		exit = fullscreenDialogAnimationExit()
	) {
		var studentName by rememberSaveable { mutableStateOf<String?>(null) }
		studentName = state.detailedPerson?.fullName()

		BackHandler(
			enabled = state.detailedPerson != null,
		) {
			state.hideDetailed()
			studentName = null
		}

		Column(
			modifier = modifier
		) {
			val newAbsenceStartTime =
				state.newAbsenceStartDateTime?.toLocalTime() ?: LocalTime.now()
			val newAbsenceEndTime =
				state.newAbsenceEndDateTime?.toLocalTime() ?: LocalTime.now().plusHours(1)

			var showStartTimePicker by remember { mutableStateOf(false) }
			var showEndTimePicker by remember { mutableStateOf(false) }

			ListItem(
				modifier = Modifier.clickable {
					showStartTimePicker = true
				},
				headlineContent = {
					Text(text = "Absence start time") // TODO Extract string resource
				},
				trailingContent = {
					Text(
						text = newAbsenceStartTime.format(DateTimeFormatter.ISO_LOCAL_TIME),
						style = MaterialTheme.typography.labelLarge
					)
				}
			)

			ListItem(
				modifier = Modifier.clickable {
					showEndTimePicker = true
				},
				headlineContent = {
					Text(text = "Absence end time") // TODO Extract string resource
				},
				trailingContent = {
					Text(
						text = newAbsenceEndTime.format(DateTimeFormatter.ISO_LOCAL_TIME),
						style = MaterialTheme.typography.labelLarge
					)
				}
			)

			if (showStartTimePicker) {
				TimePickerDialog(
					initialSelection = newAbsenceStartTime,
					onDismiss = {
						showStartTimePicker = false
					}
				) { time ->
					state.newAbsenceStartDateTime = LocalDateTime.of(
						state.newAbsenceEndDateTime?.toLocalDate() ?: LocalDate.now(),
						time
					)
					showStartTimePicker = false
				}
			}

			if (showEndTimePicker) {
				TimePickerDialog(
					initialSelection = newAbsenceEndTime,
					onDismiss = {
						showEndTimePicker = false
					}
				) { time ->
					state.newAbsenceEndDateTime = LocalDateTime.of(
						state.newAbsenceEndDateTime?.toLocalDate() ?: LocalDate.now(),
						time
					)
					showEndTimePicker = false
				}
			}
		}
	}
}
