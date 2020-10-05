package com.sapuseven.untis.activities

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sapuseven.untis.R
import com.sapuseven.untis.adapters.ProfileListAdapter
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.dialogs.ElementPickerDialog
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.models.untis.timetable.PeriodElement

class ShortcutConfigureActivity : BaseActivity(), ElementPickerDialog.ElementPickerDialogListener {

	private var userId: Long = 0
	private var timetableDatabaseInterface: TimetableDatabaseInterface? = null
	private lateinit var profileListAdapter: ProfileListAdapter
	private lateinit var userList: RecyclerView

	private val onClickListener = View.OnClickListener {
		userId = profileListAdapter.itemAt(userList.getChildLayoutPosition(it)).id ?: 0
		timetableDatabaseInterface = TimetableDatabaseInterface(UserDatabase.createInstance(this), userId)

		ElementPickerDialog.newInstance(
				timetableDatabaseInterface ?: return@OnClickListener,
				ElementPickerDialog.Companion.ElementPickerDialogConfig(TimetableDatabaseInterface.Type.CLASS)
		).show(supportFragmentManager, "elementPicker") // TODO: Do not hard-code the tag
	}
	private val onLongClickListener = View.OnLongClickListener { true }

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setResult(RESULT_CANCELED)
		setContentView(R.layout.widget_base_configuration)

		userList = findViewById(R.id.recyclerview_daily_messages_widget_configure_profile_list)
		profileListAdapter = ProfileListAdapter(this, UserDatabase.createInstance(this).getAllUsers().toMutableList(), onClickListener, onLongClickListener)
		userList.layoutManager = LinearLayoutManager(this)
		userList.adapter = profileListAdapter
	}

	override fun onDialogDismissed(dialog: DialogInterface?) {}

	override fun onPeriodElementClick(fragment: Fragment, element: PeriodElement?, useOrgId: Boolean) {
		setupShortcut(userId, element, useOrgId)
		finish()
	}

	override fun onPositiveButtonClicked(dialog: ElementPickerDialog) {
		dialog.dismiss()
	}

	private fun setupShortcut(user: Long, element: PeriodElement?, useOrgId: Boolean) {
		val shortcutIntent = Intent(this, MainActivity::class.java)
				.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
				.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
				.putExtra("user", user)
				.putExtra("type", element?.type)
				.putExtra("id", element?.id)
				.putExtra("orgId", element?.orgId)
				.putExtra("useOrgId", useOrgId)
		setResult(
				RESULT_OK,
				Intent().putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
						.putExtra(Intent.EXTRA_SHORTCUT_NAME,
								if (element == null) resources.getString(R.string.all_personal)
								else timetableDatabaseInterface?.getShortName(element.id, TimetableDatabaseInterface.Type.valueOf(element.type)))
						.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(this, R.mipmap.ic_shortcut))
		)
	}
}
