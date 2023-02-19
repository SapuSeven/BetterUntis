package com.sapuseven.untis.workers

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.room.Room
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.sapuseven.untis.BuildConfig
import com.sapuseven.untis.R
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.data.databases.entities.User
import com.sapuseven.untis.data.timetable.TimegridItem
import com.sapuseven.untis.helpers.DateTimeUtils
import com.sapuseven.untis.helpers.config.booleanDataStore
import com.sapuseven.untis.helpers.config.intDataStore
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.receivers.NotificationReceiver
import com.sapuseven.untis.receivers.NotificationReceiver.Companion.EXTRA_BOOLEAN_CLEAR
import com.sapuseven.untis.receivers.NotificationReceiver.Companion.EXTRA_BOOLEAN_FIRST
import com.sapuseven.untis.receivers.NotificationReceiver.Companion.EXTRA_INT_BREAK_END_TIME
import com.sapuseven.untis.receivers.NotificationReceiver.Companion.EXTRA_INT_ID
import com.sapuseven.untis.receivers.NotificationReceiver.Companion.EXTRA_LONG_USER_ID
import com.sapuseven.untis.receivers.NotificationReceiver.Companion.EXTRA_STRING_BREAK_END_TIME
import com.sapuseven.untis.receivers.NotificationReceiver.Companion.EXTRA_STRING_NEXT_CLASS
import com.sapuseven.untis.receivers.NotificationReceiver.Companion.EXTRA_STRING_NEXT_CLASS_LONG
import com.sapuseven.untis.receivers.NotificationReceiver.Companion.EXTRA_STRING_NEXT_ROOM
import com.sapuseven.untis.receivers.NotificationReceiver.Companion.EXTRA_STRING_NEXT_ROOM_LONG
import com.sapuseven.untis.receivers.NotificationReceiver.Companion.EXTRA_STRING_NEXT_SUBJECT
import com.sapuseven.untis.receivers.NotificationReceiver.Companion.EXTRA_STRING_NEXT_SUBJECT_LONG
import com.sapuseven.untis.receivers.NotificationReceiver.Companion.EXTRA_STRING_NEXT_TEACHER
import com.sapuseven.untis.receivers.NotificationReceiver.Companion.EXTRA_STRING_NEXT_TEACHER_LONG
import com.sapuseven.untis.workers.DailyWorker.Companion.WORKER_DATA_USER_ID
import org.joda.time.DateTime
import org.joda.time.LocalDateTime

/**
 * This worker schedules all break info notifications for the day.
 */
