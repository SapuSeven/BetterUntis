package com.sapuseven.untis.helpers

// NOTES: Should do the same as ElementPickerDialog, bit with support of dynamic list changes.
//        Maybe create a common class for these two pickers to simplify the implementation of future feaures.
/*open class ElementSelectionListHelper protected constructor(private val contextActivity: Activity) {
	private var list: MutableList<PeriodElement> = mutableListOf()
	private val adapter: GridViewAdapter
	private var view: View = ViewStub(contextActivity)

	init {
		val interfaceType = TimetableDatabaseInterface.Type.valueOf(type)
		list = timetableDatabaseInterface.getElements(type)

		adapter = GridViewAdapter(contextActivity, list) {

		}
		/*override fun modifyItemView(view: View, position: Int) {
			super.modifyItemView(view, position)
			applyStyling(view, position)
		}*/
	}

	open fun applyStyling(view: View, position: Int) {}

	fun getView(): View? {
		generateView()

		return view
	}

	fun getView(@StringRes searchFieldHint: Int): View {
		generateView()

		val searchField = view.findViewById<TextInputLayout>(R.id.etLayout)
		searchField.hint = contextActivity.resources.getString(searchFieldHint)

		return view
	}

	fun getView(searchFieldHint: String): View {
		generateView()

		val searchField = view.findViewById<TextInputLayout>(R.id.etLayout)
		searchField.hint = searchFieldHint

		return view
	}

	@SuppressLint("InflateParams")
	private fun generateView() {
		view = contextActivity.layoutInflater.inflate(R.layout.dialog_element_picker, null)

		val gridView = view.findViewById<GridView>(R.id.gv)
		gridView.setOnItemClickListener { parent, view, position, id -> onItemSelected(position) }
		gridView.adapter = adapter

		val searchField = view.findViewById<TextInputEditText>(R.id.et)
		searchField.addTextChangedListener(object : TextWatcher {
			override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
				adapter.filter.filter(s.toString())
			}

			override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

			override fun afterTextChanged(s: Editable) {}
		})
	}

	open fun onItemSelected(position: Int) {

	}

	fun setSourceField(masterDataField: String) {
		list.clear()

		val elemList = userDataList.getJSONObject("masterData")
				.getJSONArray(masterDataField)
		for (i in 0 until elemList.length())
			list.add(elemList.getJSONObject(i).getString("name"))
		list.sortWith(Comparator { obj, str -> obj.compareTo(str, ignoreCase = true) })

		adapter.originalItems = list
		adapter.notifyDataSetChanged()
	}
}
*/