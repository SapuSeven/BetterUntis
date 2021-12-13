package com.sapuseven.untis.activities

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sapuseven.untis.R
import com.sapuseven.untis.adapters.infocenter.*
import com.sapuseven.untis.data.connectivity.UntisApiConstants
import com.sapuseven.untis.data.connectivity.UntisApiConstants.RIGHT_ABSENCES
import com.sapuseven.untis.data.connectivity.UntisApiConstants.RIGHT_OFFICEHOURS
import com.sapuseven.untis.data.connectivity.UntisAuthentication
import com.sapuseven.untis.data.connectivity.UntisRequest
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.helpers.SerializationUtils.getJSON
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.models.UntisAbsence
import com.sapuseven.untis.models.UntisMessage
import com.sapuseven.untis.models.UntisOfficeHour
import com.sapuseven.untis.models.untis.UntisDate
import com.sapuseven.untis.models.untis.masterdata.SchoolYear
import com.sapuseven.untis.models.untis.params.*
import com.sapuseven.untis.models.untis.response.*
import kotlinx.android.synthetic.main.activity_infocenter.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import org.joda.time.LocalDate
import java.text.SimpleDateFormat
import java.util.*

class InfoCenterActivity : BaseActivity() {
	private val officeHourList = arrayListOf<UntisOfficeHour>()
	private val eventList = arrayListOf<EventAdapterItem>()
	private val absenceList = arrayListOf<UntisAbsence>()
	private val messageList = arrayListOf<UntisMessage>()

	private val officeHourAdapter = OfficeHourAdapter(this, officeHourList)
	private val eventAdapter = EventAdapter(this, eventList)
	private val absenceAdapter = AbsenceAdapter(this, absenceList)
	private val messageAdapter = MessageAdapter(this, messageList)

	private var officeHoursLoading = true
	private var eventsLoading = true
	private var absencesLoading = true
	private var messagesLoading = true

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
			if (!it.userData.rights.contains(RIGHT_OFFICEHOURS)) bottomnavigationview_infocenter.menu.removeItem(R.id.item_infocenter_officehours)
			if (!it.userData.rights.contains(RIGHT_ABSENCES)) bottomnavigationview_infocenter.menu.removeItem(R.id.item_infocenter_absences)

			if (bottomnavigationview_infocenter.menu.size() <= 1) bottomnavigationview_infocenter.visibility = View.GONE

