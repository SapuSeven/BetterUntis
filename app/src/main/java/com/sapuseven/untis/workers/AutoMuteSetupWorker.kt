package com.sapuseven.untis.workers

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.helpers.config.booleanDataStore
import com.sapuseven.untis.helpers.config.intDataStore
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.receivers.AutoMuteReceiver
import com.sapuseven.untis.receivers.AutoMuteReceiver.Companion.EXTRA_BOOLEAN_MUTE
import com.sapuseven.untis.receivers.AutoMuteReceiver.Companion.EXTRA_INT_ID
import com.sapuseven.untis.receivers.AutoMuteReceiver.Companion.EXTRA_LONG_USER_ID
import com.sapuseven.untis.workers.DailyWorker.Companion.WORKER_DATA_USER_ID
import org.joda.time.LocalDateTime

class AutoMuteSetupWorker(context: Context, params: WorkerParameters) :
	TimetableDependantWorker(context, params) {
	companion object {
		private const val LOG_TAG = "AutoMuteSetup"
		private const val TAG_AUTO_MUTE_SETUP_WORK = "AutoMuteSetupWork"

		fun enqueue(workManager: WorkManager, user: UserDatabase.User) {
			val data: Data = Data.Builder().run {
				put(WORKER_DATA_USER_ID, user.id)
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
		return scheduleAutoMute()
	}

	private suspend fun scheduleAutoMute(): Result {
		val userDatabase = UserDatabase.createInstance(applicationContext)

		userDatabase.getUser(inputData.getLong(WORKER_DATA_USER_ID, -1))?.let { user ->
			try {
				val personalTimetable = loadPersonalTimetableElement(user)
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
								"${item.periodData.getShort(TimetableDatabaseInterface.Type.SUBJECT)} mute scheduled for ${item.startDateTime}"
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
								"${item.periodData.getShort(TimetableDatabaseInterface.Type.SUBJECT)} unmute scheduled for ${item.endDateTime}"
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
