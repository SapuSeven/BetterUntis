package com.sapuseven.untis.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.sapuseven.untis.R
import com.sapuseven.untis.helpers.viewmodels.PeriodDataViewModel
import com.sapuseven.untis.models.untis.timetable.Period

class AbsenceCheckFragment : Fragment() {
	private var element: Period? = null

	companion object {
		fun createInstance(element: Period): AbsenceCheckFragment = AbsenceCheckFragment().apply {
			this.element = element
		}
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return activity?.layoutInflater?.inflate(R.layout.fragment_timetable_absence_check, container, false) as LinearLayout
	}

	override fun onAttach(context: Context) {
		super.onAttach(context)

		if (activity == null) return

		val periodDataViewModel: PeriodDataViewModel = ViewModelProviders.of(activity!!).get(PeriodDataViewModel::class.java)
	}
}