			eventAdapter.timetableDatabaseInterface = TimetableDatabaseInterface(userDatabase, it.id!!)
			refreshMessages(it)
			if (it.userData.rights.contains(RIGHT_OFFICEHOURS)) refreshOfficeHours(it)
			refreshEvents(it)
			if (it.userData.rights.contains(RIGHT_ABSENCES)) refreshAbsences(it)
		}

		recyclerview_infocenter.layoutManager = LinearLayoutManager(this)

		bottomnavigationview_infocenter.setOnNavigationItemSelectedListener {
			showPage(it)
			true
		}

		showPage(bottomnavigationview_infocenter.menu.getItem(0))
	}

	private fun showPage(item: MenuItem) {
		when (item.itemId) {
			R.id.item_infocenter_messages -> {
				showList(messageAdapter, messagesLoading, if (messageList.isEmpty()) getString(R.string.infocenter_messages_empty) else "") { user ->
					refreshMessages(user)
				}
			}
			R.id.item_infocenter_officehours -> {
				showList(officeHourAdapter, officeHoursLoading, if (officeHourList.isEmpty()) getString(R.string.infocenter_officehours_empty) else "") { user ->
					refreshOfficeHours(user)
				}
			}
			R.id.item_infocenter_events -> {
				showList(eventAdapter, eventsLoading, if (eventList.isEmpty()) getString(R.string.infocenter_events_empty) else "") { user ->
					refreshEvents(user)
				}
			}
			R.id.item_infocenter_absences -> {
				showList(absenceAdapter, absencesLoading, if (absenceList.isEmpty()) getString(R.string.infocenter_absences_empty) else "") { user ->
					refreshAbsences(user)
				}
			}
		}
	}

	private fun showList(adapter: RecyclerView.Adapter<*>, refreshing: Boolean, infoString: String, refreshFunction: (user: UserDatabase.User) -> Unit) {
		recyclerview_infocenter.adapter = adapter
		swiperefreshlayout_infocenter.isRefreshing = refreshing
		swiperefreshlayout_infocenter.setOnRefreshListener { user?.let { refreshFunction(it) } }
		textview_infocenter_emptylist.text = if (refreshing) "" else infoString
	}

	private fun refreshMessages(user: UserDatabase.User) = GlobalScope.launch(Dispatchers.Main) {
		messagesLoading = true
		loadMessages(user)?.let {
			messageList.clear()
			messageList.addAll(it)
			messageAdapter.notifyDataSetChanged()

			preferences.defaultPrefs.edit()
					.putInt("preference_last_messages_count", it.size)
					.putString("preference_last_messages_date", SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Calendar.getInstance().time))
					.apply()
		}
		messagesLoading = false
		if (bottomnavigationview_infocenter.selectedItemId == R.id.item_infocenter_messages) {
			swiperefreshlayout_infocenter.isRefreshing = false
			textview_infocenter_emptylist.text = if (messageList.isEmpty()) getString(R.string.infocenter_messages_empty) else ""
		}
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
			it.exam?.startDateTime?.toString() ?: it.homework?.endDate?.toString()
		}
	}

	private fun getCurrentYear(schoolYears: List<SchoolYear>): SchoolYear? {
		return schoolYears.find {
			val now = LocalDate.now()
			now.isAfter(LocalDate(it.startDate)) && now.isBefore(LocalDate(it.endDate))
		}
	}

	private suspend fun loadMessages(user: UserDatabase.User): List<UntisMessage>? {
		messagesLoading = true

		val query = UntisRequest.UntisRequestQuery(user)

		query.data.method = UntisApiConstants.METHOD_GET_MESSAGES
		query.proxyHost = preferences.defaultPrefs.getString("preference_connectivity_proxy_host", null)
		query.data.params = listOf(MessageParams(
				UntisDate.fromLocalDate(LocalDate.now()),
				auth = UntisAuthentication.createAuthObject(user)
		))

		val result = api.request(query)
		return result.fold({ data ->
			val untisResponse = getJSON().decodeFromString<MessageResponse>(data)

			untisResponse.result?.messages
		}, { null })
	}

	private suspend fun loadOfficeHours(user: UserDatabase.User): List<UntisOfficeHour>? {
		officeHoursLoading = true

		val query = UntisRequest.UntisRequestQuery(user)

		query.data.method = UntisApiConstants.METHOD_GET_OFFICEHOURS
		query.proxyHost = preferences.defaultPrefs.getString("preference_connectivity_proxy_host", null)
		query.data.params = listOf(OfficeHoursParams(
				-1,
				UntisDate.fromLocalDate(LocalDate.now()),
				auth = UntisAuthentication.createAuthObject(user)
		))

		val result = api.request(query)
		return result.fold({ data ->
			val untisResponse = getJSON().decodeFromString<OfficeHoursResponse>(data)

			untisResponse.result?.officeHours
		}, { null })
	}

	private suspend fun loadExams(user: UserDatabase.User): List<EventAdapterItem>? {
		val schoolYears = userDatabase.getAdditionalUserData<SchoolYear>(user.id!!, SchoolYear())?.values?.toList()
				?: emptyList()
		getCurrentYear(schoolYears)?.endDate?.let { currentSchoolYearEndDate ->
			val query = UntisRequest.UntisRequestQuery(user)

			query.data.method = UntisApiConstants.METHOD_GET_EXAMS
			query.proxyHost = preferences.defaultPrefs.getString("preference_connectivity_proxy_host", null)
			query.data.params = listOf(ExamParams(
					user.userData.elemId,
					user.userData.elemType ?: "",
					UntisDate.fromLocalDate(LocalDate.now()),
					UntisDate(currentSchoolYearEndDate),
					auth = UntisAuthentication.createAuthObject(user)
			))

			val result = api.request(query)
			return result.fold({ data ->
				val untisResponse = getJSON().decodeFromString<ExamResponse>(data)

				untisResponse.result?.exams?.map { EventAdapterItem(it, null, null) }
			}, { null })
		}
		return null
	}

	private suspend fun loadHomeworks(user: UserDatabase.User): List<EventAdapterItem>? {
		val schoolYears = userDatabase.getAdditionalUserData<SchoolYear>(user.id!!, SchoolYear())?.values?.toList()
				?: emptyList()
		getCurrentYear(schoolYears)?.endDate?.let { currentSchoolYearEndDate ->
			val query = UntisRequest.UntisRequestQuery(user)

			query.data.method = UntisApiConstants.METHOD_GET_HOMEWORKS
			query.proxyHost = preferences.defaultPrefs.getString("preference_connectivity_proxy_host", null)
			query.data.params = listOf(HomeworkParams(
					user.userData.elemId,
					user.userData.elemType ?: "",
					UntisDate.fromLocalDate(LocalDate.now()),
					UntisDate(currentSchoolYearEndDate),
					auth = UntisAuthentication.createAuthObject(user)
			))

			val result = api.request(query)
			return result.fold({ data ->
				val untisResponse = getJSON().decodeFromString<HomeworkResponse>(data)

				untisResponse.result?.homeWorks?.map { EventAdapterItem(null, it, untisResponse.result.lessonsById) }
			}, { null })
		}
		return null
	}

	private suspend fun loadAbsences(user: UserDatabase.User): List<UntisAbsence>? {
		absencesLoading = true

		val query = UntisRequest.UntisRequestQuery(user)

		query.data.method = UntisApiConstants.METHOD_GET_ABSENCES
		query.proxyHost = preferences.defaultPrefs.getString("preference_connectivity_proxy_host", null)
		query.data.params = listOf(AbsenceParams(
				UntisDate.fromLocalDate(LocalDate.now().minusYears(1)),
				UntisDate.fromLocalDate(LocalDate.now().plusMonths(1)),
				includeExcused = true,
				includeUnExcused = true,
				auth = UntisAuthentication.createAuthObject(user)
		))

		val result = api.request(query)
		return result.fold({ data ->
			val untisResponse = getJSON().decodeFromString<AbsenceResponse>(data)

			untisResponse.result?.absences?.sortedBy { it.excused }
		}, { null })
	}
}
