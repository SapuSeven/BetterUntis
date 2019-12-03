package com.sapuseven.untis.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.sapuseven.untis.R
import com.sapuseven.untis.data.timetable.TimegridItem
import com.sapuseven.untis.helpers.ConversionUtils
import com.sapuseven.untis.helpers.KotlinUtils.safeLet
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.models.untis.timetable.PeriodElement
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat


class TimetableItemDetailsDialog : DialogFragment() {
	private var item: TimegridItem? = null
	private var timetableDatabaseInterface: TimetableDatabaseInterface? = null

	private lateinit var listener: TimetableItemDetailsDialogListener

	companion object {
		val HOMEWORK_DUE_TIME_FORMAT: DateTimeFormatter = ISODateTimeFormat.date()

		fun createInstance(item: TimegridItem, timetableDatabaseInterface: TimetableDatabaseInterface?): TimetableItemDetailsDialog {
			val fragment = TimetableItemDetailsDialog()
			fragment.item = item
			fragment.timetableDatabaseInterface = timetableDatabaseInterface
			return fragment
		}
	}

	interface TimetableItemDetailsDialogListener {
		fun onPeriodElementClick(dialog: DialogFragment, element: PeriodElement?, useOrgId: Boolean)
	}

	override fun onAttach(context: Context) {
		super.onAttach(context)
		if (context is TimetableItemDetailsDialogListener)
			listener = context
		else
			throw ClassCastException("$context must implement TimetableItemDetailsDialogListener")
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		return activity?.let { activity ->
			return AlertDialog.Builder(activity).apply {
				safeLet(item, timetableDatabaseInterface) { item, timetableDatabaseInterface ->
					setView(generateView(activity, item, timetableDatabaseInterface))
				} ?: run {
					setMessage(getString(R.string.main_dialog_itemdetails_error_not_found))
							.setNeutralButton(getString(R.string.all_close)) { dialog, _ ->
								dialog.dismiss()
							}
				}
			}.create()
		} ?: throw IllegalStateException("Activity cannot be null")
	}

	@SuppressLint("InflateParams")
	private fun generateView(activity: FragmentActivity, item: TimegridItem, timetableDatabaseInterface: TimetableDatabaseInterface): View {
		val root = activity.layoutInflater.inflate(R.layout.dialog_timetable_item_details_page, null) as LinearLayout

		val attrs = intArrayOf(android.R.attr.textColorPrimary)
		val ta = context?.obtainStyledAttributes(attrs)
		val color = ta?.getColor(0, 0)
		ta?.recycle()

		listOf(
				item.periodData.element.text.lesson,
				item.periodData.element.text.substitution,
				item.periodData.element.text.info
		).forEach {
			if (it.isNotBlank()) {
				val infoView = activity.layoutInflater.inflate(R.layout.dialog_timetable_item_details_page_info, null)
				(infoView.findViewById<TextView>(R.id.tvInfo)).text = it
				root.addView(infoView)
			}
		}

		item.periodData.element.homeWorks?.forEach {
			val endDate = HOMEWORK_DUE_TIME_FORMAT.parseDateTime(it.endDate)

			val infoView = activity.layoutInflater.inflate(R.layout.dialog_timetable_item_details_page_homework, null)
			(infoView.findViewById<TextView>(R.id.textview_roomfinder_name)).text = it.text
			(infoView.findViewById<TextView>(R.id.tvDate)).text = getString(R.string.homeworks_due_time, endDate.toString(getString(R.string.homeworks_due_time_format)))
			root.addView(infoView)
		}

		val teacherList = root.findViewById<LinearLayout>(R.id.llTeacherList)
		val klassenList = root.findViewById<LinearLayout>(R.id.llClassList)
		val roomList = root.findViewById<LinearLayout>(R.id.llRoomList)

		if (populateList(timetableDatabaseInterface, teacherList, item.periodData.teachers.toList(), TimetableDatabaseInterface.Type.TEACHER, color))
			root.findViewById<View>(R.id.llTeachers).visibility = View.GONE
		if (populateList(timetableDatabaseInterface, klassenList, item.periodData.classes.toList(), TimetableDatabaseInterface.Type.CLASS, color))
			root.findViewById<View>(R.id.llClasses).visibility = View.GONE
		if (populateList(timetableDatabaseInterface, roomList, item.periodData.rooms.toList(), TimetableDatabaseInterface.Type.ROOM, color))
			root.findViewById<View>(R.id.llRooms).visibility = View.GONE

		if (item.periodData.subjects.size > 0) {
			var title = item.periodData.getLongTitle()
			if (item.periodData.isCancelled())
				title = getString(R.string.all_lesson_cancelled, title)
			if (item.periodData.isIrregular())
				title = getString(R.string.all_lesson_irregular, title)
			if (item.periodData.isExam())
				title = getString(R.string.all_lesson_exam, title)

			(root.findViewById(R.id.title) as TextView).text = title
		} else {
			root.findViewById<View>(R.id.title).visibility = View.GONE
		}
		return root
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
		tv.text = timetableDatabaseInterface.getShortName(if (useOrgId) element.orgId else element.id, type)
		if (tv.text.isBlank()) return null
		tv.layoutParams = params
		textColor?.let { tv.setTextColor(it) }
		tv.gravity = Gravity.CENTER_VERTICAL
		if (useOrgId) {
			tv.paintFlags = tv.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
		}
		tv.setOnClickListener {
			listener.onPeriodElementClick(this, element, useOrgId)
			dismiss()
		}
		return tv
	}
}