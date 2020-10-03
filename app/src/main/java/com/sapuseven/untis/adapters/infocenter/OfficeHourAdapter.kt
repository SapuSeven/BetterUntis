package com.sapuseven.untis.adapters.infocenter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sapuseven.untis.R
import com.sapuseven.untis.models.UntisOfficeHour
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat


class OfficeHourAdapter(
		private val context: Context,
		private val officeHourList: List<UntisOfficeHour> = ArrayList()
) : RecyclerView.Adapter<OfficeHourAdapter.ViewHolder>() {
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val v = LayoutInflater.from(parent.context).inflate(R.layout.item_officehour, parent, false)
		return ViewHolder(v)
	}

	override fun getItemCount() = officeHourList.size

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val officeHour = officeHourList[position]
		holder.tvTime.text = formatOfficeHourTime(
				officeHour.startDateTime.toLocalDateTime(),
				officeHour.endDateTime.toLocalDateTime()
		)
		holder.tvTitle.text = officeHour.displayNameTeacher

		val text = listOf(
				officeHour.displayNameRooms,
				officeHour.phone,
				officeHour.email
		).filter { it?.isNotEmpty() == true }.joinToString("\n")

		holder.tvText.visibility = if (text.isBlank()) View.GONE else View.VISIBLE
		holder.tvText.text = text
	}

	private fun formatOfficeHourTime(startDateTime: LocalDateTime, endDateTime: LocalDateTime): String {
		return context.getString(
				if (startDateTime.dayOfYear == endDateTime.dayOfYear)
					R.string.infocenter_timeformat_sameday
				else
					R.string.infocenter_timeformat,
				startDateTime.toString(DateTimeFormat.mediumDate()),
				startDateTime.toString(DateTimeFormat.shortTime()),
				endDateTime.toString(DateTimeFormat.mediumDate()),
				endDateTime.toString(DateTimeFormat.shortTime())
		)
	}

	class ViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {
		val tvTime: TextView = rootView.findViewById(R.id.textview_itemofficehour_time)
		val tvTitle: TextView = rootView.findViewById(R.id.textview_itemofficehour_title)
		val tvText: TextView = rootView.findViewById(R.id.textview_itemofficehour_text)
	}
}
