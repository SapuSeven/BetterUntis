package com.sapuseven.untis.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import com.sapuseven.untis.data.database.entities.UserDao
import com.sapuseven.untis.data.repository.TimetableRepository
import com.sapuseven.untis.ui.pages.settings.UserSettingsRepository
import crocodile8.universal_cache.FromCache
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import org.joda.time.LocalDateTime
import org.joda.time.Seconds
import java.util.concurrent.TimeUnit

/**
 * This worker caches the personal timetable if it exists and starts all other daily workers
 * which can then use the cached timetable.
 */
@HiltWorker
class DailyWorker @AssistedInject constructor(
	@Assisted context: Context,
	@Assisted params: WorkerParameters,
	timetableRepository: TimetableRepository,
	private val userDao: UserDao,
	private val settingsRepository: UserSettingsRepository,
) : TimetableDependantWorker(context, params, timetableRepository) {
	companion object {
		const val TAG_DAILY_WORK = "DailyWork"

		private fun nextWorkRequest(hourOfDay: Int = 2): WorkRequest {
			val currentTime = LocalDateTime.now()
			var dueTime = currentTime.withMillisOfDay(hourOfDay * 60 * 60 * 1000)

			if (dueTime <= currentTime)
				dueTime = dueTime.plusDays(1)

			return OneTimeWorkRequestBuilder<DailyWorker>()
				.setInitialDelay(
					Seconds.secondsBetween(currentTime, dueTime).seconds.toLong(),
					TimeUnit.SECONDS
				)
				.addTag(TAG_DAILY_WORK)
				.build()
		}

		fun enqueueNext(context: Context) {
			WorkManager.getInstance(context).enqueue(nextWorkRequest())
		}
	}

	override suspend fun doWork(): Result {
		val workManager = WorkManager.getInstance(applicationContext)

		val settings = settingsRepository.getAllSettings().first()
		userDao.getAllFlow().first().forEach { user ->
			val userSettings = settings.userSettingsMap.getOrDefault(user.id, settingsRepository.getSettingsDefaults())
			val personalTimetable = getPersonalTimetableElement(user, userSettings)
				?: return@forEach // Anonymous / no custom personal timetable

			try {
				// Load timetable to cache
				loadTimetable(
					user,
					personalTimetable,
					FromCache.NEVER
				)

				workManager.let {
					if (userSettings.notificationsEnable)
						NotificationSetupWorker.enqueue(it, user)

					if (userSettings.automuteEnable)
						AutoMuteSetupWorker.enqueue(it, user)
				}
			} catch (e: Exception) {
				Log.e("DailyWorker", "Timetable loading error", e)
			}
		}

		WidgetUpdateWorker.enqueue(workManager)

		enqueueNext(applicationContext)
		return Result.success()
	}
}
