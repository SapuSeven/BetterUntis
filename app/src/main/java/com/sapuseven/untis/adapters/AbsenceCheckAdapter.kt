package com.sapuseven.untis.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.sapuseven.untis.R

class AbsenceCheckAdapter(
		private val onClickListener: (item: AbsenceCheckAdapterItem) -> Unit
) : MutableAdapter<AbsenceCheckAdapterItem>() {

	class ViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {
		var ivStatus: ImageView = itemView.findViewById(R.id.imageview_itemabsencecheck)
		var tvName: TextView = itemView.findViewById(R.id.textview_itemabsencecheck_name)
		var tvDetails: TextView = itemView.findViewById(R.id.textview_itemabsencecheck_details)
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val v = LayoutInflater.from(parent.context).inflate(R.layout.item_absence_check, parent, false) as ConstraintLayout
		val viewHolder = ViewHolder(v)
		v.setOnClickListener {
			onClickListener(dataset[viewHolder.layoutPosition])
		}
		return viewHolder
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val item = dataset[position]
		holder.tvName.text = item.toString()
		holder.tvDetails.text = item.absence?.text ?: ""
		holder.ivStatus.setImageResource(if (item.absence == null) R.drawable.all_check else R.drawable.all_cross)
	}
}
