package com.sapuseven.untis.fragments

import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.sapuseven.untis.R
import com.sapuseven.untis.activities.MainActivity
import com.sapuseven.untis.data.connectivity.UntisApiConstants.CAN_READ_LESSON_TOPIC
import com.sapuseven.untis.data.connectivity.UntisApiConstants.CAN_READ_STUDENT_ABSENCE
import com.sapuseven.untis.data.connectivity.UntisApiConstants.CAN_WRITE_LESSON_TOPIC
import com.sapuseven.untis.data.connectivity.UntisApiConstants.CAN_WRITE_STUDENT_ABSENCE
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.data.timetable.PeriodData
import com.sapuseven.untis.data.timetable.TimegridItem
import com.sapuseven.untis.helpers.ConversionUtils
import com.sapuseven.untis.helpers.KotlinUtils.safeLet
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.models.untis.timetable.PeriodElement
import com.sapuseven.untis.viewmodels.PeriodDataViewModel
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat

class TimetableItemDetailsFragment(item: TimegridItem?, timetableDatabaseInterface: TimetableDatabaseInterface?, user: UserDatabase.User?) : Fragment() {
	constructor() : this(null, null, null)

	private val viewModel: PeriodDataViewModel by activityViewModels { PeriodDataViewModel.Factory(user, item, timetableDatabaseInterface) }

	private lateinit var listener: TimetableItemDetailsDialogListener

	interface TimetableItemDetailsDialogListener {
		fun onPeriodElementClick(fragment: Fragment, element: PeriodElement?, useOrgId: Boolean)

		fun onPeriodAbsencesClick()

		fun onLessonTopicClick()
	}

	override fun onAttach(context: Context) {
		super.onAttach(context)
		if (context is TimetableItemDetailsDialogListener)
			listener = context
		else
			throw ClassCastException("$context must implement TimetableItemDetailsDialogListener")
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return activity?.let { activity ->
			safeLet(viewModel.item.periodData, viewModel.timetableDatabaseInterface) { periodData, timetableDatabaseInterface ->
				generateView(activity, container, periodData, timetableDatabaseInterface)
			} ?: generateErrorView(activity, container)
		} ?: throw IllegalStateException("Activity cannot be null")
	}

	override fun onStart() {
		super.onStart()
		if (activity is MainActivity) (activity as MainActivity).setFullscreenDialogActionBar()
	}

	override fun onStop() {
		super.onStop()
		if (activity is MainActivity) (activity as MainActivity).setDefaultActionBar()
	}

