package com.sapuseven.untis.ui.activities

import android.app.Activity
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import com.sapuseven.untis.activities.BaseComposeActivity
import com.sapuseven.untis.data.connectivity.UntisApiConstants
import com.sapuseven.untis.data.connectivity.UntisAuthentication
import com.sapuseven.untis.data.connectivity.UntisRequest
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.helpers.SerializationUtils
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.models.*
import com.sapuseven.untis.models.untis.UntisDate
import com.sapuseven.untis.models.untis.masterdata.SchoolYear
import com.sapuseven.untis.models.untis.params.*
import com.sapuseven.untis.models.untis.response.*
import com.sapuseven.untis.preferences.DataStorePreferences
import com.sapuseven.untis.ui.activities.InfoCenterState.Companion.ID_MESSAGES
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.joda.time.LocalDate


class InfoCenterState(
	private val userDatabase: UserDatabase,
	private val user: UserDatabase.User,
	private val timetableDatabaseInterface: TimetableDatabaseInterface,
	private val preferences: DataStorePreferences,
	private val contextActivity: Activity,
	var selectedItem: MutableState<Int>,
	var absenceFilterConfiguration: MutableState<AbsenceOrder?>,
	val showAbsenceFilter: MutableState<Boolean>,

	val messagesLoading: MutableState<Boolean>,
	val eventsLoading: MutableState<Boolean>,
	val absencesLoading: MutableState<Boolean>,
	val officeHoursLoading: MutableState<Boolean>,

	val messages: MutableState<List<UntisMessage>?>,
	val events: MutableState<List<EventListItem>?>,
	val absences: MutableState<List<UntisAbsence>?>,
	val officeHours: MutableState<List<UntisOfficeHour>?>,
) {
	private var api: UntisRequest = UntisRequest()

	val shouldShowOfficeHours = user.userData.rights.contains(UntisApiConstants.RIGHT_OFFICEHOURS)
	val shouldShowAbsences = user.userData.rights.contains(UntisApiConstants.RIGHT_ABSENCES)

	companion object {
		const val ID_MESSAGES = 1
		const val ID_EVENTS = 2
		const val ID_ABSENCES = 3
		const val ID_OFFICEHOURS = 4
	}

	suspend fun loadMessages() {
		messagesLoading.value = true

		val query = UntisRequest.UntisRequestQuery(user)

		query.data.method = UntisApiConstants.METHOD_GET_MESSAGES
		query.proxyHost = preferences.proxyHost.getValue()
		query.data.params = listOf(
			MessageParams(
				UntisDate.fromLocalDate(LocalDate.now()),
				auth = UntisAuthentication.createAuthObject(user)
			)
		)

		val result = api.request(query).fold({ data ->
			val untisResponse = SerializationUtils.getJSON().decodeFromString<MessageResponse>(data)

			untisResponse.result?.messages
		}, { null /* TODO: Show error */ })

		messages.value = result
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
		val schoolYears = userDatabase.getAdditionalUserData<SchoolYear>(
			user.id,
			SchoolYear()
		)?.values?.toList()
			?: emptyList()
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
					auth = UntisAuthentication.createAuthObject(user)
				)
			)

			val result = api.request(query)
			return result.fold({ data ->
				val untisResponse = SerializationUtils.getJSON().decodeFromString<ExamResponse>(data)

				untisResponse.result?.exams?.map { EventListItem(timetableDatabaseInterface, exam = it) }
			}, { null /* TODO: Show error */ })
		}
		return null
	}

	private suspend fun loadHomeworks(): List<EventListItem>? {
		val schoolYears = userDatabase.getAdditionalUserData<SchoolYear>(
			user.id,
			SchoolYear()
		)?.values?.toList()
			?: emptyList()
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
					auth = UntisAuthentication.createAuthObject(user)
				)
			)

			val result = api.request(query)
			return result.fold({ data ->
				val untisResponse = SerializationUtils.getJSON().decodeFromString<HomeworkResponse>(data)

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
				auth = UntisAuthentication.createAuthObject(user)
			)
		)

		val result = api.request(query).fold({ data ->
			val untisResponse = SerializationUtils.getJSON().decodeFromString<AbsenceResponse>(data)

			untisResponse.result?.absences?.sortedBy { it.excused }
		}, { null /* TODO: Show error */ })

		absences.value = result
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
				auth = UntisAuthentication.createAuthObject(user)
			)
		)

		val result = api.request(query).fold({ data ->
			val untisResponse = SerializationUtils.getJSON().decodeFromString<OfficeHoursResponse>(data)

			untisResponse.result?.officeHours
		}, { null /* TODO: Show error */ })

		officeHours.value = result
		officeHoursLoading.value = false
	}

	private fun getCurrentYear(schoolYears: List<SchoolYear>): SchoolYear? {
		return schoolYears.find {
			val now = LocalDate.now()
			now.isAfter(LocalDate(it.startDate)) && now.isBefore(LocalDate(it.endDate))
		}
	}

	suspend fun updateAbsenceOrder(absenceOrder: AbsenceOrder) {
		preferences.absencesOrder.saveValue(Json.encodeToString(absenceOrder))
	}

	suspend fun loadAbsenceOrder()  {
		absenceFilterConfiguration.value = Json.decodeFromString(preferences.absencesOrder.getValue())
	}

	fun isItemSelected(itemId: Int): Boolean = selectedItem.value == itemId

	fun selectItem(itemId: Int) { selectedItem.value = itemId }

	fun onItemSelect(itemId: Int): () -> Unit = { selectedItem.value = itemId }

	fun onBackClick() = contextActivity.finish()
}

