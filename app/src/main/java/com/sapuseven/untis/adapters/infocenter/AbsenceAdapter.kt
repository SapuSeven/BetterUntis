package com.sapuseven.untis.adapters.infocenter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sapuseven.untis.R


class AbsenceAdapter(
		//private val context: Context,
		//private val onClickListener: AbsenceClickListener,
		private val absenceList: List<AbsenceAdapterItem> = ArrayList()
) : RecyclerView.Adapter<AbsenceAdapter.ViewHolder>() {
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val v = LayoutInflater.from(parent.context).inflate(R.layout.item_absence, parent, false)
		//v.setOnClickListener(onClickListener)
		return ViewHolder(v)
	}

	override fun getItemCount() = absenceList.size

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val absence = absenceList[position]
	}

	class ViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {
		val tvTime: TextView = rootView.findViewById(R.id.textview_itemabsence_time)
		val tvTitle: TextView = rootView.findViewById(R.id.textview_itemabsence_title)
	}
}
