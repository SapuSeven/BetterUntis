package com.sapuseven.untis.activities

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.sapuseven.untis.R
import com.sapuseven.untis.adapters.infocenter.*
import kotlinx.android.synthetic.main.activity_infocenter.*

class InfoCenterActivity : BaseActivity() {
	private val contactList = arrayListOf<ContactAdapterItem>()
	private val eventList = arrayListOf<EventAdapterItem>()
	private val absenceList = arrayListOf<AbsenceAdapterItem>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_infocenter)

		val contactAdapter = ContactAdapter(contactList)
		val eventAdapter = EventAdapter(eventList)
		val absenceAdapter = AbsenceAdapter(absenceList)

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
}
