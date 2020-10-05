package com.sapuseven.untis.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.GridView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.sapuseven.untis.R
import com.sapuseven.untis.adapters.GridViewDatabaseItemAdapter
import com.sapuseven.untis.adapters.GridViewDatabaseItemCheckBoxAdapter
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.models.untis.timetable.PeriodElement

class ElementPickerDialog : DialogFragment() {
	private var timetableDatabaseInterface: TimetableDatabaseInterface? = null
	private var type: TimetableDatabaseInterface.Type? = null
	private var config: ElementPickerDialogConfig? = null

	private lateinit var adapter: GridViewDatabaseItemAdapter

	private lateinit var defaultTextColor: ColorStateList
	private lateinit var holder: Holder

	private lateinit var listener: ElementPickerDialogListener

	private lateinit var searchField: TextInputEditText

	interface ElementPickerDialogListener {
		fun onDialogDismissed(dialog: DialogInterface?)
		fun onPeriodElementClick(fragment: Fragment, element: PeriodElement?, useOrgId: Boolean)
		fun onPositiveButtonClicked(dialog: ElementPickerDialog)
	}

	override fun onAttach(context: Context) {
		super.onAttach(context)
		if (context is ElementPickerDialogListener)
			listener = context
		else if (!::listener.isInitialized)
			throw ClassCastException("$context must implement ElementPickerDialogListener if no listener is passed to initialize()")

		adapter = if (config?.multiSelect == true) GridViewDatabaseItemCheckBoxAdapter(context) else GridViewDatabaseItemAdapter(context)
		adapter.timetableDatabaseInterface = timetableDatabaseInterface
		adapter.notifyDataSetChanged()
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		return activity?.let { activity ->
			val builder = MaterialAlertDialogBuilder(activity)

			builder.setView(generateView(activity))

			config?.let {
				builder.setPositiveButton(it.positiveButtonText) { _: DialogInterface, _: Int ->
					listener.onPositiveButtonClicked(this)
				}

			}

			builder.create()
		} ?: throw IllegalStateException("Activity cannot be null")
	}

	override fun onDismiss(dialog: DialogInterface) {
		super.onDismiss(dialog)
		listener.onDialogDismissed(dialog)
	}

	@SuppressLint("InflateParams")
	private fun generateView(activity: FragmentActivity): View {
		val root = activity.layoutInflater.inflate(R.layout.dialog_element_picker, null) as LinearLayout

		val gridView = root.findViewById<GridView>(R.id.gridview_elementpicker_list)
		gridView.setOnItemClickListener { _, _, position, _ -> listener.onPeriodElementClick(this@ElementPickerDialog, adapter.itemAt(position), false) }
		gridView.adapter = adapter

		holder = Holder(
				etSearch = root.findViewById(R.id.textinputlayout_elementpicker_search),
				tvTeachers = root.findViewById(R.id.textview_elementpicker_teachers),
				tvPersonal = root.findViewById(R.id.textview_elementpicker_personal),
				tvClasses = root.findViewById(R.id.textview_elementpicker_classes),
				tvRooms = root.findViewById(R.id.textview_elementpicker_rooms))

		defaultTextColor = holder.tvPersonal.textColors

		holder.tvPersonal.setOnClickListener {
			listener.onPeriodElementClick(this@ElementPickerDialog, null, false)
		}

		holder.tvClasses.setOnClickListener {
			if (type != TimetableDatabaseInterface.Type.CLASS) {
				type = TimetableDatabaseInterface.Type.CLASS
				select(getTextViewFromElemType(type))
			}
		}

		holder.tvTeachers.setOnClickListener {
			if (type != TimetableDatabaseInterface.Type.TEACHER) {
				type = TimetableDatabaseInterface.Type.TEACHER
				select(getTextViewFromElemType(type))
			}
		}

		holder.tvRooms.setOnClickListener {
			if (type != TimetableDatabaseInterface.Type.ROOM) {
				type = TimetableDatabaseInterface.Type.ROOM
				select(getTextViewFromElemType(type))
			}
		}

		if (config?.hideTypeSelection == true)
			root.findViewById<LinearLayout>(R.id.linearlayout_elementpicker_typeselect).visibility = View.GONE

		searchField = root.findViewById(R.id.textinputedittext_elementpicker_search)
		searchField.addTextChangedListener(object : TextWatcher {
			override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
				if (searchField.isFocused)
					adapter.filter.filter(s.toString())
			}

			override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

			override fun afterTextChanged(s: Editable) {}
		})

