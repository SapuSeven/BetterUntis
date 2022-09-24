package com.sapuseven.untis.activities

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.sapuseven.untis.R
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.models.untis.timetable.PeriodElement
import com.sapuseven.untis.ui.dialogs.ElementPickerDialogFullscreen
import com.sapuseven.untis.ui.common.ProfileSelectorAction
import com.sapuseven.untis.widgets.saveIdPref

class WidgetConfigureActivity : BaseComposeActivity() {
	private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setResult(RESULT_CANCELED)

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

		val userDatabase = UserDatabase.createInstance(this)
		val users = userDatabase.getAllUsers()

		setContent {
			AppTheme {
				var selectedUserId by rememberSaveable {
					mutableStateOf(users.firstOrNull()?.id ?: -1)
				}

				var timetableDatabaseInterface by remember {
					mutableStateOf(TimetableDatabaseInterface(userDatabase, selectedUserId))
				}

				LaunchedEffect(selectedUserId) {
					timetableDatabaseInterface =
						TimetableDatabaseInterface(userDatabase, selectedUserId)
				}

				Surface(
					modifier = Modifier.fillMaxSize()
				) {
					ElementPickerDialogFullscreen(
						title = { Text(stringResource(id = R.string.widget_configuration)) },
						timetableDatabaseInterface = timetableDatabaseInterface,
						onDismiss = { finish() },
						onSelect = { element ->
							setupWidget(selectedUserId, element)
							finish()
						},
						additionalActions = {
							ProfileSelectorAction(
								users = userDatabase.getAllUsers(),
								currentSelectionId = selectedUserId,
								hideIfSingleProfile = true,
								onSelectionChange =  {
									selectedUserId = it.id
								}
							)
						}
					)
				}
			}
		}
	}

	private fun setupWidget(userId: Long, element: PeriodElement?) {
		val context = this@WidgetConfigureActivity

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
}
