package com.sapuseven.untis.adapters.infocenter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sapuseven.untis.R


class EventAdapter(
		//private val context: Context,
		//private val onClickListener: AbsenceClickListener,
		private val eventList: List<EventAdapterItem> = ArrayList()
) : RecyclerView.Adapter<EventAdapter.ViewHolder>() {
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val v = LayoutInflater.from(parent.context).inflate(R.layout.item_absence, parent, false) // TODO: Switch to event item
		//v.setOnClickListener(onClickListener)
		return ViewHolder(v)
	}

	override fun getItemCount() = eventList.size

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val event = eventList[position]
	}

	class ViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView)
}
