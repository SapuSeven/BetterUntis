package com.sapuseven.untis.activities

import android.app.Activity
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
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.work.WorkManager
import com.sapuseven.untis.R
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.models.untis.timetable.PeriodElement
import com.sapuseven.untis.ui.common.ProfileSelectorAction
import com.sapuseven.untis.ui.dialogs.ElementPickerDialogFullscreen
import com.sapuseven.untis.widgets.BaseComposeWidget
import com.sapuseven.untis.widgets.BaseComposeWidget.Companion.PREFERENCE_KEY_INT_ELEMENT_ID
import com.sapuseven.untis.widgets.BaseComposeWidget.Companion.PREFERENCE_KEY_LONG_USER
import com.sapuseven.untis.widgets.BaseComposeWidget.Companion.PREFERENCE_KEY_STRING_ELEMENT_TYPE
import com.sapuseven.untis.workers.TimetableDependantWorker
import com.sapuseven.untis.workers.WidgetUpdateWorker
import kotlinx.coroutines.runBlocking

class ElementPickerWidgetConfigureActivity : BaseComposeActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setResult(RESULT_CANCELED)

		val appWidgetId = intent?.extras?.getInt(
			AppWidgetManager.EXTRA_APPWIDGET_ID,
			AppWidgetManager.INVALID_APPWIDGET_ID
		) ?: AppWidgetManager.INVALID_APPWIDGET_ID

		if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
			finish()
			return
		}

		val userDatabase = UserDatabase.createInstance(this)
		val users = userDatabase.getAllUsers()

		setContent {
			AppTheme(navBarInset = false) {
				var selectedUserId by rememberSaveable {
					mutableStateOf(users.firstOrNull()?.id ?: -1)
				}

				val timetableDatabaseInterface by remember(selectedUserId) {
					mutableStateOf(TimetableDatabaseInterface(userDatabase, selectedUserId))
				}

				val context = this

				Surface(
					modifier = Modifier.fillMaxSize()
				) {
					ElementPickerDialogFullscreen(
						title = { Text(stringResource(id = R.string.widget_configuration)) },
						timetableDatabaseInterface = timetableDatabaseInterface,
						onDismiss = { finish() },
						onSelect = { element ->
							runBlocking {
								val user = userDatabase.getUser(selectedUserId) ?: run {
									setResult(Activity.RESULT_CANCELED)
									finish()
									return@runBlocking
								}

								val selectedElement =
									element
										?: TimetableDependantWorker.loadPersonalTimetableElement(
											user,
											context
										)?.let {
											PeriodElement(
												it.second,
												it.first,
												it.first
											)
										} ?: run {
											finish()
											return@runBlocking
										}

								val glanceId =
									GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)
								updateAppWidgetState(context, glanceId) { prefs ->
									prefs[longPreferencesKey(PREFERENCE_KEY_LONG_USER)] =
										selectedUserId
									prefs[intPreferencesKey(PREFERENCE_KEY_INT_ELEMENT_ID)] =
										selectedElement.id
									prefs[stringPreferencesKey(PREFERENCE_KEY_STRING_ELEMENT_TYPE)] =
										selectedElement.type
								}
								BaseComposeWidget().update(context, glanceId)
								WidgetUpdateWorker.enqueue(WorkManager.getInstance(context))

								val resultValue = Intent().putExtra(
									AppWidgetManager.EXTRA_APPWIDGET_ID,
									appWidgetId
								)
								setResult(Activity.RESULT_OK, resultValue)
								finish()
							}
						},
						additionalActions = {
							ProfileSelectorAction(
								users = userDatabase.getAllUsers(),
								currentSelectionId = selectedUserId,
								hideIfSingleProfile = true,
								onSelectionChange = {
									selectedUserId = it.id
								}
							)
						}
					)
				}
			}
		}
	}
}
