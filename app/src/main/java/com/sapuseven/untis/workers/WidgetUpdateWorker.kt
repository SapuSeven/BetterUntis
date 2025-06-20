package com.sapuseven.untis.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.sapuseven.untis.data.repository.TimetableRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * This worker loads the data for widgets.
 */
@HiltWorker
class WidgetUpdateWorker @AssistedInject constructor(
	@Assisted context: Context,
	@Assisted params: WorkerParameters,
	timetableRepository: TimetableRepository,
) :
	TimetableDependantWorker(context, params, timetableRepository) {
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
		/*val userDatabase = UserDatabase.getInstance(applicationContext)

		val timeFormatter = DateTimeFormat.forPattern("HH:mm")

		GlanceAppWidgetManager(applicationContext).getGlanceIds(TimetableWidget::class.java)
			.forEach { glanceId ->
				val prefs =
					TimetableWidget().getAppWidgetState<Preferences>(applicationContext, glanceId)

				val userId = prefs[longPreferencesKey(PREFERENCE_KEY_LONG_USER)] ?: -1
				val id = prefs[intPreferencesKey(PREFERENCE_KEY_INT_ELEMENT_ID)] ?: -1
				val type = prefs[stringPreferencesKey(PREFERENCE_KEY_STRING_ELEMENT_TYPE)] ?: ""

				val user = userDatabase.userDao().getById(userId)

				/*user?.let {
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
										ElementType.SUBJECT
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
				}*/
			}*/

		return Result.success()
	}
}
