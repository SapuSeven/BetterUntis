package com.sapuseven.untis.feature.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import android.util.Log
import com.sapuseven.untis.core.data.system.setBest
import com.sapuseven.untis.core.domain.timetable.classes
import com.sapuseven.untis.core.domain.timetable.rooms
import com.sapuseven.untis.core.domain.timetable.subjects
import com.sapuseven.untis.core.domain.timetable.teachers
import com.sapuseven.untis.core.domain.timetable.toLongString
import com.sapuseven.untis.core.domain.timetable.toShortString
import com.sapuseven.untis.core.model.timetable.Period
import com.sapuseven.untis.core.ui.BuildConfig
import com.sapuseven.untis.feature.notifications.receiver.NotificationReceiver
import com.sapuseven.untis.feature.notifications.receiver.NotificationReceiver.Companion.EXTRA_BOOLEAN_CLEAR
import com.sapuseven.untis.feature.notifications.receiver.NotificationReceiver.Companion.EXTRA_BOOLEAN_FIRST
import com.sapuseven.untis.feature.notifications.receiver.NotificationReceiver.Companion.EXTRA_INT_BREAK_END_TIME
import com.sapuseven.untis.feature.notifications.receiver.NotificationReceiver.Companion.EXTRA_INT_ID
import com.sapuseven.untis.feature.notifications.receiver.NotificationReceiver.Companion.EXTRA_LONG_USER_ID
import com.sapuseven.untis.feature.notifications.receiver.NotificationReceiver.Companion.EXTRA_STRING_BREAK_END_TIME
import com.sapuseven.untis.feature.notifications.receiver.NotificationReceiver.Companion.EXTRA_STRING_NEXT_CLASS
import com.sapuseven.untis.feature.notifications.receiver.NotificationReceiver.Companion.EXTRA_STRING_NEXT_CLASS_LONG
import com.sapuseven.untis.feature.notifications.receiver.NotificationReceiver.Companion.EXTRA_STRING_NEXT_ROOM
import com.sapuseven.untis.feature.notifications.receiver.NotificationReceiver.Companion.EXTRA_STRING_NEXT_ROOM_LONG
import com.sapuseven.untis.feature.notifications.receiver.NotificationReceiver.Companion.EXTRA_STRING_NEXT_SUBJECT
import com.sapuseven.untis.feature.notifications.receiver.NotificationReceiver.Companion.EXTRA_STRING_NEXT_SUBJECT_LONG
import com.sapuseven.untis.feature.notifications.receiver.NotificationReceiver.Companion.EXTRA_STRING_NEXT_TEACHER
import com.sapuseven.untis.feature.notifications.receiver.NotificationReceiver.Companion.EXTRA_STRING_NEXT_TEACHER_LONG
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaLocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.inject.Inject

