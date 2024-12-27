package com.sapuseven.untis.activities

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.work.WorkManager
import com.sapuseven.untis.R
import com.sapuseven.untis.api.model.untis.timetable.PeriodElement
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
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

		val users = userDatabase.userDao().getAll()

		setContent {
			AppTheme(navBarInset = false) {
				var selectedUserId by rememberSaveable {
					mutableStateOf(users.firstOrNull()?.id ?: -1)
				}

				val timetableDatabaseInterface by remember(selectedUserId) {
					mutableStateOf(TimetableDatabaseInterface(userDatabase, selectedUserId))
				}

				val context = this

				var showConfirmationDialog by remember { mutableStateOf(true) }

				Surface(
					modifier = Modifier.fillMaxSize()
				) {
					ElementPickerDialogFullscreen(
						title = { Text(stringResource(id = R.string.widget_configuration)) },
						timetableDatabaseInterface = timetableDatabaseInterface,
						onDismiss = { finish() },
						onSelect = { element ->
							// TODO
							/*runBlocking {
								val user = userDatabase.userDao().getById(selectedUserId) ?: run {
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
							}*/
						},
						additionalActions = {
							ProfileSelectorAction(
								users = userDatabase.userDao().getAll(),
								currentSelectionId = selectedUserId,
								hideIfSingleProfile = true,
								onSelectionChange = {
									selectedUserId = it.id
								}
							)
						}
					)

					if (showConfirmationDialog)
						AlertDialog(
							onDismissRequest = {
								showConfirmationDialog = false
							},
							title = {
								Text(getString(R.string.widget_disclaimer_title))
							},
							text = {
								Text(getString(R.string.widget_disclaimer_text))
							},
							confirmButton = {
								TextButton(
									onClick = {
										showConfirmationDialog = false
									}) {
									Text(stringResource(id = R.string.all_ok))
								}
							}
						)
				}
			}
		}
	}
}
