package com.sapuseven.untis.preferences

import android.content.Context
import android.util.AttributeSet
import androidx.preference.DialogPreference
import com.sapuseven.untis.data.databases.User
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface

// Only data handling here (backend)
class ElementPickerPreference(context: Context, attrs: AttributeSet) : DialogPreference(context, attrs) {
	private val DIALOG_FRAGMENT_TAG = "ElementPickerPreference"
	private var profileId: Long = -1
	private val userDatabase = UserDatabase.createInstance(context)
	private lateinit var profileUser: User
	private lateinit var timetableDatabaseInterface: TimetableDatabaseInterface

	// TODO: No idea how this works, the code below is from past experiments
	/*override fun onClick() {
		if (!loadProfile())
			showElementPicker()
	}

	private fun loadProfile(): Boolean {
		if (userDatabase.getUsersCount() < 1)
			return true

		profileId = PreferenceManager(context).defaultPrefs.getInt("profile", -1).toLong() // TODO: Do not hard-code "profile"
		profileId = userDatabase.getAllUsers()[0].id
				?: -1 // TODO: Debugging only. This is a dynamic id.
		profileUser = userDatabase.getUser(profileId)!! // TODO: Show error (invalid profile) if (profileId == -1) or (profileUser == null) and default to the first profile/re-login if necessary. It is mandatory to stop the execution of more code, or else the app will crash.

		timetableDatabaseInterface = TimetableDatabaseInterface(userDatabase, profileUser.id ?: -1)
		return false
	}

	private fun showElementPicker() {
		ElementPickerDialog.newInstance(
				timetableDatabaseInterface,
				ElementPickerDialog.Companion.ElementPickerDialogConfig(TimetableDatabaseInterface.Type.TEACHER)
		)//.show(, DIALOG_FRAGMENT_TAG) // TODO: Do not hard-code the tag
	}*/

	/*private var tvPersonal: TextView? = null
	private var tvClasses: TextView? = null
	private var tvTeachers: TextView? = null
	private var tvRooms: TextView? = null

	private var selectedType: TimetableDatabaseInterface.Type? = null
	private var selectedName: String? = null
	private var selectedId = -1
	private var selectedPosition = -1

	private var defaultTextColor: ColorStateList? = null
	private var defaultItemTextColor: ColorStateList? = null
	private var elemList: ViewGroup? = null
	private var dialog: AlertDialog? = null
	private val DIALOG_FRAGMENT_TAG = "ElementPickerPreference"*/


	/*override fun getDialogLayoutResource(): Int {
		return R.layout.dialog_element_picker
	}

	val summary: CharSequence?
		get() {
			if (selectedName == null) {
				val sharedPrefs = sharedPreferences
				selectedType = TimetableDatabaseInterface.Type.valueOf(sharedPrefs.getString(key, null) ?: "")
				selectedId = sharedPrefs.getInt(key + "_id", -1)
				selectedName = sharedPrefs.getString(key + "_name", "")
			}

			return if (selectedType != null)
				selectedName
			else
				""
		}

	init {
		dialogLayoutResource = R.layout.dialog_element_picker
	}

	protected fun onDialogClosed(positiveResult: Boolean) {
		super.onDialogClosed(positiveResult)

		if (positiveResult) {
			saveSelected()
			if (selectedType != UNKNOWN)
				setSummary(selectedName)
			else
				setSummary("")
		}
	}

	private fun saveSelected() {
		val editor = getEditor()
		editor.putInt(key, selectedType.value)
		editor.putInt(key + "_id", selectedId)
		editor.putString(key + "_name", selectedName)
		editor.commit()
	}*/
}
