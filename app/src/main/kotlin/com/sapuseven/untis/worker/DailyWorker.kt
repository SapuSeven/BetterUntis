package com.sapuseven.untis.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import com.sapuseven.untis.core.datastore.UserSettingsDataSource
import com.sapuseven.untis.core.domain.cache.FromCache
import com.sapuseven.untis.core.domain.repository.TimetableRepository
import com.sapuseven.untis.core.domain.repository.UserRepository
import com.sapuseven.untis.core.domain.worker.TimetableHandler
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.todayIn
import java.util.concurrent.TimeUnit


/**
 * This worker fetches the personal timetable for all users and calls all registered handlers with the result.
 * It is scheduled to run once a day, but can also be used for manual refreshes.
 */
@HiltWorker
class DailyWorker @AssistedInject constructor(
	@Assisted context: Context,
	@Assisted params: WorkerParameters,
	private val userRepository: UserRepository,
	private val userSettingsDataSource: UserSettingsDataSource,
	private val timetableRepository: TimetableRepository,
	private val handlers: @JvmSuppressWildcards Set<TimetableHandler>,
	private val clock: Clock = Clock.System,
	private val zone: TimeZone = TimeZone.currentSystemDefault(),
) : CoroutineWorker(context, params) {
	companion object {
		const val TAG_DAILY_WORK = "DailyWork"

		private fun nextWorkRequest(clock: Clock, zone: TimeZone, hourOfDay: Int = 2): WorkRequest {
			val now = clock.now()
			val dueInstant = clock.todayIn(zone).atTime(hourOfDay, 0).toInstant(zone)
			val targetInstant =
				dueInstant.takeIf { it > now } ?: dueInstant.plus(1, DateTimeUnit.DAY, zone)

			return OneTimeWorkRequestBuilder<DailyWorker>()
				.setInitialDelay((targetInstant - now).inWholeSeconds, TimeUnit.SECONDS)
				.addTag(TAG_DAILY_WORK)
				.build()
		}

		fun enqueueNext(context: Context, clock: Clock, zone: TimeZone) {
			WorkManager.getInstance(context).enqueue(nextWorkRequest(clock, zone))
		}
	}

	override suspend fun doWork(): Result {
		val today = clock.todayIn(zone)

		userRepository.observeAllUsers().first().forEach { user ->
			val userSettings = userSettingsDataSource.getSettings(user.id).first()
			val element = user.element // TODO: Honor personal timetable setting
				?: return@forEach // Anonymous / no custom personal timetable

			try {
				val timetable = timetableRepository.getTimetable(
					user,
					TimetableRepository.TimetableParams(
						element.id,
						element.type,
						today,
						today
					),
					FromCache.NEVER
				).firstOrNull() ?: return@forEach

				handlers.forEach { handler ->
					if (handler.isEnabled(user)) {
						try {
							handler.onNewTimetable(user, timetable)
						} catch (e: Exception) {
							Log.e(
								TAG_DAILY_WORK,
								"Handler ${handler::class.simpleName} failed for user ${user.id}",
								e
							)
						}
					}
				}
			} catch (e: Exception) {
				Log.e(TAG_DAILY_WORK, "Timetable loading failed for user ${user.id}", e)
			}
		}

		enqueueNext(applicationContext, clock, zone)
		return Result.success()
	}
}
