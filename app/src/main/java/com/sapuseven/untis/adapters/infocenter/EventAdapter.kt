package com.sapuseven.untis.adapters.infocenter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sapuseven.untis.R
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.models.untis.UntisDate
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat


class EventAdapter(
		private val context: Context,
		//private val onClickListener: AbsenceClickListener,
		private val eventList: List<EventAdapterItem> = ArrayList(),
		var timetableDatabaseInterface: TimetableDatabaseInterface? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
	companion object {
		private const val TYPE_EXAM = 1
		private const val TYPE_HOMEWORK = 2
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		return if (viewType == TYPE_EXAM) {
			val v = LayoutInflater.from(parent.context).inflate(R.layout.item_exam, parent, false)
			//v.setOnClickListener(onClickListener)
			ExamViewHolder(v)
		} else {
			val v = LayoutInflater.from(parent.context).inflate(R.layout.item_homework, parent, false)
			//v.setOnClickListener(onClickListener)
			HomeworkViewHolder(v)
		}
	}

	override fun getItemCount() = eventList.size

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		val event = eventList[position]
		if (holder is ExamViewHolder && event.exam != null) {
			holder.tvTime.text = formatExamTime(
					UntisDate(event.exam.startDateTime).toDateTime().withZone(DateTimeZone.UTC),
					UntisDate(event.exam.endDateTime).toDateTime().withZone(DateTimeZone.UTC)
			)
			holder.tvTitle.text = event.exam.name
		} else if (holder is HomeworkViewHolder && event.homework != null) {
			holder.tvTime.text = UntisDate(event.homework.endDate).toDateTime().withZone(DateTimeZone.UTC).toString(DateTimeFormat.mediumDate())
			holder.tvTitle.text = timetableDatabaseInterface?.getLongName(event.lessonsById?.get(event.homework.lessonId.toString())?.subjectId
					?: 0, TimetableDatabaseInterface.Type.SUBJECT)
					?: event.homework.lessonId.toString()
			holder.tvText.text = event.homework.text
		}
	}

	override fun getItemViewType(position: Int): Int {
		val event = eventList[position]

		return if (event.exam != null) TYPE_EXAM else TYPE_HOMEWORK
	}

	private fun formatExamTime(startDateTime: DateTime, endDateTime: DateTime): String {
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

	class ExamViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {
		val tvTime: TextView = rootView.findViewById(R.id.textview_itemexam_time)
		val tvTitle: TextView = rootView.findViewById(R.id.textview_itemexam_title)
	}

	class HomeworkViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {
		val tvTime: TextView = rootView.findViewById(R.id.textview_itemhomework_time)
		val tvTitle: TextView = rootView.findViewById(R.id.textview_itemhomework_title)
		val tvText: TextView = rootView.findViewById(R.id.textview_itemhomework_text)
	}
}
