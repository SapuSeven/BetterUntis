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
import com.sapuseven.untis.models.UntisAbsence
import com.sapuseven.untis.models.untis.UntisDate
import com.sapuseven.untis.models.untis.params.AbsenceParams
import com.sapuseven.untis.models.untis.response.AbsenceResponse
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
	private val eventAdapter = EventAdapter(eventList)
	private val absenceAdapter = AbsenceAdapter(this, absenceList)

	private var api: UntisRequest = UntisRequest()

	companion object {
		const val EXTRA_LONG_PROFILE_ID = "com.sapuseven.untis.activities.profileid"
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_infocenter)

		UserDatabase.createInstance(this).getUser(intent.getLongExtra(EXTRA_LONG_PROFILE_ID, -1))?.let {
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

			return untisResponse.result?.absences
		}, { error ->
			return null
		})
	}
}