@Composable
fun rememberInfoCenterState(
	userDatabase: UserDatabase,
	user: UserDatabase.User,
	timetableDatabaseInterface: TimetableDatabaseInterface,
	preferences: DataStorePreferences,
	contextActivity: BaseComposeActivity,
	selectedItem: MutableState<Int> = rememberSaveable { mutableStateOf(ID_MESSAGES) },
	absenceFilterConfiguration: MutableState<AbsenceOrder?> = rememberSaveable(
		saver = Saver(
			save = { Json.encodeToString(it.value) },
			restore = { mutableStateOf(Json.decodeFromString(it)) }
		)
	) {
		mutableStateOf(null)
	},
	showAbsenceFilter: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
	messages: MutableState<List<UntisMessage>?> = remember { mutableStateOf<List<UntisMessage>?>(null) },
	officeHours: MutableState<List<UntisOfficeHour>?> = remember { mutableStateOf<List<UntisOfficeHour>?>(null) },
	events: MutableState<List<EventListItem>?> = remember { mutableStateOf<List<EventListItem>?>(null) },
	absences: MutableState<List<UntisAbsence>?> = remember { mutableStateOf<List<UntisAbsence>?>(null) },
	messagesLoading: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
	eventsLoading: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
	absencesLoading: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
	officeHoursLoading: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
) = remember(user) {
	InfoCenterState(
		userDatabase = userDatabase,
		user = user,
		timetableDatabaseInterface = timetableDatabaseInterface,
		preferences = preferences,
		contextActivity = contextActivity,
		selectedItem = selectedItem,
		absenceFilterConfiguration = absenceFilterConfiguration,
		showAbsenceFilter = showAbsenceFilter,
		messages = messages,
		officeHours = officeHours,
		events = events,
		absences = absences,
		messagesLoading = messagesLoading,
		eventsLoading = eventsLoading,
		absencesLoading = absencesLoading,
		officeHoursLoading = officeHoursLoading,
	)
}

@Serializable
sealed class AbsenceOrder {

	abstract val orderType: OrderType
	abstract val id: Int

	@Serializable
	class LastSevenDays(override val orderType: OrderType, override val id: Int = 1): AbsenceOrder()
	@Serializable
	class LastFourteenDays(override val orderType: OrderType, override val id: Int = 2): AbsenceOrder()
	@Serializable
	class LastThirtyDays(override val orderType: OrderType, override val id: Int = 3): AbsenceOrder()
	@Serializable
	class LastNinetyDays(override val orderType: OrderType, override val id: Int = 4): AbsenceOrder()
	@Serializable
	class CurrentSchoolYear(override val orderType: OrderType, override val id: Int = 5): AbsenceOrder()

	fun copy(orderType: OrderType): AbsenceOrder {
		return when(this) {
			is LastFourteenDays -> LastFourteenDays(orderType)
			is LastNinetyDays -> LastNinetyDays(orderType)
			is LastSevenDays -> LastSevenDays(orderType)
			is LastThirtyDays -> LastThirtyDays(orderType)
			is CurrentSchoolYear -> CurrentSchoolYear(orderType)
		}
	}

	override fun toString(): String {
		return when(this){
			is CurrentSchoolYear -> "Current school year"
			is LastFourteenDays -> "Show last 14 days"
			is LastNinetyDays -> "Show last 90 days"
			is LastSevenDays -> "Show last 7 days"
			is LastThirtyDays -> "Show last 30 days"
		}
	}

}
@Serializable
sealed class OrderType {

	abstract val showOnlyUnexcused: Boolean

	@Serializable
	class Ascending(override val showOnlyUnexcused: Boolean) : OrderType()

	@Serializable
	class Descending(override val showOnlyUnexcused : Boolean) : OrderType()

	fun copy(showOnlyUnexcused: Boolean): OrderType {
		return when(this){
			is Ascending -> Ascending(showOnlyUnexcused)
			is Descending -> Descending(showOnlyUnexcused)
		}
	}
}



class EventListItem private constructor(
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

	constructor(timetableDatabaseInterface: TimetableDatabaseInterface, homework: UntisHomework, lessonsById: Map<String, UntisHomeworkLesson>) : this(
		init = null,
		timetableDatabaseInterface = timetableDatabaseInterface,
		homework = homework,
		lessonsById = lessonsById
	)
}