class NotificationRepository @Inject constructor(
	@param:ApplicationContext private val context: Context,
	private val zone: TimeZone = TimeZone.currentSystemDefault(),
) {
	companion object {
		private val LOG_TAG = NotificationRepository::class.simpleName

		const val CHANNEL_ID_DEBUG = "notifications.debug"
		const val CHANNEL_ID_BACKGROUNDERRORS = "notifications.backgrounderrors"
		const val CHANNEL_ID_BREAKINFO = "notifications.breakinfo"
		const val CHANNEL_ID_FIRSTLESSON = "notifications.firstlesson"
	}

	internal fun setupNotificationChannels() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val notificationManager: NotificationManager =
				context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

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
					context.getString(R.string.feature_notifications_channel_backgrounderrors),
					NotificationManager.IMPORTANCE_MIN
				).apply {
					description =
						context.getString(R.string.feature_notifications_channel_backgrounderrors_desc)
				},
				NotificationChannel(
					CHANNEL_ID_BREAKINFO,
					context.getString(R.string.feature_notifications_channel_breakinfo),
					NotificationManager.IMPORTANCE_LOW
				).apply {
					description =
						context.getString(R.string.feature_notifications_channel_breakinfo_desc)
				},
				NotificationChannel(
					CHANNEL_ID_FIRSTLESSON,
					context.getString(R.string.feature_notifications_channel_firstlesson),
					NotificationManager.IMPORTANCE_LOW
				).apply {
					description =
						context.getString(R.string.feature_notifications_channel_firstlesson_desc)
				},
			).forEach {
				notificationManager.createNotificationChannel(it)
			}
		}
	}

	internal fun scheduleNotification(
		userId: Long,
		notificationTime: Instant,
		notificationEndPeriod: Period,
		isFirst: Boolean = false
	) {
		val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
		val notificationId = ("${userId}_${notificationTime.epochSeconds}").hashCode()

		// TODO: Include state (cancelled, irregular etc)
		val intent = Intent(context, NotificationReceiver::class.java)
			.putExtra(EXTRA_INT_ID, notificationId)
			.putExtra(EXTRA_LONG_USER_ID, userId)
			.putExtra(
				EXTRA_INT_BREAK_END_TIME,
				notificationEndPeriod.startDateTime.time.toSecondOfDay()
			)
			.putExtra(
				EXTRA_STRING_BREAK_END_TIME,
				notificationEndPeriod.startDateTime.toJavaLocalDateTime()
					.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
			)
			.putExtra(
				EXTRA_STRING_NEXT_SUBJECT,
				notificationEndPeriod.subjects.toShortString()
			)
			.putExtra(
				EXTRA_STRING_NEXT_SUBJECT_LONG,
				notificationEndPeriod.subjects.toLongString()
			)
			.putExtra(
				EXTRA_STRING_NEXT_ROOM,
				notificationEndPeriod.rooms.toShortString()
			)
			.putExtra(
				EXTRA_STRING_NEXT_ROOM_LONG,
				notificationEndPeriod.rooms.toLongString()
			)
			.putExtra(
				EXTRA_STRING_NEXT_TEACHER,
				notificationEndPeriod.teachers.toShortString()
			)
			.putExtra(
				EXTRA_STRING_NEXT_TEACHER_LONG,
				notificationEndPeriod.teachers.toLongString()
			)
			.putExtra(
				EXTRA_STRING_NEXT_CLASS,
				notificationEndPeriod.classes.toShortString()
			)
			.putExtra(
				EXTRA_STRING_NEXT_CLASS_LONG,
				notificationEndPeriod.classes.toLongString()
			)

		if (isFirst) intent.putExtra(EXTRA_BOOLEAN_FIRST, true)

		val pendingIntent = PendingIntent.getBroadcast(
			context,
			notificationId,
			intent,
			FLAG_IMMUTABLE
		)

		alarmManager.setBest(notificationTime, pendingIntent)
		Log.d(
			LOG_TAG,
			"${notificationEndPeriod.subjects.toShortString()} scheduled for $notificationTime"
		)

		val deletingIntent = Intent(context, NotificationReceiver::class.java)
			.putExtra(EXTRA_INT_ID, notificationId)
			.putExtra(EXTRA_BOOLEAN_CLEAR, true)
		val deletingPendingIntent = PendingIntent.getBroadcast(
			context,
			("${userId}_${notificationTime.epochSeconds}_DEL").hashCode(),
			deletingIntent,
			FLAG_IMMUTABLE
		)
		alarmManager.setBest(
			notificationEndPeriod.startDateTime.toInstant(zone),
			deletingPendingIntent
		)
		Log.d(
			LOG_TAG,
			"${notificationEndPeriod.subjects.toShortString()} delete scheduled for ${notificationEndPeriod.startDateTime}"
		)
	}

	internal fun clearNotification(
		userId: Long,
		notificationTime: Instant,
	) {
		(context.getSystemService(ALARM_SERVICE) as AlarmManager).run {
			cancel(
				PendingIntent.getBroadcast(
					context,
					("${userId}_${notificationTime.epochSeconds}").hashCode(),
					Intent(context, NotificationReceiver::class.java),
					FLAG_IMMUTABLE
				)
			)
		}
	}

	internal fun canPostNotifications(): Boolean {
		val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
		return (Build.VERSION.SDK_INT < Build.VERSION_CODES.N || notificationManager.areNotificationsEnabled())
	}
}
