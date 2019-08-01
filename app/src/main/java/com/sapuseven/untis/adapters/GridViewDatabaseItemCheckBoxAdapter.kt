package com.sapuseven.untis.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import com.sapuseven.untis.R
import com.sapuseven.untis.models.untis.timetable.PeriodElement


class GridViewDatabaseItemCheckBoxAdapter(context: Context) :
		GridViewDatabaseItemAdapter(context) {
	private var inflater: LayoutInflater = LayoutInflater.from(context)
	private val selectedItems: MutableList<PeriodElement> = ArrayList()

	override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
		val view = convertView ?: inflater.inflate(R.layout.item_gridview_checkbox, parent, false)
		val holder = Holder(view.findViewById(R.id.checkbox))
		holder.checkBox.text = getItem(position)
		holder.checkBox.setOnCheckedChangeListener(null)
		holder.checkBox.isChecked = selectedItems.contains(itemAt(position))
		holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
			if (isChecked && !selectedItems.contains(itemAt(position)))
				selectedItems.add(itemAt(position))
			else if (selectedItems.contains(itemAt(position)))
				selectedItems.remove(itemAt(position))
		}
		return view
	}

	fun getSelectedItems(): List<PeriodElement> {
		return selectedItems.toList()
	}

	private data class Holder(
			var checkBox: CheckBox
	)
}