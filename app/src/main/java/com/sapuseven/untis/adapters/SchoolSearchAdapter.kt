package com.sapuseven.untis.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sapuseven.untis.R
import java.util.*

class SchoolSearchAdapter(private val onClickListener: View.OnClickListener) : RecyclerView.Adapter<SchoolSearchAdapter.ViewHolder>() {
	private val dataset = ArrayList<SchoolSearchAdapterItem>()

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val inflater = LayoutInflater.from(parent.context)
		val v = inflater.inflate(R.layout.list_item_twoline, parent, false)
		v.setOnClickListener(onClickListener)
		return ViewHolder(v)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val item = dataset[position]
		val schoolInfo = item.untisSchoolInfo
		schoolInfo?.let {
			holder.tvSchool.text = it.displayName
			holder.tvAddress.text = it.address
		}
	}

	override fun getItemCount(): Int {
		return dataset.size
	}

	fun clearDataset() {
		dataset.clear()
	}

	fun addToDataset(element: SchoolSearchAdapterItem) {
		dataset.add(element)
	}

	fun getDatasetItem(index: Int): SchoolSearchAdapterItem {
		return dataset[index]
	}

	class ViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {
		var tvSchool: TextView = itemView.findViewById(R.id.textview_listitem_line1)
		var tvAddress: TextView = itemView.findViewById(R.id.textview_listitem_line2)
	}
}