		select(getTextViewFromElemType(type))

		return root
	}

	private fun getHint(): String {
		return getString(when (type) {
			TimetableDatabaseInterface.Type.CLASS -> R.string.elementpicker_hint_search_classes
			TimetableDatabaseInterface.Type.TEACHER -> R.string.elementpicker_hint_search_teachers
			TimetableDatabaseInterface.Type.ROOM -> R.string.elementpicker_hint_search_rooms
			else -> R.string.elementpicker_hint_search
		})
	}

	private fun select(tv: TextView) {
		deselect(holder.tvPersonal)
		deselect(holder.tvClasses)
		deselect(holder.tvTeachers)
		deselect(holder.tvRooms)

		context?.let {
			tv.setTextColor(getAttrColor(R.attr.colorPrimary))
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
				tv.compoundDrawables[1 /* TOP */].setTint(getAttrColor(R.attr.colorPrimary))
		}

		searchField.clearFocus()
		hideKeyboard(context)
		searchField.setText("")
		holder.etSearch.hint = getHint()

		adapter.type = type
		adapter.notifyDataSetChanged()
	}

	private fun hideKeyboard(context: Context?) =
			(context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)
					?.hideSoftInputFromWindow(searchField.windowToken, 0)

	private fun deselect(tv: TextView) {
		tv.setTextColor(defaultTextColor)
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			tv.compoundDrawables[1 /* TOP */].setTintList(null)
	}

	private fun getAttrColor(@ColorInt id: Int): Int {
		val typedValue = TypedValue()

		val a = context?.obtainStyledAttributes(typedValue.data, intArrayOf(id))
		val color = a?.getColor(0, 0)

		a?.recycle()

		return color ?: Color.GRAY
	}

	private fun getTextViewFromElemType(elemType: TimetableDatabaseInterface.Type?): TextView {
		return when (elemType) {
			TimetableDatabaseInterface.Type.CLASS -> holder.tvClasses
			TimetableDatabaseInterface.Type.TEACHER -> holder.tvTeachers
			TimetableDatabaseInterface.Type.ROOM -> holder.tvRooms
			else -> holder.tvPersonal
		}
	}

	fun getSelectedItems(): List<PeriodElement> {
		return if (adapter is GridViewDatabaseItemCheckBoxAdapter)
			(adapter as GridViewDatabaseItemCheckBoxAdapter).getSelectedItems()
		else
			emptyList()
	}

	data class Holder(
			val etSearch: TextInputLayout,
			val tvTeachers: TextView,
			val tvPersonal: TextView,
			val tvClasses: TextView,
			val tvRooms: TextView
	)

	companion object {
		fun newInstance(
				timetableDatabaseInterface: TimetableDatabaseInterface,
				config: ElementPickerDialogConfig,
				listener: ElementPickerDialogListener? = null): ElementPickerDialog {
			val fragment = ElementPickerDialog()
			fragment.timetableDatabaseInterface = timetableDatabaseInterface
			fragment.type = config.startPage
			fragment.config = config
			listener?.let { fragment.listener = it }
			return fragment
		}

		data class ElementPickerDialogConfig(
				val startPage: TimetableDatabaseInterface.Type,
				val multiSelect: Boolean = false,
				val hideTypeSelection: Boolean = false,
				val positiveButtonText: String? = null
		)
	}
}