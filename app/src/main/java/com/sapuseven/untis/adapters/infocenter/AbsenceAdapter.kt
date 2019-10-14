package com.sapuseven.untis.adapters.infocenter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sapuseven.untis.R
import com.sapuseven.untis.models.UntisAbsence
import com.sapuseven.untis.models.untis.UntisDate
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat


class AbsenceAdapter(
		private val context: Context,
		//private val onClickListener: AbsenceClickListener,
		private val absenceList: List<UntisAbsence> = ArrayList()
) : RecyclerView.Adapter<AbsenceAdapter.ViewHolder>() {
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val v = LayoutInflater.from(parent.context).inflate(R.layout.item_absence, parent, false)
		//v.setOnClickListener(onClickListener)
		return ViewHolder(v)
	}

	override fun getItemCount() = absenceList.size

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val absence = absenceList[position]

		holder.tvTime.text = formatAbsenceTime(
				UntisDate(absence.startDateTime).toDateTime().withZone(DateTimeZone.UTC),
				UntisDate(absence.endDateTime).toDateTime().withZone(DateTimeZone.UTC)
		)
		holder.tvTitle.text = absence.absenceReason
	}

	private fun formatAbsenceTime(startDateTime: DateTime, endDateTime: DateTime): String {
		return context.getString(
				if (startDateTime.dayOfYear == endDateTime.dayOfYear)
					R.string.infocenter_absences_timeformat_sameday
				else
					R.string.infocenter_absences_timeformat,
				startDateTime.toString(DateTimeFormat.mediumDate()),
				startDateTime.toString(DateTimeFormat.shortTime()),
				endDateTime.toString(DateTimeFormat.mediumDate()),
				endDateTime.toString(DateTimeFormat.shortTime())
		)
	}

	class ViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {
		val tvTime: TextView = rootView.findViewById(R.id.textview_itemabsence_time)
		val tvTitle: TextView = rootView.findViewById(R.id.textview_itemabsence_title)
	}
}
