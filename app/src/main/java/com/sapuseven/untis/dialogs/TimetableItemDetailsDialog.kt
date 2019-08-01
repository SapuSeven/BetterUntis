package com.sapuseven.untis.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
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
import com.sapuseven.untis.helpers.KotlinUtils.safeLet
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.models.untis.timetable.PeriodElement

class TimetableItemDetailsDialog : DialogFragment() {
	private var item: TimegridItem? = null
	private var timetableDatabaseInterface: TimetableDatabaseInterface? = null

	private lateinit var listener: TimetableItemDetailsDialogListener

	interface TimetableItemDetailsDialogListener {
		fun onPeriodElementClick(dialog: DialogFragment, element: PeriodElement?)
	}

	override fun onAttach(context: Context) {
		super.onAttach(context)
		if (context is TimetableItemDetailsDialogListener)
			listener = context
		else
			throw ClassCastException(("$context must implement TimetableItemDetailsDialogListener"))
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		return activity?.let { activity ->
			val builder = AlertDialog.Builder(activity)

			if (item == null || timetableDatabaseInterface == null) {
				// TODO: Refactor hard-coded strings
				builder.setMessage("Item not found")
						.setNeutralButton("Close") { dialog, _ ->
							dialog.dismiss()
						}
			} else {
				safeLet(item, timetableDatabaseInterface) { item, timetableDatabaseInterface ->
					builder.setView(generateView(activity, item, timetableDatabaseInterface))
				}
			}

			builder.create()
		} ?: throw IllegalStateException("Activity cannot be null")
	}

	@SuppressLint("InflateParams")
	private fun generateView(activity: FragmentActivity, item: TimegridItem, timetableDatabaseInterface: TimetableDatabaseInterface): View {
		val root = activity.layoutInflater.inflate(R.layout.dialog_timetable_item_details_page, null) as LinearLayout

		if (item.periodData.teachers.isEmpty())
			root.findViewById<View>(R.id.llTeachers).visibility = View.GONE
		if (item.periodData.classes.isEmpty())
			root.findViewById<View>(R.id.llClasses).visibility = View.GONE
		if (item.periodData.rooms.isEmpty())
			root.findViewById<View>(R.id.llRooms).visibility = View.GONE

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

		item.periodData.element.homeWorks.forEach {
			val infoView = activity.layoutInflater.inflate(R.layout.dialog_timetable_item_details_page_homework, null)
			(infoView.findViewById<TextView>(R.id.textview_roomfinder_name)).text = it.text
			(infoView.findViewById<TextView>(R.id.tvDate)).text = "von " + it.startDate + " bis " + it.endDate
			root.addView(infoView)
		}

		val teacherList = root.findViewById<LinearLayout>(R.id.llTeacherList)
		val klassenList = root.findViewById<LinearLayout>(R.id.llClassList)
		val roomList = root.findViewById<LinearLayout>(R.id.llRoomList)

		populateList(timetableDatabaseInterface, teacherList, item.periodData.teachers, TimetableDatabaseInterface.Type.TEACHER, color)
		populateList(timetableDatabaseInterface, klassenList, item.periodData.classes, TimetableDatabaseInterface.Type.CLASS, color)
		populateList(timetableDatabaseInterface, roomList, item.periodData.rooms, TimetableDatabaseInterface.Type.ROOM, color)

		if (item.periodData.subjects.size > 0) {
			var title = item.periodData.getLongTitle()
			if (item.periodData.isCancelled())
				title = getString(R.string.lesson_cancelled, title)
			if (item.periodData.isIrregular())
				title = getString(R.string.lesson_irregular, title)
			if (item.periodData.isExam())
				title = getString(R.string.lesson_exam, title)

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
	                         textColor: Int?) {
		data.forEach { element ->
			val tv = TextView(context!!.applicationContext)
			val params = LinearLayout.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.MATCH_PARENT)
			params.setMargins(0, 0, /*dp2px(12)*/20, 0) // TODO: Make resolution-independent
			tv.text = timetableDatabaseInterface.getShortName(element.id, type)
			tv.layoutParams = params
			textColor?.let { tv.setTextColor(it) }
			tv.gravity = Gravity.CENTER_VERTICAL
			tv.setOnClickListener {
				listener.onPeriodElementClick(this, element)
				dismiss()
			}
			list.addView(tv)
		}
	}

	companion object {
		fun createInstance(item: TimegridItem, timetableDatabaseInterface: TimetableDatabaseInterface?): TimetableItemDetailsDialog {
			val fragment = TimetableItemDetailsDialog()
			fragment.item = item
			fragment.timetableDatabaseInterface = timetableDatabaseInterface
			return fragment
		}
	}
}