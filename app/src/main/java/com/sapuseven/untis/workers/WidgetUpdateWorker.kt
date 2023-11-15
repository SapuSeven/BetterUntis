package com.sapuseven.untis.workers

import android.content.Context
import android.util.Log
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.ui.widgets.WidgetListItemModel
import com.sapuseven.untis.widgets.BaseComposeWidget.Companion.PREFERENCE_KEY_INT_ELEMENT_ID
import com.sapuseven.untis.widgets.BaseComposeWidget.Companion.PREFERENCE_KEY_LONG_USER
import com.sapuseven.untis.widgets.BaseComposeWidget.Companion.PREFERENCE_KEY_STRING_ELEMENT_TYPE
import com.sapuseven.untis.widgets.TimetableWidget
import com.sapuseven.untis.widgets.toGlanceTextStyle
import org.joda.time.format.DateTimeFormat

/**
 * This worker loads the data for widgets.
 */
class WidgetUpdateWorker(context: Context, params: WorkerParameters) :
	TimetableDependantWorker(context, params) {
	companion object {
		private const val LOG_TAG = "WidgetUpdate"
		private const val TAG_WIDGET_UPDATE_WORK = "WidgetUpdateWork"

		fun enqueue(workManager: WorkManager) {
			workManager.enqueue(
				OneTimeWorkRequestBuilder<WidgetUpdateWorker>()
					.addTag(TAG_WIDGET_UPDATE_WORK)
					.build()
			)
		}
	}

	override suspend fun doWork(): Result {
		val userDatabase = UserDatabase.getInstance(applicationContext)

		val timeFormatter = DateTimeFormat.forPattern("HH:mm")

		GlanceAppWidgetManager(applicationContext).getGlanceIds(TimetableWidget::class.java)
			.forEach { glanceId ->
				val prefs =
					TimetableWidget().getAppWidgetState<Preferences>(applicationContext, glanceId)

				val userId = prefs[longPreferencesKey(PREFERENCE_KEY_LONG_USER)] ?: -1
				val id = prefs[intPreferencesKey(PREFERENCE_KEY_INT_ELEMENT_ID)] ?: -1
				val type = prefs[stringPreferencesKey(PREFERENCE_KEY_STRING_ELEMENT_TYPE)] ?: ""

				val user = userDatabase.userDao().getById(userId)

				user?.let {
					try {
						val timetable = loadTimetable(
							user,
							TimetableDatabaseInterface(userDatabase, user.id),
							id to type
						)

						val timetableItems =
							timetable.items.sortedBy { it.startDateTime.toString() }

						val timetableListItems = timetableItems
							.mapIndexed { index, item ->
								val sameTimeAsPrevious =
									timetableItems.getOrNull(index - 1)?.startDateTime == item.startDateTime
								val sameTimeAsNext =
									timetableItems.getOrNull(index + 1)?.startDateTime == item.startDateTime

								WidgetListItemModel(
									headlineContent = item.periodData.getLong(
										TimetableDatabaseInterface.Type.SUBJECT
									),
									supportingContent = arrayOf(item.top, item.bottom)
										.filter { s -> s.isNotBlank() }
										.joinToString(" - "),
									leadingContent = { surfaceColor, textColor ->
										Column(
											horizontalAlignment = Alignment.CenterHorizontally,
											modifier = GlanceModifier
												.fillMaxHeight()
										) {
											Box(
												modifier = GlanceModifier
													.width(if (sameTimeAsPrevious) 4.dp else 0.dp)
													.defaultWeight()
													.background(textColor)
											) {}
											Box(
												modifier = GlanceModifier
													.width(if (sameTimeAsNext) 4.dp else 0.dp)
													.defaultWeight()
													.background(textColor)
											) {}
										}

										Column(
											modifier = GlanceModifier
												.wrapContentSize()
												.background(surfaceColor)
										) {
											Text(
												item.startDateTime.toString(timeFormatter),
												style = MaterialTheme.typography.bodySmall.toGlanceTextStyle(
													textColor
												)
											)
											Text(
												item.endDateTime.toString(timeFormatter),
												style = MaterialTheme.typography.bodySmall.toGlanceTextStyle(
													textColor
												)
											)
										}
									}
								)
							}

						TimetableWidget().run {
							setData(timetableListItems)
							update(applicationContext, glanceId)
						}
					} catch (e: Exception) {
						//setError(e) TODO
						Log.e(LOG_TAG, "Timetable loading error", e)
					}
				}
			}

		return Result.success()
	}
}