class NotificationSetupWorker(context: Context, params: WorkerParameters) :
	TimetableDependantWorker(context, params) {
	companion object {
		private const val LOG_TAG = "NotificationSetup"
		private const val TAG_NOTIFICATION_SETUP_WORK = "NotificationSetupWork"

		const val CHANNEL_ID_DEBUG = "notifications.debug"
		const val CHANNEL_ID_BACKGROUNDERRORS = "notifications.backgrounderrors"
		const val CHANNEL_ID_BREAKINFO = "notifications.breakinfo"

		fun enqueue(workManager: WorkManager, user: User) {
			val data: Data = Data.Builder().run {
				put(WORKER_DATA_USER_ID, user.id)
				build()
			}

			workManager.enqueue(
				OneTimeWorkRequestBuilder<NotificationSetupWorker>()
					.addTag(TAG_NOTIFICATION_SETUP_WORK)
					.setInputData(data)
					.build()
			)
		}
	}

	override suspend fun doWork(): Result {
		setupNotificationChannels()
		return checkAlarmPermission() ?: scheduleNotifications()
	}

	private suspend fun checkAlarmPermission(): Result? {
		if (canPostNotifications() && canScheduleExactAlarms()) return null

		disablePreference("preference_notifications_enable")
		Log.w(LOG_TAG, "Schedule exact alarm permission revoked, disabling notifications")
		return Result.failure()
	}

	private suspend fun scheduleNotifications(): Result {
		val userDatabase = Room.databaseBuilder(
			applicationContext,
			UserDatabase::class.java, "users"
		).build()

		userDatabase.userDao().getById(inputData.getLong(WORKER_DATA_USER_ID, -1))?.let { user ->
			var scheduledNotifications = 0

			try {
				val personalTimetable = loadPersonalTimetableElement(user, applicationContext)
					?: return@let // Anonymous / no custom personal timetable

				val timetable = loadTimetable(
					user,
					TimetableDatabaseInterface(userDatabase, user.id),
					personalTimetable
				)

				val notificationsBeforeFirst = applicationContext.booleanDataStore(
					user.id,
					"preference_notifications_before_first"
				).getValue()

				val notificationsBeforeFirstTime = applicationContext.intDataStore(
					user.id,
					"preference_notifications_before_first_time"
				).getValue()

				val notificationsInMultiple = applicationContext.booleanDataStore(
					user.id,
					"preference_notifications_in_multiple"
				).getValue()

				val preparedItems = timetable.items.filter { !it.periodData.isCancelled() }
					.sortedBy { it.startDateTime }.merged().zipWithNext()

				if (preparedItems.isEmpty()) {
					Log.d(LOG_TAG, "No notifications to schedule")
					return Result.success()
				}

				with(preparedItems.first().first) {
					if (startDateTime.millisOfDay < LocalDateTime.now().millisOfDay) return@with

					if (notificationsBeforeFirst && preparedItems.isNotEmpty()) {
						scheduleNotification(
							applicationContext,
							user.id,
							startDateTime.minusMinutes(notificationsBeforeFirstTime),
							this,
							true
						)
						scheduledNotifications++
					} else {
						clearNotification(
							applicationContext,
							startDateTime.minusMinutes(notificationsBeforeFirstTime)
						)
					}
				}

				preparedItems.forEach { item ->
					if (item.first.endDateTime == item.second.startDateTime)
						return@forEach // No break exists

					if (item.first.equalsIgnoreTime(item.second) && !notificationsInMultiple) {
						clearNotification(
							applicationContext,
							item.first.endDateTime
						)
						return@forEach // multi-hour lesson
					}

					if (item.second.startDateTime.millisOfDay < LocalDateTime.now().millisOfDay)
						return@forEach // lesson is in the past

					scheduleNotification(
						applicationContext,
						user.id,
						item.first.endDateTime,
						item.second
					)
					scheduledNotifications++
				}
			} catch (e: Exception) {
				Log.e(LOG_TAG, "Notifications couldn't be scheduled", e)
				return Result.failure()
			}
		}

		return Result.success()
	}

	private fun scheduleNotification(
		context: Context,
		userId: Long,
		notificationTime: DateTime,
		notificationEndLesson: TimegridItem,
		isFirst: Boolean = false
	) {
		val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
		val id = notificationTime.millisOfDay / 1000 // generate a unique id

		val intent = Intent(context, NotificationReceiver::class.java)
			.putExtra(EXTRA_INT_ID, id)
			.putExtra(EXTRA_LONG_USER_ID, userId)
			.putExtra(EXTRA_INT_BREAK_END_TIME, notificationEndLesson.startDateTime.millisOfDay)
			.putExtra(
				EXTRA_STRING_BREAK_END_TIME,
				notificationEndLesson.startDateTime.toString(DateTimeUtils.shortDisplayableTime())
			)
			.putExtra(
				EXTRA_STRING_NEXT_SUBJECT,
				notificationEndLesson.periodData.getShort(TimetableDatabaseInterface.Type.SUBJECT)
			)
			.putExtra(
				EXTRA_STRING_NEXT_SUBJECT_LONG,
				notificationEndLesson.periodData.getLong(TimetableDatabaseInterface.Type.SUBJECT)
			)
			.putExtra(
				EXTRA_STRING_NEXT_ROOM,
				notificationEndLesson.periodData.getShort(TimetableDatabaseInterface.Type.ROOM)
			)
			.putExtra(
				EXTRA_STRING_NEXT_ROOM_LONG,
				notificationEndLesson.periodData.getLong(TimetableDatabaseInterface.Type.ROOM)
			)
			.putExtra(
				EXTRA_STRING_NEXT_TEACHER,
				notificationEndLesson.periodData.getShort(TimetableDatabaseInterface.Type.TEACHER)
			)
			.putExtra(
				EXTRA_STRING_NEXT_TEACHER_LONG,
				notificationEndLesson.periodData.getLong(TimetableDatabaseInterface.Type.TEACHER)
			)
			.putExtra(
				EXTRA_STRING_NEXT_CLASS,
				notificationEndLesson.periodData.getShort(TimetableDatabaseInterface.Type.CLASS)
			)
			.putExtra(
				EXTRA_STRING_NEXT_CLASS_LONG,
				notificationEndLesson.periodData.getLong(TimetableDatabaseInterface.Type.CLASS)
			)

		if (isFirst) intent.putExtra(EXTRA_BOOLEAN_FIRST, true)

		val pendingIntent = PendingIntent.getBroadcast(
			context,
			notificationTime.millisOfDay,
			intent,
			FLAG_IMMUTABLE
		)
		alarmManager.setExact(AlarmManager.RTC_WAKEUP, notificationTime.millis, pendingIntent)
		Log.d(
			LOG_TAG,
			"${notificationEndLesson.periodData.getShort(TimetableDatabaseInterface.Type.SUBJECT)} scheduled for $notificationTime"
		)

		val deletingIntent = Intent(context, NotificationReceiver::class.java)
			.putExtra(EXTRA_INT_ID, id)
			.putExtra(EXTRA_BOOLEAN_CLEAR, true)
		val deletingPendingIntent = PendingIntent.getBroadcast(
			context,
			notificationTime.millisOfDay + 1, // Different id to previous intent
			deletingIntent,
			FLAG_IMMUTABLE
		)
		alarmManager.setExact(
			AlarmManager.RTC_WAKEUP,
			notificationEndLesson.startDateTime.millis,
			deletingPendingIntent
		)
		Log.d(
			LOG_TAG,
			"${notificationEndLesson.periodData.getShort(TimetableDatabaseInterface.Type.SUBJECT)} delete scheduled for ${notificationEndLesson.startDateTime}"
		)
	}

	private fun clearNotification(
		context: Context,
		notificationTime: DateTime,
	) {
		(context.getSystemService(ALARM_SERVICE) as AlarmManager).run {
			cancel(
				PendingIntent.getBroadcast(
					context,
					notificationTime.millisOfDay,
					Intent(context, NotificationReceiver::class.java),
					FLAG_IMMUTABLE
				)
			)
		}
	}

	private fun setupNotificationChannels() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val notificationManager: NotificationManager =
				applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

			listOfNotNull(
				if (BuildConfig.DEBUG)
					NotificationChannel(
						CHANNEL_ID_DEBUG,
						"Debug",
						NotificationManager.IMPORTANCE_DEFAULT
					).apply {
						description = "Notifications for debugging"
					}
				else null,
				NotificationChannel(
					CHANNEL_ID_BACKGROUNDERRORS,
					applicationContext.getString(R.string.notifications_channel_backgrounderrors),
					NotificationManager.IMPORTANCE_MIN
				).apply {
					description =
						applicationContext.getString(R.string.notifications_channel_backgrounderrors_desc)
				},
				NotificationChannel(
					CHANNEL_ID_BREAKINFO,
					applicationContext.getString(R.string.notifications_channel_breakinfo),
					NotificationManager.IMPORTANCE_LOW
				).apply {
					description =
						applicationContext.getString(R.string.notifications_channel_breakinfo_desc)
				},
			).forEach {
				notificationManager.createNotificationChannel(it)
			}
		}
	}
}
