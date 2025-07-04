package com.sapuseven.untis.workers

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import com.sapuseven.untis.data.database.entities.User
import com.sapuseven.untis.data.database.entities.UserDao
import com.sapuseven.untis.data.repository.MasterDataRepository
import com.sapuseven.untis.data.repository.TimetableRepository
import com.sapuseven.untis.data.repository.UserSettingsRepository
import com.sapuseven.untis.mappers.TimetableMapper
import com.sapuseven.untis.receivers.AutoMuteReceiver
import com.sapuseven.untis.receivers.AutoMuteReceiver.Companion.EXTRA_BOOLEAN_MUTE
import com.sapuseven.untis.receivers.AutoMuteReceiver.Companion.EXTRA_INT_ID
import com.sapuseven.untis.receivers.AutoMuteReceiver.Companion.EXTRA_LONG_USER_ID
import crocodile8.universal_cache.FromCache
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * This worker schedules all auto-mute events for the day.
 */
@HiltWorker
class AutoMuteSetupWorker @AssistedInject constructor(
	private val userSettingsRepository: UserSettingsRepository,
	private val timetableMapper: TimetableMapper,
	private val masterDataRepository: MasterDataRepository,
	private val userDao: UserDao,
	@Assisted context: Context,
	@Assisted params: WorkerParameters,
	timetableRepository: TimetableRepository,
) : TimetableDependantWorker(context, params, timetableRepository) {
	companion object {
		private const val LOG_TAG = "AutoMuteSetup"
		private const val TAG_AUTO_MUTE_SETUP_WORK = "AutoMuteSetupWork"
		private const val WORKER_DATA_USER_ID = "UserId"

		fun enqueue(workManager: WorkManager, user: User) {
			workManager.enqueue(
				OneTimeWorkRequestBuilder<AutoMuteSetupWorker>()
					.addTag(TAG_AUTO_MUTE_SETUP_WORK)
					.setInputData(
						workDataOf(
							WORKER_DATA_USER_ID to user.id
						)
					)
					.build()
			)
		}
	}

	override suspend fun doWork(): Result {
		return checkAlarmPermission() ?: scheduleAutoMute()
	}

	private fun checkAlarmPermission(): Result? {
		if (!canAutoMute()) return Result.failure()

		return null
	}

	private suspend fun scheduleAutoMute(): Result {
		val userId = inputData.getLong(WORKER_DATA_USER_ID, -1)
		val userSettings = userSettingsRepository.getSettings(userId).first()

		userDao.getByIdAsync(userId)?.let { user ->
			try {
				val personalTimetable = getPersonalTimetableElement(user, userSettings)
					?: return@let // Anonymous and no custom personal timetable

				val timetable = timetableMapper.preparePeriods(loadTimetable(
					user,
					personalTimetable,
					FromCache.ONLY
				), !userSettings.automuteCancelledLessons)

				timetable.merged(masterDataRepository).sortedBy { it.originalPeriod.startDateTime }.zipWithNext().withLast()
					.forEach {
						it.first?.let { item ->
							val alarmManager =
								applicationContext.getSystemService(ALARM_SERVICE) as AlarmManager
							val id = item.originalPeriod.startDateTime.toLocalTime().toSecondOfDay()

							if (item.originalPeriod.endDateTime.isBefore(LocalDateTime.now()))
								return@forEach // lesson is in the past

							if (item.isCancelled() && !userSettings.automuteCancelledLessons)
								return@forEach // lesson is cancelled

							val muteIntent =
								Intent(applicationContext, AutoMuteReceiver::class.java)
									.putExtra(EXTRA_INT_ID, id)
									.putExtra(EXTRA_LONG_USER_ID, user.id)
									.putExtra(EXTRA_BOOLEAN_MUTE, true)
							val pendingMuteIntent = PendingIntent.getBroadcast(
								applicationContext,
								item.originalPeriod.startDateTime.toLocalTime().toSecondOfDay(),
								muteIntent,
								FLAG_IMMUTABLE
							)
							alarmManager.setBest(
								item.originalPeriod.startDateTime,
								pendingMuteIntent
							)
							Log.d(
								LOG_TAG,
								"${item.getShort(ElementType.SUBJECT)} mute scheduled for ${item.originalPeriod.startDateTime}"
							)

							val subsequentBreakLength = it.second?.let { subsequentPeriod ->
								ChronoUnit.MINUTES.between(item.originalPeriod.endDateTime, subsequentPeriod.originalPeriod.startDateTime)
							}
							if (subsequentBreakLength != null && subsequentBreakLength < userSettings.automuteMinimumBreakLength) {
								Log.d(
									"AutoMuteSetup",
									"${item.getShort(ElementType.SUBJECT)} unmute NOT scheduled for ${item.originalPeriod.endDateTime} - subsequent break too short (${subsequentBreakLength} min)"
								)
								return@forEach
							}

							val unmuteIntent =
								Intent(applicationContext, AutoMuteReceiver::class.java)
									.putExtra(EXTRA_INT_ID, id)
									.putExtra(EXTRA_LONG_USER_ID, user.id)
									.putExtra(EXTRA_BOOLEAN_MUTE, false)
							val pendingUnmuteIntent = PendingIntent.getBroadcast(
								applicationContext,
								item.originalPeriod.endDateTime.toLocalTime().toSecondOfDay(),
								unmuteIntent,
								FLAG_IMMUTABLE
							)
							alarmManager.setBest(
								item.originalPeriod.endDateTime,
								pendingUnmuteIntent
							)
							Log.d(
								"AutoMuteSetup",
								"${item.getShort(ElementType.SUBJECT)} unmute scheduled for ${item.originalPeriod.endDateTime}"
							)
						}
					}
			} catch (e: Exception) {
				Log.e(LOG_TAG, "Auto mute events couldn't be scheduled", e)
				return Result.failure()
			}
		}

		return Result.success()
	}
}
