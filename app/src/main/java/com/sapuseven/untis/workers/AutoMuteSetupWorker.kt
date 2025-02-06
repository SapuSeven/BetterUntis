package com.sapuseven.untis.workers

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.sapuseven.untis.data.database.entities.User


class AutoMuteSetupWorker(context: Context, params: WorkerParameters) :
	TimetableDependantWorker(context, params) {
	companion object {
		private const val LOG_TAG = "AutoMuteSetup"
		private const val TAG_AUTO_MUTE_SETUP_WORK = "AutoMuteSetupWork"

		fun enqueue(workManager: WorkManager, user: User) {
			val data: Data = Data.Builder().run {
				//put(WORKER_DATA_USER_ID, user.id)
				build()
			}

			workManager.enqueue(
				OneTimeWorkRequestBuilder<AutoMuteSetupWorker>()
					.addTag(TAG_AUTO_MUTE_SETUP_WORK)
					.setInputData(data)
					.build()
			)
		}
	}

	override suspend fun doWork(): Result {
		return checkAlarmPermission() ?: scheduleAutoMute()
	}

	private suspend fun checkAlarmPermission(): Result? {
		if (canAutoMute() && canScheduleExactAlarms()) return null

		disablePreference("preference_automute_enable")
		Log.w(LOG_TAG, "Schedule exact alarm permission revoked, disabling auto mute")
		return Result.failure()
	}

	private suspend fun scheduleAutoMute(): Result {
		/*val userDatabase = UserDatabase.getInstance(applicationContext)

		userDatabase.userDao().getById(inputData.getLong(WORKER_DATA_USER_ID, -1))?.let { user ->
			try {
				val personalTimetable = loadPersonalTimetableElement(user, applicationContext)
					?: return@let // Anonymous / no custom personal timetable

				val timetable = loadTimetable(
					user,
					TimetableDatabaseInterface(userDatabase, user.id),
					personalTimetable
				)

				val automuteCancelledLessons = applicationContext.booleanDataStore(
					user.id,
					"preference_automute_cancelled_lessons"
				).getValue()

				val automuteMinimumBreakLength = applicationContext.intDataStore(
					user.id,
					"preference_automute_minimum_break_length"
				).getValue()

				timetable.items.merged().sortedBy { it.startDateTime }.zipWithNext().withLast()
					.forEach {
						it.first?.let { item ->
							val alarmManager =
								applicationContext.getSystemService(ALARM_SERVICE) as AlarmManager
							val id = item.startDateTime.millisOfDay / 1000

							if (item.endDateTime.millisOfDay <= LocalDateTime.now().millisOfDay)
								return@forEach // lesson is in the past

							if (item.periodData.isCancelled() && !automuteCancelledLessons)
								return@forEach // lesson is cancelled

							val muteIntent =
								Intent(applicationContext, AutoMuteReceiver::class.java)
									.putExtra(EXTRA_INT_ID, id)
									.putExtra(EXTRA_LONG_USER_ID, user.id)
									.putExtra(EXTRA_BOOLEAN_MUTE, true)
							val pendingMuteIntent = PendingIntent.getBroadcast(
								applicationContext,
								item.startDateTime.millisOfDay,
								muteIntent,
								FLAG_IMMUTABLE
							)
							alarmManager.setExact(
								AlarmManager.RTC_WAKEUP,
								item.startDateTime.millis,
								pendingMuteIntent
							)
							Log.d(
								LOG_TAG,
								"${item.periodData.getShort(ElementType.SUBJECT)} mute scheduled for ${item.startDateTime}"
							)

							val minimumBreakLengthMillis = automuteMinimumBreakLength * 60 * 1000
							if (it.second != null
								&& it.second!!.startDateTime.millisOfDay - item.endDateTime.millisOfDay < minimumBreakLengthMillis
							)
								return@forEach // Break to next element is too short
							val unmuteIntent =
								Intent(applicationContext, AutoMuteReceiver::class.java)
									.putExtra(EXTRA_INT_ID, id)
									.putExtra(EXTRA_LONG_USER_ID, user.id)
									.putExtra(EXTRA_BOOLEAN_MUTE, false)
							val pendingUnmuteIntent = PendingIntent.getBroadcast(
								applicationContext,
								item.endDateTime.millisOfDay,
								unmuteIntent,
								FLAG_IMMUTABLE
							)
							alarmManager.setExact(
								AlarmManager.RTC_WAKEUP,
								item.endDateTime.millis,
								pendingUnmuteIntent
							)
							Log.d(
								"AutoMuteSetup",
								"${item.periodData.getShort(ElementType.SUBJECT)} unmute scheduled for ${item.endDateTime}"
							)
						}
					}
			} catch (e: Exception) {
				Log.e(LOG_TAG, "Auto mute events couldn't be scheduled", e)
				return Result.failure()
			}
		}*/

		return Result.success()
	}
}
