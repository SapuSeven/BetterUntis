package com.sapuseven.untis.ui.activities.infocenter

import android.app.Activity
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.sapuseven.untis.activities.BaseComposeActivity
import com.sapuseven.untis.data.database.UserDatabase
import com.sapuseven.untis.data.database.entities.User
import com.sapuseven.untis.models.*
import com.sapuseven.untis.api.model.untis.masterdata.SchoolYear
import com.sapuseven.untis.models.untis.params.*
import com.sapuseven.untis.models.untis.response.*
import com.sapuseven.untis.preferences.DataStorePreferences
import com.sapuseven.untis.ui.activities.infocenter.InfoCenterState.Companion.ID_MESSAGES
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime


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

		suspend fun loadMessages() {
			messagesLoading.value = true

			val query = UntisRequest.UntisRequestQuery(user)

			query.data.method = UntisApiConstants.METHOD_GET_MESSAGES
			query.proxyHost = preferences.proxyHost.getValue()
			query.data.params = listOf(
				MessageParams(
					UntisDate.fromLocalDate(LocalDate.now()),
					auth = Authentication.createAuthObject(user)
				)
			)

			messages.value = api.request<MessageResponse>(query).fold({ untisResponse ->
				untisResponse.result?.messages
			}, { null /* TODO: Show error */ })

			messagesLoading.value = false
		}

		suspend fun loadEvents() {
			eventsLoading.value = true

			val allEvents = mutableListOf<EventListItem>()
			loadExams()?.let { allEvents.addAll(it) }
			loadHomeworks()?.let { allEvents.addAll(it) }

			val result = allEvents.toList().sortedBy {
				it.exam?.startDateTime?.toString() ?: it.homework?.endDate?.toString()
			}

			events.value = result
			eventsLoading.value = false
		}

		private suspend fun loadExams(): List<EventListItem>? {
			val schoolYears =
				userDatabase.userDao().getByIdWithData(user.id)?.schoolYears ?: emptyList()
			getCurrentYear(schoolYears)?.endDate?.let { currentSchoolYearEndDate ->
				val query = UntisRequest.UntisRequestQuery(user)

				query.data.method = UntisApiConstants.METHOD_GET_EXAMS
				query.proxyHost = preferences.proxyHost.getValue()
				query.data.params = listOf(
					ExamParams(
						user.userData.elemId,
						user.userData.elemType ?: "",
						UntisDate.fromLocalDate(LocalDate.now()),
						UntisDate(currentSchoolYearEndDate),
						auth = Authentication.createAuthObject(user)
					)
				)

				return api.request<ExamResponse>(query).fold({ untisResponse ->
					untisResponse.result?.exams?.map {
						EventListItem(
							timetableDatabaseInterface,
							exam = it
						)
					}
				}, { null /* TODO: Show error */ })
			}
			return null
		}

		private suspend fun loadHomeworks(): List<EventListItem>? {
			val schoolYears =
				userDatabase.userDao().getByIdWithData(user.id)?.schoolYears ?: emptyList()
			getCurrentYear(schoolYears)?.endDate?.let { currentSchoolYearEndDate ->
				val query = UntisRequest.UntisRequestQuery(user)

				query.data.method = UntisApiConstants.METHOD_GET_HOMEWORKS
				query.proxyHost = preferences.proxyHost.getValue()
				query.data.params = listOf(
					HomeworkParams(
						user.userData.elemId,
						user.userData.elemType ?: "",
						UntisDate.fromLocalDate(LocalDate.now()),
						UntisDate(currentSchoolYearEndDate),
						auth = Authentication.createAuthObject(user)
					)
				)

				return api.request<HomeworkResponse>(query).fold({ untisResponse ->
					untisResponse.result?.homeWorks?.map {
						EventListItem(
							timetableDatabaseInterface,
							homework = it,
							lessonsById = untisResponse.result.lessonsById
						)
					}
				}, { null /* TODO: Show error */ })
			}
			return null
		}

		suspend fun loadAbsences() {
			if (!shouldShowAbsences) return

			absencesLoading.value = true
			val query = UntisRequest.UntisRequestQuery(user)

			query.data.method = UntisApiConstants.METHOD_GET_ABSENCES
			query.proxyHost = preferences.proxyHost.getValue()
			query.data.params = listOf(
				AbsenceParams(
					UntisDate.fromLocalDate(LocalDate.now().minusYears(1)),
					UntisDate.fromLocalDate(LocalDate.now().plusMonths(1)),
					includeExcused = true,
					includeUnExcused = true,
					auth = Authentication.createAuthObject(user)
				)
			)

			absences.value = api.request<AbsenceResponse>(query).fold({ untisResponse ->
				untisResponse.result?.absences?.sortedBy { it.excused }
			}, { null /* TODO: Show error */ })

			absencesLoading.value = false
		}

		suspend fun loadOfficeHours() {
			if (!shouldShowOfficeHours) return

			officeHoursLoading.value = true

			val query = UntisRequest.UntisRequestQuery(user)

			query.data.method = UntisApiConstants.METHOD_GET_OFFICEHOURS
			query.proxyHost = preferences.proxyHost.getValue()
			query.data.params = listOf(
				OfficeHoursParams(
					-1,
					UntisDate.fromLocalDate(LocalDate.now()),
					auth = Authentication.createAuthObject(user)
				)
			)

			officeHours.value = api.request<OfficeHoursResponse>(query).fold({ untisResponse ->
				untisResponse.result?.officeHours
			}, { null /* TODO: Show error */ })

			officeHoursLoading.value = false
		}

		private fun getCurrentYear(schoolYears: List<SchoolYear>): SchoolYear? {
			return schoolYears.find {
				val now = LocalDate.now()
				now.isAfter(LocalDate(it.startDate)) && now.isBefore(LocalDate(it.endDate))
			}
		}

		fun isItemSelected(itemId: Int): Boolean = selectedItem.value == itemId

		fun selectItem(itemId: Int) {
			selectedItem.value = itemId
		}

		fun onItemSelect(itemId: Int): () -> Unit = { selectedItem.value = itemId }

		fun onBackClick() = contextActivity.finish()*/
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

/*class EventListItem private constructor(
	@Suppress("unused") private val init: Nothing? = null, // Dummy parameter to avoid infinite constructor loops below
	val timetableDatabaseInterface: TimetableDatabaseInterface,
	val exam: UntisExam? = null,
	val homework: UntisHomework? = null,
	val lessonsById: Map<String, UntisHomeworkLesson>? = null
) {
	constructor(timetableDatabaseInterface: TimetableDatabaseInterface, exam: UntisExam) : this(
		init = null,
		timetableDatabaseInterface = timetableDatabaseInterface,
		exam = exam
	)

	constructor(
		timetableDatabaseInterface: TimetableDatabaseInterface,
		homework: UntisHomework,
		lessonsById: Map<String, UntisHomeworkLesson>
	) : this(
		init = null,
		timetableDatabaseInterface = timetableDatabaseInterface,
		homework = homework,
		lessonsById = lessonsById
	)
}*/
