package com.sapuseven.untis.adapters.infocenter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.sapuseven.untis.R
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat


class EventAdapter(
		private val context: Context,
		private val eventList: List<EventAdapterItem> = ArrayList(),
		var timetableDatabaseInterface: TimetableDatabaseInterface? = null
) : RecyclerView.Adapter<ViewHolder>() {
	companion object {
		private const val TYPE_EXAM = 1
		private const val TYPE_HOMEWORK = 2
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		return if (viewType == TYPE_EXAM) {
			val v = LayoutInflater.from(parent.context).inflate(R.layout.item_exam, parent, false)
			ExamViewHolder(v)
		} else {
			val v = LayoutInflater.from(parent.context).inflate(R.layout.item_homework, parent, false)
			HomeworkViewHolder(v)
		}
	}

	override fun getItemCount() = eventList.size

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val event = eventList[position]
		if (holder is ExamViewHolder && event.exam != null) {
			val subject = timetableDatabaseInterface?.getShortName(event.exam.subjectId, TimetableDatabaseInterface.Type.SUBJECT)
			holder.tvTime.text = formatExamTime(
					event.exam.startDateTime.toLocalDateTime(),
					event.exam.endDateTime.toLocalDateTime()
			)
			holder.tvTitle.text = if (subject != null && !event.exam.name.contains(subject)) context.getString(R.string.infocenter_events_exam_name_long, subject, event.exam.name) else event.exam.name
		} else if (holder is HomeworkViewHolder && event.homework != null) {
			holder.tvTime.text = event.homework.endDate.toLocalDate().toString(DateTimeFormat.mediumDate())
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

	private fun formatExamTime(startDateTime: LocalDateTime, endDateTime: LocalDateTime): String {
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

	class ExamViewHolder(rootView: View) : ViewHolder(rootView) {
		val tvTime: TextView = rootView.findViewById(R.id.textview_itemexam_time)
		val tvTitle: TextView = rootView.findViewById(R.id.textview_itemexam_title)
	}

	class HomeworkViewHolder(rootView: View) : ViewHolder(rootView) {
		val tvTime: TextView = rootView.findViewById(R.id.textview_itemhomework_time)
		val tvTitle: TextView = rootView.findViewById(R.id.textview_itemhomework_title)
		val tvText: TextView = rootView.findViewById(R.id.textview_itemhomework_text)
	}
}
