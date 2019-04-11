package com.sapuseven.untis.adapters

import android.content.Context
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.models.untis.timetable.PeriodElement
import java.util.*

open class GridViewAdapter(context: Context,
                      var originalItems: List<PeriodElement>,
                      val getItemName: (PeriodElement) -> String)
	: ArrayAdapter<String>(context, android.R.layout.simple_list_item_1), Filterable {

	private val filteredItems: MutableList<PeriodElement> = originalItems.toMutableList()
	private val filter = ItemFilter()
	var timetableDatabaseInterface: TimetableDatabaseInterface? = null

	override fun getFilter(): Filter {
		return filter
	}

	override fun getCount(): Int {
		return filteredItems.size
	}

	override fun getItem(position: Int): String {
		return getItemName(filteredItems[position])
	}

	fun itemAt(position: Int): PeriodElement {
		return filteredItems[position]
	}

	private inner class ItemFilter : Filter() {
		override fun publishResults(constraint: CharSequence, results: Filter.FilterResults) {
			filteredItems.clear()

			if (results.values is List<*>)
				filteredItems.addAll((results.values as List<*>).map { it as PeriodElement })
			else
				filteredItems.addAll(originalItems)

			notifyDataSetChanged()
		}

		override fun performFiltering(constraint: CharSequence?): Filter.FilterResults {
			val results = Filter.FilterResults()
			val filteredList = ArrayList<PeriodElement>()

			if (constraint.isNullOrBlank()) {
				results.count = originalItems.size
				results.values = originalItems
			} else {
				for (i in originalItems.indices) {
					val data = originalItems[i]
					if (timetableDatabaseInterface?.elementContains(data, constraint.toString().toLowerCase(), true) == true)
						filteredList.add(data)

				}
				results.count = filteredList.size
				results.values = filteredList
			}
			return results
		}
	}

	open fun modifyItemView(view: View, position: Int) {}
}