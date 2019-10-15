package com.sapuseven.untis.activities

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.sapuseven.untis.R
import com.sapuseven.untis.adapters.infocenter.*
import com.sapuseven.untis.data.connectivity.UntisApiConstants
import com.sapuseven.untis.data.connectivity.UntisAuthentication
import com.sapuseven.untis.data.connectivity.UntisRequest
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.helpers.SerializationUtils.getJSON
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.models.UntisAbsence
import com.sapuseven.untis.models.untis.UntisDate
import com.sapuseven.untis.models.untis.masterdata.SchoolYear
import com.sapuseven.untis.models.untis.params.AbsenceParams
import com.sapuseven.untis.models.untis.params.ExamParams
import com.sapuseven.untis.models.untis.params.HomeworkParams
import com.sapuseven.untis.models.untis.response.AbsenceResponse
import com.sapuseven.untis.models.untis.response.ExamResponse
import com.sapuseven.untis.models.untis.response.HomeworkResponse
import kotlinx.android.synthetic.main.activity_infocenter.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.joda.time.LocalDate

class InfoCenterActivity : BaseActivity() {
	private val contactList = arrayListOf<ContactAdapterItem>()
	private val eventList = arrayListOf<EventAdapterItem>()
	private val absenceList = arrayListOf<UntisAbsence>()

	private val contactAdapter = ContactAdapter(contactList)
	private val eventAdapter = EventAdapter(this, eventList)
	private val absenceAdapter = AbsenceAdapter(this, absenceList)

	private var api: UntisRequest = UntisRequest()

	private lateinit var userDatabase: UserDatabase

	companion object {
		const val EXTRA_LONG_PROFILE_ID = "com.sapuseven.untis.activities.profileid"
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_infocenter)

		userDatabase = UserDatabase.createInstance(this)
		userDatabase.getUser(intent.getLongExtra(EXTRA_LONG_PROFILE_ID, -1))?.let {
			eventAdapter.timetableDatabaseInterface = TimetableDatabaseInterface(userDatabase, it.id!!)
			loadData(it)
		}

		recyclerview_infocenter?.layoutManager = LinearLayoutManager(this)

		bottomnavigationview_infocenter?.setOnNavigationItemSelectedListener {
			when (it.itemId) {
				R.id.item_infocenter_contact -> {
					recyclerview_infocenter.adapter = contactAdapter
				}
				R.id.item_infocenter_events -> {
					recyclerview_infocenter.adapter = eventAdapter
				}
				R.id.item_infocenter_absences -> {
					recyclerview_infocenter.adapter = absenceAdapter
				}
			}
			true
		}
	}

	private fun loadData(user: UserDatabase.User) = GlobalScope.launch(Dispatchers.Main) {
		loadAbsences(user)?.let {
			absenceList.addAll(it)
			absenceAdapter.notifyDataSetChanged()
		}
		loadEvents(user)?.let {
			eventList.addAll(it)
			eventAdapter.notifyDataSetChanged()
		}
	}

	private suspend fun loadEvents(user: UserDatabase.User): List<EventAdapterItem>? {
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
					auth = UntisAuthentication.getAuthObject(user.user, user.key)
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
					auth = UntisAuthentication.getAuthObject(user.user, user.key)
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
				auth = UntisAuthentication.getAuthObject(user.user, user.key)
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
