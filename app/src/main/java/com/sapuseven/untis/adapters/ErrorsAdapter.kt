package com.sapuseven.untis.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sapuseven.untis.R
import com.sapuseven.untis.activities.ErrorsActivity

class ErrorsAdapter(
		val errorList: List<ErrorsActivity.ErrorData> = ArrayList()
) : RecyclerView.Adapter<ErrorsAdapter.ViewHolder>() {
	private var onClickListener: (ErrorsActivity.ErrorData) -> Unit = {}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item_twoline, parent, false)
		return ViewHolder(v)
	}

	override fun getItemCount() = errorList.size

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val error = errorList[position]

		holder.line1.text = error.time
		holder.line2.text = error.log.substringBefore("\n")

		holder.rootView.setOnClickListener {
			onClickListener(errorList[position])
		}
	}

	fun setOnItemClickListener(listener: (ErrorsActivity.ErrorData) -> Unit) {
		onClickListener = listener
	}

	class ViewHolder(val rootView: View) : RecyclerView.ViewHolder(rootView) {
		val line1: TextView = rootView.findViewById(R.id.textview_listitem_line1)
		val line2: TextView = rootView.findViewById(R.id.textview_listitem_line2)
	}
}
