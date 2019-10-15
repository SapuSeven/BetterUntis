package com.sapuseven.untis.activities

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sapuseven.untis.R
import com.sapuseven.untis.adapters.infocenter.AbsenceAdapter
import com.sapuseven.untis.adapters.infocenter.ContactAdapter
import com.sapuseven.untis.adapters.infocenter.EventAdapter
import com.sapuseven.untis.adapters.infocenter.EventAdapterItem
import com.sapuseven.untis.data.connectivity.UntisApiConstants
import com.sapuseven.untis.data.connectivity.UntisAuthentication
import com.sapuseven.untis.data.connectivity.UntisRequest
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.helpers.SerializationUtils.getJSON
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.models.UntisAbsence
import com.sapuseven.untis.models.UntisOfficeHour
import com.sapuseven.untis.models.untis.UntisDate
import com.sapuseven.untis.models.untis.masterdata.SchoolYear
import com.sapuseven.untis.models.untis.params.AbsenceParams
import com.sapuseven.untis.models.untis.params.ExamParams
import com.sapuseven.untis.models.untis.params.HomeworkParams
import com.sapuseven.untis.models.untis.params.OfficeHoursParams
import com.sapuseven.untis.models.untis.response.AbsenceResponse
import com.sapuseven.untis.models.untis.response.ExamResponse
import com.sapuseven.untis.models.untis.response.HomeworkResponse
import com.sapuseven.untis.models.untis.response.OfficeHoursResponse
import kotlinx.android.synthetic.main.activity_infocenter.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.joda.time.LocalDate

class InfoCenterActivity : BaseActivity() {
	private val contactList = arrayListOf<UntisOfficeHour>()
	private val eventList = arrayListOf<EventAdapterItem>()
	private val absenceList = arrayListOf<UntisAbsence>()

	private val contactAdapter = ContactAdapter(this, contactList)
	private val eventAdapter = EventAdapter(this, eventList)
	private val absenceAdapter = AbsenceAdapter(this, absenceList)

	private var contactsLoading = true
	private var eventsLoading = true
	private var absencesLoading = true

	private var api: UntisRequest = UntisRequest()

	private lateinit var userDatabase: UserDatabase
	private var user: UserDatabase.User? = null

	companion object {
		const val EXTRA_LONG_PROFILE_ID = "com.sapuseven.untis.activities.profileid"
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_infocenter)

		userDatabase = UserDatabase.createInstance(this)
		user = userDatabase.getUser(intent.getLongExtra(EXTRA_LONG_PROFILE_ID, -1))
		user?.let {
			eventAdapter.timetableDatabaseInterface = TimetableDatabaseInterface(userDatabase, it.id!!)
			refreshOfficeHours(it)
			refreshEvents(it)
			refreshAbsences(it)
		}

		recyclerview_infocenter.layoutManager = LinearLayoutManager(this)

		bottomnavigationview_infocenter.setOnNavigationItemSelectedListener {
			when (it.itemId) {
				R.id.item_infocenter_contact -> {
					showList(contactAdapter, contactsLoading) { user -> GlobalScope.launch(Dispatchers.Main) { refreshOfficeHours(user) } }
				}
				R.id.item_infocenter_events -> {
					showList(eventAdapter, eventsLoading) { user -> GlobalScope.launch(Dispatchers.Main) { refreshEvents(user) } }
				}
				R.id.item_infocenter_absences -> {
					showList(absenceAdapter, absencesLoading) { user -> GlobalScope.launch(Dispatchers.Main) { refreshAbsences(user) } }
				}
			}
			true
		}

