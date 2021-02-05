package com.sapuseven.untis.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
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

class SubjectPickerDialog : DialogFragment() {
	private var timetableDatabaseInterface: TimetableDatabaseInterface? = null
	private var type: TimetableDatabaseInterface.Type? = null
	private var config: SubjectPickerDialogConfig? = null

	private lateinit var adapter: GridViewDatabaseItemAdapter

	private lateinit var defaultTextColor: ColorStateList
	private lateinit var holder: Holder

	private lateinit var listener: SubjectPickerDialogListener

	private lateinit var searchField: TextInputEditText

	interface SubjectPickerDialogListener {
		fun onDialogDismissed(dialog: DialogInterface?)
		fun onPeriodElementClick(fragment: Fragment, element: PeriodElement?, useOrgId: Boolean)
		fun onPositiveButtonClicked(dialog: SubjectPickerDialog)
	}

	override fun onAttach(context: Context) {
		super.onAttach(context)
		if (context is SubjectPickerDialogListener)
			listener = context
		else if (!::listener.isInitialized)
			throw ClassCastException("$context must implement SubjectPickerDialogListener if no listener is passed to initialize()")

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
		gridView.adapter = adapter
		var ids = config?.sharedPreferences?.getString("preference_timetable_hide_subject_ids", "")?.trim()?.split(",")
		var items = ArrayList<PeriodElement>()
		timetableDatabaseInterface?.getElements(TimetableDatabaseInterface.Type.SUBJECT)?.forEach{ if (ids?.contains(it.id.toString()) == true) items.add(it)}
		(adapter as GridViewDatabaseItemCheckBoxAdapter).selectItems(items)

		holder = Holder(
				etSearch = root.findViewById(R.id.textinputlayout_elementpicker_search),
				tvSubjects = root.findViewById(R.id.textview_subjectpicker_subjects))

		defaultTextColor = holder.tvSubjects.textColors

		holder.tvSubjects.setOnClickListener {
			if (type != TimetableDatabaseInterface.Type.SUBJECT) {
				type = TimetableDatabaseInterface.Type.SUBJECT
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
			TimetableDatabaseInterface.Type.SUBJECT -> holder.tvSubjects
			else -> holder.tvSubjects
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
			val tvSubjects: TextView
	)

	companion object {


		fun newInstance(
				timetableDatabaseInterface: TimetableDatabaseInterface,
				config: SubjectPickerDialogConfig,
				listener: SubjectPickerDialogListener? = null): SubjectPickerDialog {
			val fragment = SubjectPickerDialog()
			fragment.timetableDatabaseInterface = timetableDatabaseInterface
			fragment.type = config.startPage
			fragment.config = config
			listener?.let { fragment.listener = it }
			return fragment
		}

		data class SubjectPickerDialogConfig(
			val sharedPreferences: SharedPreferences,
			val startPage: TimetableDatabaseInterface.Type = TimetableDatabaseInterface.Type.SUBJECT,
				val multiSelect: Boolean = true,
				val hideTypeSelection: Boolean = true,
				val positiveButtonText: String? = "done"
		)
	}
}