	private fun generateView(activity: FragmentActivity, container: ViewGroup?, periodData: PeriodData, timetableDatabaseInterface: TimetableDatabaseInterface): View {
		val root = activity.layoutInflater.inflate(R.layout.fragment_timetable_item_details_page, container, false) as ScrollView
		val linearLayout = root.getChildAt(0) as LinearLayout

		val attrs = intArrayOf(android.R.attr.textColorPrimary)
		val ta = context?.obtainStyledAttributes(attrs)
		val color = ta?.getColor(0, 0)
		ta?.recycle()

		setOf(
				periodData.element.text.lesson,
				periodData.element.text.substitution,
				periodData.element.text.info
		).forEach {
			if (it.isNotBlank())
				activity.layoutInflater.inflate(R.layout.fragment_timetable_item_details_page_info, linearLayout, false).run {
					(findViewById<TextView>(R.id.tvInfo)).text = it
					linearLayout.addView(this)
				}
		}

		periodData.element.homeWorks?.forEach {
			val endDate = it.endDate.toLocalDate()

			activity.layoutInflater.inflate(R.layout.fragment_timetable_item_details_page_homework, linearLayout, false).run {
				(findViewById<TextView>(R.id.textview_roomfinder_name)).text = it.text
				(findViewById<TextView>(R.id.tvDate)).text = getString(R.string.homeworks_due_time, endDate.toString(getString(R.string.homeworks_due_time_format)))
				linearLayout.addView(this)
			}
		}

		if (periodData.element.can.contains(CAN_READ_STUDENT_ABSENCE))
			activity.layoutInflater.inflate(R.layout.fragment_timetable_item_details_page_absences, linearLayout, false).run {
				viewModel.periodData().observe(viewLifecycleOwner, Observer {
					this.findViewById<TextView>(R.id.textview_timetableitemdetails_absencestatus).text = getString(
							if (it.absenceChecked) R.string.all_dialog_absences_checked
							else R.string.all_dialog_absences_not_checked
					)
					this.findViewById<ImageView>(R.id.imageview_timetableitemdetails_absence).setImageResource(
							if (it.absenceChecked) R.drawable.all_absences_checked
							else R.drawable.all_absences
					)
				})

				if (periodData.element.can.contains(CAN_WRITE_STUDENT_ABSENCE))
					setOnClickListener {
						listener.onPeriodAbsencesClick()
					}
				linearLayout.addView(this)
			}

		if (periodData.element.can.contains(CAN_READ_LESSON_TOPIC)) {
			activity.layoutInflater.inflate(R.layout.fragment_timetable_item_details_page_lessontopic, root, false).run {
				viewModel.periodData().observe(viewLifecycleOwner, Observer {
					this.findViewById<TextView>(R.id.textview_timetableitemdetails_lessontopic).text =
							if (it.topic?.text.isNullOrBlank())
								if (periodData.element.can.contains(CAN_WRITE_LESSON_TOPIC))
									getString(R.string.all_hint_tap_to_edit)
								else
									getString(R.string.all_lessontopic_none)
							else
								it.topic?.text
				})
				if (periodData.element.can.contains(CAN_WRITE_LESSON_TOPIC))
					setOnClickListener {
						listener.onLessonTopicClick()
					}
				linearLayout.addView(this)
			}
		}

		val teacherList = linearLayout.findViewById<LinearLayout>(R.id.llTeacherList)
		val klassenList = linearLayout.findViewById<LinearLayout>(R.id.llClassList)
		val roomList = linearLayout.findViewById<LinearLayout>(R.id.llRoomList)

		if (populateList(timetableDatabaseInterface, teacherList, periodData.teachers.toList(), TimetableDatabaseInterface.Type.TEACHER, color))
			linearLayout.findViewById<View>(R.id.llTeachers).visibility = View.GONE
		if (populateList(timetableDatabaseInterface, klassenList, periodData.classes.toList(), TimetableDatabaseInterface.Type.CLASS, color))
			linearLayout.findViewById<View>(R.id.llClasses).visibility = View.GONE
		if (populateList(timetableDatabaseInterface, roomList, periodData.rooms.toList(), TimetableDatabaseInterface.Type.ROOM, color))
			linearLayout.findViewById<View>(R.id.llRooms).visibility = View.GONE

		if (periodData.subjects.size > 0) {
			var title = periodData.getLong(periodData.subjects, TimetableDatabaseInterface.Type.SUBJECT)
			if (periodData.isCancelled())
				title = getString(R.string.all_lesson_cancelled, title)
			if (periodData.isIrregular())
				title = getString(R.string.all_lesson_irregular, title)
			if (periodData.isExam())
				title = getString(R.string.all_lesson_exam, title)

			(linearLayout.findViewById(R.id.title) as TextView).text = title
		} else {
			linearLayout.findViewById<View>(R.id.title).visibility = View.GONE
		}

		linearLayout.findViewById<TextView>(R.id.time).text = formatLessonTime(periodData.element.startDateTime.toLocalDateTime(), periodData.element.endDateTime.toLocalDateTime())
		return root
	}

	private fun generateErrorView(activity: FragmentActivity, container: ViewGroup?): View {
		return activity.layoutInflater.inflate(R.layout.fragment_timetable_item_details_page_error, container, false)
	}

	private fun populateList(timetableDatabaseInterface: TimetableDatabaseInterface,
	                         list: LinearLayout,
	                         data: List<PeriodElement>,
	                         type: TimetableDatabaseInterface.Type,
	                         textColor: Int?): Boolean {
		if (data.isEmpty()) return true
		data.forEach { element ->
			generateTextViewForElement(element, type, timetableDatabaseInterface, textColor, false)?.let { list.addView(it) }
			if (element.id != element.orgId)
				generateTextViewForElement(element, type, timetableDatabaseInterface, textColor, true)?.let { list.addView(it) }
		}
		return false
	}

	private fun generateTextViewForElement(element: PeriodElement,
	                                       type: TimetableDatabaseInterface.Type,
	                                       timetableDatabaseInterface: TimetableDatabaseInterface,
	                                       textColor: Int?,
	                                       useOrgId: Boolean = false): TextView? {
		val tv = TextView(requireContext())
		val params = LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.MATCH_PARENT)
		params.setMargins(0, 0, ConversionUtils.dpToPx(12.0f, requireContext()).toInt(), 0)
		tv.text =
				if (type == TimetableDatabaseInterface.Type.TEACHER) timetableDatabaseInterface.getLongName(if (useOrgId) element.orgId else element.id, type)
				else timetableDatabaseInterface.getShortName(if (useOrgId) element.orgId else element.id, type)
		if (tv.text.isBlank()) return null
		tv.layoutParams = params
		textColor?.let { tv.setTextColor(it) }
		tv.gravity = Gravity.CENTER_VERTICAL
		if (useOrgId) {
			tv.paintFlags = tv.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
		}
		tv.setOnClickListener {
			listener.onPeriodElementClick(this, element, useOrgId)
		}
		return tv
	}

	private fun formatLessonTime(startDateTime: LocalDateTime, endDateTime: LocalDateTime): String {
		return requireContext().getString(
				R.string.main_dialog_itemdetails_timeformat,
				startDateTime.toString(DateTimeFormat.shortTime()),
				endDateTime.toString(DateTimeFormat.shortTime())
		)
	}
}

private fun <E> List<E>.containsAny(vararg items: E): Boolean = this.any(items.toSet()::contains)
