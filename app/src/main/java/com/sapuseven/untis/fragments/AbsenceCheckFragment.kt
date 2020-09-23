package com.sapuseven.untis.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.sapuseven.untis.R
import com.sapuseven.untis.adapters.AbsenceCheckAdapter
import com.sapuseven.untis.adapters.AbsenceCheckAdapterItem
import com.sapuseven.untis.viewmodels.PeriodDataViewModel
import java.text.Collator

class AbsenceCheckFragment : Fragment() {
	private val viewModel: PeriodDataViewModel by activityViewModels()

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		val rootView = activity?.layoutInflater?.inflate(R.layout.fragment_timetable_absence_check, container, false) as ViewGroup

		val rvAbsenceCheck = rootView.findViewById<RecyclerView>(R.id.recyclerview_absence_check)
		val adapter = AbsenceCheckAdapter {
			if (it.absence is PeriodDataViewModel.PendingAbsence) return@AbsenceCheckAdapter

			if (it.absence.untisAbsence == null)
				viewModel.createAbsence(it.student)
			else
				viewModel.deleteAbsence(it.student, it.absence.untisAbsence)
		}
		rvAbsenceCheck.layoutManager = LinearLayoutManager(context)
		rvAbsenceCheck.adapter = adapter

		viewModel.absenceData().observe(viewLifecycleOwner, Observer { absenceList ->
			rootView.findViewById<ProgressBar>(R.id.progressbar_absencecheck_loading).visibility = View.GONE
			adapter.clear()
			adapter.addItems(absenceList.map { AbsenceCheckAdapterItem(it.key, it.value) }.sortedWith(Comparator { s1, s2 ->
				Collator.getInstance().compare(s1.student.fullName(), s2.student.fullName())
			}))
			adapter.notifyDataSetChanged()
		})

		rootView.findViewById<FloatingActionButton>(R.id.fab_absencecheck_save).setOnClickListener {
			viewModel.submitAbsencesChecked(
					onSuccess = {
						parentFragmentManager.popBackStack()
						Snackbar.make(rootView, "Absences checked.", Snackbar.LENGTH_SHORT).show()
					},
					onFailure = {
						Snackbar.make(rootView, "Network error: ${it.message}", Snackbar.LENGTH_SHORT).show()
					}
			)
		}

		return rootView
	}
}
