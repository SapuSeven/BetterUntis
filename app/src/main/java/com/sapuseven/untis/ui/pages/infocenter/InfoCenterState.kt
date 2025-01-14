package com.sapuseven.untis.ui.pages.infocenter

import android.app.Activity
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.sapuseven.untis.activities.BaseComposeActivity
import com.sapuseven.untis.data.database.UserDatabase
import com.sapuseven.untis.data.database.entities.User
import com.sapuseven.untis.preferences.DataStorePreferences
import com.sapuseven.untis.ui.pages.infocenter.InfoCenterState.Companion.ID_MESSAGES


class InfoCenterState(
	private val userDatabase: UserDatabase,
	private val user: User,
	//private val timetableDatabaseInterface: TimetableDatabaseInterface,
	private val contextActivity: Activity,
	val preferences: DataStorePreferences,
	var selectedItem: MutableState<Int>,
	val showAbsenceFilter: MutableState<Boolean>,

	val messagesLoading: MutableState<Boolean>,
	val eventsLoading: MutableState<Boolean>,
	val absencesLoading: MutableState<Boolean>,
	val officeHoursLoading: MutableState<Boolean>,

	//val messages: MutableState<List<UntisMessage>?>,
	//val events: MutableState<List<EventListItem>?>,
	//val absences: MutableState<List<UntisAbsence>?>,
	//val officeHours: MutableState<List<UntisOfficeHour>?>,

	val absencesOnlyUnexcused: State<Boolean>,
	val absencesSortReversed: State<Boolean>,
	val absencesTimeRange: State<String>,
) {
	companion object {
		const val ID_MESSAGES = 1
		const val ID_EVENTS = 2
		const val ID_ABSENCES = 3
		const val ID_OFFICEHOURS = 4
	}

	/*private var api: UntisRequest = UntisRequest()

		val shouldShowOfficeHours = user.userData.rights.contains(UntisApiConstants.RIGHT_OFFICEHOURS)
		val shouldShowAbsences = user.userData.rights.contains(UntisApiConstants.RIGHT_ABSENCES)

		val messageList: List<UntisMessage>?
			get() = messages.value

		val eventList: List<EventListItem>?
			get() = events.value

		val officeHourList: List<UntisOfficeHour>?
			get() = officeHours.value

		val absenceList: List<UntisAbsence>?
			get() = absences.value.let {
				if (absencesSortReversed.value) {
					it?.sortedBy { absence -> absence.startDateTime.toLocalDateTime() } // oldest first
				} else {
					it?.sortedByDescending { absence -> absence.startDateTime.toLocalDateTime() } // newest first
				}
			}.let {
				it?.filter { absence ->
					(absencesOnlyUnexcused.value != absence.excused) || !absence.excused
				}
			}.let {
				when (absencesTimeRange.value) {
					"seven_days" -> 7
					"fourteen_days" -> 14
					"thirty_days" -> 30
					"ninety_days" -> 90
					else -> null
				}?.let { days ->
					it?.filter { absence ->
						LocalDateTime.now().minusDays(days)
							.isBefore(absence.startDateTime.toLocalDateTime())
					}
				} ?: it
			}
*/
}

@Composable
fun rememberInfoCenterState(
	userDatabase: UserDatabase,
	user: User,
	//timetableDatabaseInterface: TimetableDatabaseInterface,
	preferences: DataStorePreferences,
	contextActivity: BaseComposeActivity,
	selectedItem: MutableState<Int> = rememberSaveable { mutableStateOf(ID_MESSAGES) },
	showAbsenceFilter: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
	/*messages: MutableState<List<UntisMessage>?> = remember {
		mutableStateOf<List<UntisMessage>?>(
			null
		)
	},
	officeHours: MutableState<List<UntisOfficeHour>?> = remember {
		mutableStateOf<List<UntisOfficeHour>?>(
			null
		)
	},
	events: MutableState<List<EventListItem>?> = remember {
		mutableStateOf<List<EventListItem>?>(
			null
		)
	},
	absences: MutableState<List<UntisAbsence>?> = remember {
		mutableStateOf<List<UntisAbsence>?>(
			null
		)
	},*/
	messagesLoading: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
	eventsLoading: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
	absencesLoading: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
	officeHoursLoading: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
	absencesOnlyUnexcused: State<Boolean> = preferences.infocenterAbsencesOnlyUnexcused.getState(),
	absencesSortReversed: State<Boolean> = preferences.infocenterAbsencesSortReverse.getState(),
	absencesTimeRange: State<String> = preferences.infocenterAbsencesTimeRange.getState(),
) = remember(user) {
	InfoCenterState(
		userDatabase = userDatabase,
		user = user,
		//timetableDatabaseInterface = timetableDatabaseInterface,
		preferences = preferences,
		contextActivity = contextActivity,
		selectedItem = selectedItem,
		showAbsenceFilter = showAbsenceFilter,
		//messages = messages,
		//officeHours = officeHours,
		//events = events,
		//absences = absences,
		messagesLoading = messagesLoading,
		eventsLoading = eventsLoading,
		absencesLoading = absencesLoading,
		officeHoursLoading = officeHoursLoading,
		absencesOnlyUnexcused = absencesOnlyUnexcused,
		absencesSortReversed = absencesSortReversed,
		absencesTimeRange = absencesTimeRange
	)
}
