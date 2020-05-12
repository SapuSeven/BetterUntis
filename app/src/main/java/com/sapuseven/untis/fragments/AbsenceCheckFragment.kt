package com.sapuseven.untis.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sapuseven.untis.R
import com.sapuseven.untis.adapters.AbsenceCheckAdapter
import com.sapuseven.untis.adapters.AbsenceCheckAdapterItem
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.helpers.KotlinUtils
import com.sapuseven.untis.models.untis.timetable.Period
import com.sapuseven.untis.viewmodels.AbsenceCheckViewModel

class AbsenceCheckFragment : Fragment() {
	private var element: Period? = null
	private var user: UserDatabase.User? = null

	companion object {
		fun createInstance(user: UserDatabase.User, element: Period): AbsenceCheckFragment = AbsenceCheckFragment().apply {
			this.user = user
			this.element = element
		}
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		val rootView = activity?.layoutInflater?.inflate(R.layout.fragment_timetable_absence_check, container, false) as ViewGroup

		val rvAbsenceCheck = rootView.findViewById<RecyclerView>(R.id.recyclerview_absence_check)
		val adapter = AbsenceCheckAdapter(requireContext())
		rvAbsenceCheck.layoutManager = LinearLayoutManager(context)
		rvAbsenceCheck.adapter = adapter

		KotlinUtils.safeLet(user, element?.id) { user, lessonId ->
			val viewModel by viewModels<AbsenceCheckViewModel> { AbsenceCheckViewModel.Factory(user, lessonId) }
			viewModel.absenceList().observe(viewLifecycleOwner, Observer { absenceList ->
				adapter.clear()
				adapter.addItems(absenceList.map { AbsenceCheckAdapterItem(it.key, it.value) }.sortedBy { it.student.fullName() })
				adapter.notifyDataSetChanged()
			})
		}

		return rootView
	}

	override fun onAttach(context: Context) {
		super.onAttach(context)

		if (activity == null) return
	}
}
