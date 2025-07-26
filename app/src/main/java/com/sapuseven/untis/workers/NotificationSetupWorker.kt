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
import androidx.hilt.work.HiltWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.sapuseven.untis.BuildConfig
import com.sapuseven.untis.R
import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import com.sapuseven.untis.persistence.entity.User
import com.sapuseven.untis.persistence.entity.UserDao
import com.sapuseven.untis.data.repository.MasterDataRepository
import com.sapuseven.untis.data.repository.TimetableRepository
import com.sapuseven.untis.data.repository.UserSettingsRepository
import com.sapuseven.untis.mappers.TimetableMapper
import com.sapuseven.untis.models.PeriodItem
import com.sapuseven.untis.models.equalsIgnoreTime
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
import crocodile8.universal_cache.FromCache
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.Clock
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * This worker schedules all break info notifications for the day.
 */
@HiltWorker
class NotificationSetupWorker @AssistedInject constructor(
	private val userSettingsRepository: UserSettingsRepository,
	private val masterDataRepository: MasterDataRepository,
	private val timetableMapper: TimetableMapper,
	private val clock: Clock,
	private val userDao: UserDao,
	@Assisted context: Context,
	@Assisted params: WorkerParameters,
	timetableRepository: TimetableRepository,
) : TimetableDependantWorker(context, params, timetableRepository) {
	companion object {
		private const val LOG_TAG = "NotificationSetup"
		private const val TAG_NOTIFICATION_SETUP_WORK = "NotificationSetupWork"
		private const val WORKER_DATA_USER_ID = "UserId"

		const val CHANNEL_ID_DEBUG = "notifications.debug"
		const val CHANNEL_ID_BACKGROUNDERRORS = "notifications.backgrounderrors"
		const val CHANNEL_ID_BREAKINFO = "notifications.breakinfo"
		const val CHANNEL_ID_FIRSTLESSON = "notifications.firstlesson"

		fun enqueue(workManager: WorkManager, user: User) {
			workManager.enqueue(
				OneTimeWorkRequestBuilder<NotificationSetupWorker>()
					.addTag(TAG_NOTIFICATION_SETUP_WORK)
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
		setupNotificationChannels()
		return checkAlarmPermission() ?: scheduleNotifications()
	}

	private fun checkAlarmPermission(): Result? {
		if (!canPostNotifications()) return Result.failure()

		return null
	}

	private suspend fun scheduleNotifications(): Result {
		val userId = inputData.getLong(WORKER_DATA_USER_ID, -1)
		val userSettings = userSettingsRepository.getSettings(userId).first()

		userDao.getByIdAsync(userId)?.let { user ->
			var scheduledNotifications = 0

			try {
				val personalTimetable = getPersonalTimetableElement(user, userSettings)
					?: return@let // Anonymous and no custom personal timetable

				val timetable = timetableMapper.preparePeriods(loadTimetable(
					user,
					personalTimetable,
					FromCache.ONLY
				), false)

				val preparedItems = timetable
					.sortedBy { it.startDateTime }
					.merged(masterDataRepository)
					.filter { !it.isCancelled() }
					.zipWithNext()

				if (preparedItems.isEmpty()) {
					Log.d(LOG_TAG, "No notifications to schedule")
					return Result.success()
				}

				with(preparedItems.first().first) {
					if (originalPeriod.startDateTime.isBefore(LocalDateTime.now(clock))) return@with

					if (userSettings.notificationsBeforeFirst && preparedItems.isNotEmpty()) {
						scheduleNotification(
							applicationContext,
							user.id,
							originalPeriod.startDateTime.minusMinutes(userSettings.notificationsBeforeFirstTime.toLong()),
							this,
							true
						)
						scheduledNotifications++
					} else {
						clearNotification(
							applicationContext,
							originalPeriod.startDateTime.minusMinutes(userSettings.notificationsBeforeFirstTime.toLong())
						)
					}
				}

				preparedItems.forEach { item ->
					if (item.first.originalPeriod.endDateTime == item.second.originalPeriod.startDateTime)
						return@forEach // No break exists

					if (item.first.equalsIgnoreTime(item.second) && !userSettings.notificationsInMultiple) {
						clearNotification(
							applicationContext,
							item.first.originalPeriod.endDateTime
						)
						return@forEach // multi-hour lesson
					}

					if (item.second.originalPeriod.startDateTime.isBefore(LocalDateTime.now()))
						return@forEach // lesson is in the past

					scheduleNotification(
						applicationContext,
						user.id,
						item.first.originalPeriod.endDateTime,
						item.second
					)
					scheduledNotifications++
				}
			} catch (e: Exception) {
				Log.e(LOG_TAG, "Notifications couldn't be scheduled", e)
				return Result.failure()
			}

			Log.d(LOG_TAG, "Scheduled $scheduledNotifications notifications for today.")
		}

		return Result.success()
	}

	private fun scheduleNotification(
		context: Context,
		userId: Long,
		notificationTime: LocalDateTime,
		notificationEndPeriodItem: PeriodItem,
		isFirst: Boolean = false
	) {
		val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
		val id = notificationTime.toLocalTime().second // generate a unique id

		// TODO: Include state (cancelled, irregular etc)
		val intent = Intent(context, NotificationReceiver::class.java)
			.putExtra(EXTRA_INT_ID, id)
			.putExtra(EXTRA_LONG_USER_ID, userId)
			.putExtra(
				EXTRA_INT_BREAK_END_TIME,
				notificationEndPeriodItem.originalPeriod.startDateTime.toLocalTime().toSecondOfDay()
			)
			.putExtra(
				EXTRA_STRING_BREAK_END_TIME,
				notificationEndPeriodItem.originalPeriod.startDateTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
			)
			.putExtra(
				EXTRA_STRING_NEXT_SUBJECT,
				notificationEndPeriodItem.getShort(ElementType.SUBJECT)
			)
			.putExtra(
				EXTRA_STRING_NEXT_SUBJECT_LONG,
				notificationEndPeriodItem.getLong(ElementType.SUBJECT)
			)
			.putExtra(
				EXTRA_STRING_NEXT_ROOM,
				notificationEndPeriodItem.getShort(ElementType.ROOM)
			)
			.putExtra(
				EXTRA_STRING_NEXT_ROOM_LONG,
				notificationEndPeriodItem.getLong(ElementType.ROOM)
			)
			.putExtra(
				EXTRA_STRING_NEXT_TEACHER,
				notificationEndPeriodItem.getShort(ElementType.TEACHER)
			)
			.putExtra(
				EXTRA_STRING_NEXT_TEACHER_LONG,
				notificationEndPeriodItem.getLong(ElementType.TEACHER)
			)
			.putExtra(
				EXTRA_STRING_NEXT_CLASS,
				notificationEndPeriodItem.getShort(ElementType.CLASS)
			)
			.putExtra(
				EXTRA_STRING_NEXT_CLASS_LONG,
				notificationEndPeriodItem.getLong(ElementType.CLASS)
			)

		if (isFirst) intent.putExtra(EXTRA_BOOLEAN_FIRST, true)

		val pendingIntent = PendingIntent.getBroadcast(
			context,
			notificationTime.toLocalTime().toSecondOfDay(),
			intent,
			FLAG_IMMUTABLE
		)

		alarmManager.setBest(notificationTime, pendingIntent)
		Log.d(
			LOG_TAG,
			"${notificationEndPeriodItem.getShort(ElementType.SUBJECT)} scheduled for $notificationTime"
		)

		val deletingIntent = Intent(context, NotificationReceiver::class.java)
			.putExtra(EXTRA_INT_ID, id)
			.putExtra(EXTRA_BOOLEAN_CLEAR, true)
		val deletingPendingIntent = PendingIntent.getBroadcast(
			context,
			notificationTime.toLocalTime().toSecondOfDay() + 1, // Different id to previous intent
			deletingIntent,
			FLAG_IMMUTABLE
		)
		alarmManager.setBest(
			notificationEndPeriodItem.originalPeriod.startDateTime,
			deletingPendingIntent
		)
		Log.d(
			LOG_TAG,
			"${notificationEndPeriodItem.getShort(ElementType.SUBJECT)} delete scheduled for ${notificationEndPeriodItem.originalPeriod.startDateTime}"
		)
	}

	private fun clearNotification(
		context: Context,
		notificationTime: LocalDateTime,
	) {
		(context.getSystemService(ALARM_SERVICE) as AlarmManager).run {
			cancel(
				PendingIntent.getBroadcast(
					context,
					notificationTime.toLocalTime().toSecondOfDay(),
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
					description = applicationContext.getString(R.string.notifications_channel_backgrounderrors_desc)
				},
				NotificationChannel(
					CHANNEL_ID_BREAKINFO,
					applicationContext.getString(R.string.notifications_channel_breakinfo),
					NotificationManager.IMPORTANCE_LOW
				).apply {
					description = applicationContext.getString(R.string.notifications_channel_breakinfo_desc)
				},
				NotificationChannel(
					CHANNEL_ID_FIRSTLESSON,
					applicationContext.getString(R.string.notifications_channel_firstlesson),
					NotificationManager.IMPORTANCE_LOW
				).apply {
					description = applicationContext.getString(R.string.notifications_channel_firstlesson_desc)
				},
			).forEach {
				notificationManager.createNotificationChannel(it)
			}
		}
	}
}
