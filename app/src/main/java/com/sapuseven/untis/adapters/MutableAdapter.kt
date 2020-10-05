package com.sapuseven.untis.adapters

import androidx.recyclerview.widget.RecyclerView

abstract class MutableAdapter<T>(
		val dataset: MutableList<T> = mutableListOf()
) : RecyclerView.Adapter<AbsenceCheckAdapter.ViewHolder>() {
	override fun getItemCount() = dataset.size

	fun clear() = dataset.clear()

	fun addItems(elements: Collection<T>) = dataset.addAll(elements)
}