		showList(contactAdapter, contactsLoading) { user -> GlobalScope.launch(Dispatchers.Main) { loadOfficeHours(user) } }
	}

	private fun showList(adapter: RecyclerView.Adapter<*>, refreshing: Boolean, refreshFunction: (user: UserDatabase.User) -> Unit) {
		recyclerview_infocenter.adapter = adapter
		swiperefreshlayout_infocenter.isRefreshing = refreshing
		swiperefreshlayout_infocenter.setOnRefreshListener { user?.let { refreshFunction(it) } }
	}

	private fun refreshOfficeHours(user: UserDatabase.User) = GlobalScope.launch(Dispatchers.Main) {
		contactsLoading = true
		loadOfficeHours(user)?.let {
			contactsLoading = false
			if (bottomnavigationview_infocenter.selectedItemId == R.id.item_infocenter_contact)
				swiperefreshlayout_infocenter.isRefreshing = false
			contactList.clear()
			contactList.addAll(it)
			contactAdapter.notifyDataSetChanged()
		}
	}

	private fun refreshEvents(user: UserDatabase.User) = GlobalScope.launch(Dispatchers.Main) {
		eventsLoading = true
		loadEvents(user)?.let {
			eventsLoading = false
			if (bottomnavigationview_infocenter.selectedItemId == R.id.item_infocenter_events)
				swiperefreshlayout_infocenter.isRefreshing = false
			eventList.clear()
			eventList.addAll(it)
			eventAdapter.notifyDataSetChanged()
		}
	}

	private fun refreshAbsences(user: UserDatabase.User) = GlobalScope.launch(Dispatchers.Main) {
		absencesLoading = true
		loadAbsences(user)?.let {
			absencesLoading = false
			if (bottomnavigationview_infocenter.selectedItemId == R.id.item_infocenter_absences)
				swiperefreshlayout_infocenter.isRefreshing = false
			absenceList.clear()
			absenceList.addAll(it)
			absenceAdapter.notifyDataSetChanged()
		}
	}

	private suspend fun loadEvents(user: UserDatabase.User): List<EventAdapterItem>? {
		eventsLoading = true

		val events = mutableListOf<EventAdapterItem>()
		loadExams(user)?.let { events.addAll(it) }
		loadHomeworks(user)?.let { events.addAll(it) }
		return events.toList().sortedBy {
			it.exam?.startDateTime ?: it.homework?.endDate
		}
	}

	private fun getCurrentYear(schoolYears: List<SchoolYear>): SchoolYear? {
		return schoolYears.find {
			val now = LocalDate.now()
			now.isAfter(LocalDate(it.startDate)) && now.isBefore(LocalDate(it.endDate))
		}
	}

	private suspend fun loadOfficeHours(user: UserDatabase.User): List<UntisOfficeHour>? {
		contactsLoading = true

		val query = UntisRequest.UntisRequestQuery()

		query.data.method = UntisApiConstants.METHOD_GET_OFFICEHOURS
		query.url = user.apiUrl
				?: (UntisApiConstants.DEFAULT_WEBUNTIS_PROTOCOL + UntisApiConstants.DEFAULT_WEBUNTIS_HOST + UntisApiConstants.DEFAULT_WEBUNTIS_PATH + user.schoolId)
		query.proxyHost = preferences.defaultPrefs.getString("preference_connectivity_proxy_host", null)
		query.data.params = listOf(OfficeHoursParams(
				-1,
				UntisDate.fromLocalDate(LocalDate.now()),
				auth = UntisAuthentication.getAuthObject(user)
		))

		val result = api.request(query)
		result.fold({ data ->
			val untisResponse = getJSON().parse(OfficeHoursResponse.serializer(), data)

			return untisResponse.result?.officeHours
		}, { error ->
			return null
		})
	}

	private suspend fun loadExams(user: UserDatabase.User): List<EventAdapterItem>? {
		val schoolYears = userDatabase.getAdditionalUserData<SchoolYear>(user.id!!, SchoolYear())?.values?.toList()
				?: emptyList()
		getCurrentYear(schoolYears)?.endDate?.let { currentSchoolYearEndDate ->
			val query = UntisRequest.UntisRequestQuery()

			query.data.method = UntisApiConstants.METHOD_GET_EXAMS
			query.url = user.apiUrl
					?: (UntisApiConstants.DEFAULT_WEBUNTIS_PROTOCOL + UntisApiConstants.DEFAULT_WEBUNTIS_HOST + UntisApiConstants.DEFAULT_WEBUNTIS_PATH + user.schoolId)
			query.proxyHost = preferences.defaultPrefs.getString("preference_connectivity_proxy_host", null)
			query.data.params = listOf(ExamParams(
					user.userData.elemId,
					user.userData.elemType ?: "",
					UntisDate.fromLocalDate(LocalDate.now()),
					UntisDate(currentSchoolYearEndDate),
					auth = UntisAuthentication.getAuthObject(user)
			))

			val result = api.request(query)
			result.fold({ data ->
				val untisResponse = getJSON().parse(ExamResponse.serializer(), data)

				return untisResponse.result?.exams?.map { EventAdapterItem(it, null, null) }
			}, { error ->
				return null
			})
		}
		return null
	}

	private suspend fun loadHomeworks(user: UserDatabase.User): List<EventAdapterItem>? {
		val schoolYears = userDatabase.getAdditionalUserData<SchoolYear>(user.id!!, SchoolYear())?.values?.toList()
				?: emptyList()
		getCurrentYear(schoolYears)?.endDate?.let { currentSchoolYearEndDate ->
			val query = UntisRequest.UntisRequestQuery()

			query.data.method = UntisApiConstants.METHOD_GET_HOMEWORKS
			query.url = user.apiUrl
					?: (UntisApiConstants.DEFAULT_WEBUNTIS_PROTOCOL + UntisApiConstants.DEFAULT_WEBUNTIS_HOST + UntisApiConstants.DEFAULT_WEBUNTIS_PATH + user.schoolId)
			query.proxyHost = preferences.defaultPrefs.getString("preference_connectivity_proxy_host", null)
			query.data.params = listOf(HomeworkParams(
					user.userData.elemId,
					user.userData.elemType ?: "",
					UntisDate.fromLocalDate(LocalDate.now()),
					UntisDate(currentSchoolYearEndDate),
					auth = UntisAuthentication.getAuthObject(user)
			))

			val result = api.request(query)
			result.fold({ data ->
				val untisResponse = getJSON().parse(HomeworkResponse.serializer(), data)

				return untisResponse.result?.homeWorks?.map { EventAdapterItem(null, it, untisResponse.result.lessonsById) }
			}, { error ->
				return null
			})
		}
		return null
	}

	private suspend fun loadAbsences(user: UserDatabase.User): List<UntisAbsence>? {
		absencesLoading = true

		val query = UntisRequest.UntisRequestQuery()

		query.data.method = UntisApiConstants.METHOD_GET_ABSENCES
		query.url = user.apiUrl
				?: (UntisApiConstants.DEFAULT_WEBUNTIS_PROTOCOL + UntisApiConstants.DEFAULT_WEBUNTIS_HOST + UntisApiConstants.DEFAULT_WEBUNTIS_PATH + user.schoolId)
		query.proxyHost = preferences.defaultPrefs.getString("preference_connectivity_proxy_host", null)
		query.data.params = listOf(AbsenceParams(
				UntisDate.fromLocalDate(LocalDate.now().minusYears(1)),
				UntisDate.fromLocalDate(LocalDate.now().plusMonths(1)),
				includeExcused = true,
				includeUnExcused = true,
				auth = UntisAuthentication.getAuthObject(user)
		))

		val result = api.request(query)
		result.fold({ data ->
			val untisResponse = getJSON().parse(AbsenceResponse.serializer(), data)

			return untisResponse.result?.absences?.sortedBy {
				it.excused
			}
		}, { error ->
			return null
		})
	}
}
