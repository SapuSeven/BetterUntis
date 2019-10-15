package com.sapuseven.untis.activities

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sapuseven.untis.R
import com.sapuseven.untis.adapters.infocenter.AbsenceAdapter
import com.sapuseven.untis.adapters.infocenter.EventAdapter
import com.sapuseven.untis.adapters.infocenter.EventAdapterItem
import com.sapuseven.untis.adapters.infocenter.OfficeHourAdapter
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
	private val officeHourList = arrayListOf<UntisOfficeHour>()
	private val eventList = arrayListOf<EventAdapterItem>()
	private val absenceList = arrayListOf<UntisAbsence>()

	private val officeHourAdapter = OfficeHourAdapter(this, officeHourList)
	private val eventAdapter = EventAdapter(this, eventList)
	private val absenceAdapter = AbsenceAdapter(this, absenceList)

	private var officeHoursLoading = true
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
				R.id.item_infocenter_officehours -> {
					showList(officeHourAdapter, officeHoursLoading, if (officeHourList.isEmpty()) getString(R.string.infocenter_officehours_empty) else "") { user ->
						GlobalScope.launch(Dispatchers.Main) { refreshOfficeHours(user) }
					}
				}
				R.id.item_infocenter_events -> {
					showList(eventAdapter, eventsLoading, if (eventList.isEmpty()) getString(R.string.infocenter_events_empty) else "") { user ->
						GlobalScope.launch(Dispatchers.Main) { refreshEvents(user) }
					}
				}
				R.id.item_infocenter_absences -> {
					showList(absenceAdapter, absencesLoading, if (absenceList.isEmpty()) getString(R.string.infocenter_absences_empty) else "") { user ->
						GlobalScope.launch(Dispatchers.Main) { refreshAbsences(user) }
					}
				}
			}
			true
		}

		showList(officeHourAdapter, officeHoursLoading, if (officeHourList.isEmpty()) getString(R.string.infocenter_officehours_empty) else "") { user ->
			GlobalScope.launch(Dispatchers.Main) { loadOfficeHours(user) }
		}
	}

	private fun showList(adapter: RecyclerView.Adapter<*>, refreshing: Boolean, infoString: String, refreshFunction: (user: UserDatabase.User) -> Unit) {
		recyclerview_infocenter.adapter = adapter
		swiperefreshlayout_infocenter.isRefreshing = refreshing
		swiperefreshlayout_infocenter.setOnRefreshListener { user?.let { refreshFunction(it) } }
		textview_infocenter_emptylist.text = if (refreshing) "" else infoString
	}

	private fun refreshOfficeHours(user: UserDatabase.User) = GlobalScope.launch(Dispatchers.Main) {
		officeHoursLoading = true
		loadOfficeHours(user)?.let {
			officeHourList.clear()
			officeHourList.addAll(it)
			officeHourAdapter.notifyDataSetChanged()
		}
		officeHoursLoading = false
		if (bottomnavigationview_infocenter.selectedItemId == R.id.item_infocenter_officehours) {
			swiperefreshlayout_infocenter.isRefreshing = false
			textview_infocenter_emptylist.text = if (officeHourList.isEmpty()) getString(R.string.infocenter_officehours_empty) else ""
		}
	}

	private fun refreshEvents(user: UserDatabase.User) = GlobalScope.launch(Dispatchers.Main) {
		eventsLoading = true
		loadEvents(user)?.let {
			eventList.clear()
			eventList.addAll(it)
			eventAdapter.notifyDataSetChanged()
		}
		eventsLoading = false
		if (bottomnavigationview_infocenter.selectedItemId == R.id.item_infocenter_events) {
			swiperefreshlayout_infocenter.isRefreshing = false
			textview_infocenter_emptylist.text = if (eventList.isEmpty()) getString(R.string.infocenter_events_empty) else ""
		}
	}

	private fun refreshAbsences(user: UserDatabase.User) = GlobalScope.launch(Dispatchers.Main) {
		absencesLoading = true
		loadAbsences(user)?.let {
			absenceList.clear()
			absenceList.addAll(it)
			absenceAdapter.notifyDataSetChanged()
		}
		absencesLoading = false
		if (bottomnavigationview_infocenter.selectedItemId == R.id.item_infocenter_absences) {
			swiperefreshlayout_infocenter.isRefreshing = false
			textview_infocenter_emptylist.text = if (absenceList.isEmpty()) getString(R.string.infocenter_absences_empty) else ""
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
		officeHoursLoading = true

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
		return result.fold({ data ->
			val untisResponse = getJSON().parse(OfficeHoursResponse.serializer(), data)

			untisResponse.result?.officeHours
		}, { null })
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
			return result.fold({ data ->
				val untisResponse = getJSON().parse(ExamResponse.serializer(), data)

				untisResponse.result?.exams?.map { EventAdapterItem(it, null, null) }
			}, { null })
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
			return result.fold({ data ->
				val untisResponse = getJSON().parse(HomeworkResponse.serializer(), data)

				untisResponse.result?.homeWorks?.map { EventAdapterItem(null, it, untisResponse.result.lessonsById) }
			}, { null })
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
		return result.fold({ data ->
			val untisResponse = getJSON().parse(AbsenceResponse.serializer(), data)

			untisResponse.result?.absences?.sortedBy { it.excused }
		}, { null })
	}
}
