package com.sapuseven.untis.activities

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat.createShortcutResultIntent
import androidx.core.graphics.drawable.IconCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.sapuseven.untis.R
import com.sapuseven.untis.adapters.ProfileListAdapter
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.dialogs.ElementPickerDialog
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.models.untis.timetable.PeriodElement
import com.sapuseven.untis.ui.common.ElementPickerDialogFullscreen
import com.sapuseven.untis.ui.theme.AppTheme

class ShortcutConfigureActivity : BaseActivity(), ElementPickerDialog.ElementPickerDialogListener {

	private var userId: Long = 0
	private lateinit var timetableDatabaseInterface: TimetableDatabaseInterface
	private lateinit var profileListAdapter: ProfileListAdapter
	private lateinit var userList: RecyclerView

	private val onClickListener = View.OnClickListener {
		userId = profileListAdapter.itemAt(userList.getChildLayoutPosition(it)).id ?: 0
		timetableDatabaseInterface =
			TimetableDatabaseInterface(UserDatabase.createInstance(this), userId)

		ElementPickerDialog.newInstance(
			timetableDatabaseInterface ?: return@OnClickListener,
			ElementPickerDialog.Companion.ElementPickerDialogConfig(TimetableDatabaseInterface.Type.CLASS)
		).show(supportFragmentManager, "elementPicker") // TODO: Do not hard-code the tag
	}
	private val onLongClickListener = View.OnLongClickListener { true }

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setResult(RESULT_CANCELED)

		val userDatabase = UserDatabase.createInstance(this)
		userId = userDatabase.getAllUsers().first().id!!
		timetableDatabaseInterface = TimetableDatabaseInterface(userDatabase, userId)

		setContent {
			AppTheme {
				ElementPickerDialogFullscreen(
					title = { Text(stringResource(id = R.string.widget_configuration)) },
					timetableDatabaseInterface = timetableDatabaseInterface,
					onDismiss = { finish() },
					onSelect = {
						setupShortcut(userId, it)
						finish()
					}
				)
			}
		}
	}

	override fun onDialogDismissed(dialog: DialogInterface?) {}

	override fun onPeriodElementClick(
		fragment: Fragment,
		element: PeriodElement?,
		useOrgId: Boolean
	) {
		setupShortcut(userId, element)
		finish()
	}

	override fun onPositiveButtonClicked(dialog: ElementPickerDialog) {
		dialog.dismiss()
	}

	private fun setupShortcut(user: Long, element: PeriodElement?) {
		val shortcutIntent = Intent(this, MainActivity::class.java)
			.setAction("")
			.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
			.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
			.putExtra("user", user)
			.putExtra("type", element?.type)
			.putExtra("id", element?.id)
			.putExtra("orgId", element?.orgId)
			.putExtra("useOrgId", false)

		setResult(
			RESULT_OK,
			createShortcutResultIntent(
				this,
				ShortcutInfoCompat.Builder(
					this,
					"${element?.type}-${element?.id}-${element?.orgId}"
				)
					.setIntent(shortcutIntent)
					//.setActivity(MainActivity)
					.setShortLabel(
						if (element == null) resources.getString(R.string.all_personal)
						else timetableDatabaseInterface.getShortName(
							element.id,
							TimetableDatabaseInterface.Type.valueOf(element.type)
						) ?: "Timetable"
					)
					.setIcon(IconCompat.createWithResource(this, R.mipmap.ic_shortcut))
					.build()
			)
		)
	}
}
