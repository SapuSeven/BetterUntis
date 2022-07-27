package com.sapuseven.untis.activities

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.compose.setContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import androidx.recyclerview.widget.RecyclerView
import com.sapuseven.untis.R
import com.sapuseven.untis.adapters.ProfileListAdapter
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.ui.common.ElementPickerDialogFullscreen
import com.sapuseven.untis.ui.theme.AppTheme
import com.sapuseven.untis.widgets.saveIdPref

class WidgetConfigureActivity : BaseComposeActivity() {
	private lateinit var userDatabase: UserDatabase
	private lateinit var user: UserDatabase.User
	private lateinit var timetableDatabaseInterface: TimetableDatabaseInterface

	private lateinit var profileListAdapter: ProfileListAdapter
	private lateinit var userList: RecyclerView
	private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

	private val onClickListener = View.OnClickListener {
		val context = this@WidgetConfigureActivity

		val userId = profileListAdapter.itemAt(userList.getChildLayoutPosition(it)).id ?: 0
		saveIdPref(context, appWidgetId, userId)

		setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))
		context.sendBroadcast(
			Intent().setComponent(
				AppWidgetManager.getInstance(context).getAppWidgetInfo(appWidgetId).provider
			)
				.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
				.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
		)
		finish()
	}
	private val onLongClickListener = View.OnLongClickListener { true }

	@OptIn(ExperimentalMaterial3Api::class)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setResult(RESULT_CANCELED)

		userDatabase = UserDatabase.createInstance(this)
		timetableDatabaseInterface =
			TimetableDatabaseInterface(userDatabase, userDatabase.getAllUsers().first().id!!)

		setContent {
			AppTheme {
				ElementPickerDialogFullscreen(
					title = { Text(stringResource(id = R.string.widget_configuration)) },
					timetableDatabaseInterface = timetableDatabaseInterface,
					onDismiss = { finish() },
					onSelect = {}
				)
			}
		}

		/*userList = findViewById(R.id.recyclerview_daily_messages_widget_configure_profile_list)
		profileListAdapter = ProfileListAdapter(
			this,
			UserDatabase.createInstance(this).getAllUsers().toMutableList(),
			onClickListener,
			onLongClickListener
		)
		userList.layoutManager = LinearLayoutManager(this)
		userList.adapter = profileListAdapter*/

		val extras = intent.extras
		if (extras != null) {
			appWidgetId = extras.getInt(
				AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
			)
		}

		if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
			finish()
			return
		}
	}
}
