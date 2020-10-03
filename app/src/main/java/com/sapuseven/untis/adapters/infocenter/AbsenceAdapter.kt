package com.sapuseven.untis.adapters.infocenter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.sapuseven.untis.R
import com.sapuseven.untis.models.UntisAbsence
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import java.util.*
import kotlin.collections.ArrayList

class AbsenceAdapter(
		private val context: Context,
		private val absenceList: List<UntisAbsence> = ArrayList()
) : RecyclerView.Adapter<AbsenceAdapter.ViewHolder>() {
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val v = LayoutInflater.from(parent.context).inflate(R.layout.item_absence, parent, false)
		return ViewHolder(v)
	}

	override fun getItemCount() = absenceList.size

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val absence = absenceList[position]

		holder.tvTime.text = formatAbsenceTime(
				absence.startDateTime.toLocalDateTime(),
				absence.endDateTime.toLocalDateTime()
		)
		holder.tvTitle.text =
				if (absence.absenceReason.isNotEmpty())
					absence.absenceReason.substring(0, 1).toUpperCase(Locale.getDefault()) + absence.absenceReason.substring(1)
				else
					context.getString(R.string.infocenter_absence_unknown_reason)

		if (absence.text.isNotEmpty()) {
			holder.tvText.visibility = View.VISIBLE
			holder.tvText.text = absence.text
		}

		holder.ivExcused.setImageDrawable(
				if (absence.excused)
					context.getDrawable(R.drawable.infocenter_absences_excused)
				else
					context.getDrawable(R.drawable.infocenter_absences_unexcused)
		)
	}

	private fun formatAbsenceTime(startDateTime: LocalDateTime, endDateTime: LocalDateTime): String {
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
		val tvTime: TextView = rootView.findViewById(R.id.textview_itemabsence_time)
		val tvTitle: TextView = rootView.findViewById(R.id.textview_itemabsence_title)
		val tvText: TextView = rootView.findViewById(R.id.textview_itemabsence_text)
		val ivExcused: AppCompatImageView = rootView.findViewById(R.id.imageview_itemabsence_excused)
	}
}
