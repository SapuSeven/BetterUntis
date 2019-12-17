package com.sapuseven.untis.receivers

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.sapuseven.untis.R
import com.sapuseven.untis.data.timetable.TimegridItem
import com.sapuseven.untis.helpers.DateTimeUtils
import com.sapuseven.untis.helpers.config.PreferenceManager
import com.sapuseven.untis.helpers.config.PreferenceUtils
import com.sapuseven.untis.receivers.NotificationReceiver.Companion.EXTRA_BOOLEAN_CLEAR
import com.sapuseven.untis.receivers.NotificationReceiver.Companion.EXTRA_BOOLEAN_FIRST
import com.sapuseven.untis.receivers.NotificationReceiver.Companion.EXTRA_INT_BREAK_END_TIME
import com.sapuseven.untis.receivers.NotificationReceiver.Companion.EXTRA_INT_ID
import com.sapuseven.untis.receivers.NotificationReceiver.Companion.EXTRA_STRING_BREAK_END_TIME
import com.sapuseven.untis.receivers.NotificationReceiver.Companion.EXTRA_STRING_NEXT_CLASS
import com.sapuseven.untis.receivers.NotificationReceiver.Companion.EXTRA_STRING_NEXT_CLASS_LONG
import com.sapuseven.untis.receivers.NotificationReceiver.Companion.EXTRA_STRING_NEXT_ROOM
import com.sapuseven.untis.receivers.NotificationReceiver.Companion.EXTRA_STRING_NEXT_ROOM_LONG
import com.sapuseven.untis.receivers.NotificationReceiver.Companion.EXTRA_STRING_NEXT_SUBJECT
import com.sapuseven.untis.receivers.NotificationReceiver.Companion.EXTRA_STRING_NEXT_SUBJECT_LONG
import com.sapuseven.untis.receivers.NotificationReceiver.Companion.EXTRA_STRING_NEXT_TEACHER
import com.sapuseven.untis.receivers.NotificationReceiver.Companion.EXTRA_STRING_NEXT_TEACHER_LONG
import org.joda.time.DateTime
import org.joda.time.LocalDateTime

/**
 * This receiver is responsible for setting up alarms for every notification of the current day.
 */
class NotificationSetup : LessonEventSetup() {
	private var receivedManually: Boolean = false
	private lateinit var preferenceManager: PreferenceManager

	companion object {
		// Describes whether the broadcast was sent manually by opening the app or automatically every day.
		// This is to only show error notifications when loading in the background.
		const val EXTRA_BOOLEAN_MANUAL = "com.sapuseven.untis.notifications.manual"

		const val CHANNEL_ID_BACKGROUNDERRORS = "notifications.backgrounderrors"
	}

	override fun onReceive(context: Context, intent: Intent) {
		Log.d("NotificationSetup", "NotificationSetup received")

		preferenceManager = PreferenceManager(context, intent.getLongExtra(EXTRA_LONG_PROFILE_ID, 0))
		if (PreferenceUtils.getPrefBool(preferenceManager, "preference_notifications_enable"))
			super.onReceive(context, intent)

		receivedManually = intent.getBooleanExtra(EXTRA_BOOLEAN_MANUAL, false)
	}

	override fun onLoadingSuccess(context: Context, items: List<TimegridItem>) {
		val preparedItems = items.filter { !it.periodData.isCancelled() }.sortedBy { it.startDateTime }.merged().zipWithNext()

		if (preparedItems.isNotEmpty() && PreferenceUtils.getPrefBool(preferenceManager, "preference_notifications_before_first"))
			with(preparedItems.first().first) {
				if (startDateTime.millisOfDay < LocalDateTime.now().millisOfDay) return@with

				scheduleNotification(context,
						startDateTime.minusMinutes(PreferenceUtils.getPrefInt(preferenceManager, "preference_notifications_before_first_time")),
						this,
						true)
			}

		preparedItems.forEach { item ->
			if (item.first.endDateTime == item.second.startDateTime) return // No break exists

			if (item.first.equalsIgnoreTime(item.second)
					&& !PreferenceUtils.getPrefBool(preferenceManager, "preference_notifications_in_multiple"))
				return@forEach // multi-hour lesson

			if (item.second.startDateTime.millisOfDay < LocalDateTime.now().millisOfDay) return@forEach // ignore lessons in the past

			Log.d("NotificationSetup", "found ${item.first.periodData.getShortTitle()}")
			scheduleNotification(context, item.first.endDateTime, item.second)
		}
	}

	private fun scheduleNotification(context: Context, notificationTime: DateTime, notificationEndLesson: TimegridItem, isFirst: Boolean = false) {
		val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
		val id = notificationTime.millisOfDay / 1000

		val intent = Intent(context, NotificationReceiver::class.java)
				.putExtra(EXTRA_INT_ID, id)
				.putExtra(EXTRA_INT_BREAK_END_TIME, notificationEndLesson.startDateTime.millisOfDay)
				.putExtra(EXTRA_STRING_BREAK_END_TIME, notificationEndLesson.startDateTime.toString(DateTimeUtils.shortDisplayableTime()))
				.putExtra(EXTRA_STRING_NEXT_SUBJECT, notificationEndLesson.periodData.getShortTitle())
				.putExtra(EXTRA_STRING_NEXT_SUBJECT_LONG, notificationEndLesson.periodData.getLongTitle())
				.putExtra(EXTRA_STRING_NEXT_ROOM, notificationEndLesson.periodData.getShortRooms())
				.putExtra(EXTRA_STRING_NEXT_ROOM_LONG, notificationEndLesson.periodData.getLongRooms())
				.putExtra(EXTRA_STRING_NEXT_TEACHER, notificationEndLesson.periodData.getShortTeachers())
				.putExtra(EXTRA_STRING_NEXT_TEACHER_LONG, notificationEndLesson.periodData.getLongTeachers())
				.putExtra(EXTRA_STRING_NEXT_CLASS, notificationEndLesson.periodData.getShortClasses())
				.putExtra(EXTRA_STRING_NEXT_CLASS_LONG, notificationEndLesson.periodData.getLongClasses())

		if (isFirst) intent.putExtra(EXTRA_BOOLEAN_FIRST, true)

		val pendingIntent = PendingIntent.getBroadcast(context, notificationTime.millisOfDay, intent, 0)
		alarmManager.setExact(AlarmManager.RTC_WAKEUP, notificationTime.millis, pendingIntent)
		Log.d("NotificationSetup", "${notificationEndLesson.periodData.getShortTitle()} scheduled for $notificationTime")

		val deletingIntent = Intent(context, NotificationReceiver::class.java)
				.putExtra(EXTRA_INT_ID, id)
				.putExtra(EXTRA_BOOLEAN_CLEAR, true)
		val deletingPendingIntent = PendingIntent.getBroadcast(context, notificationTime.millisOfDay + 1, deletingIntent, 0)
		alarmManager.setExact(AlarmManager.RTC_WAKEUP, notificationEndLesson.startDateTime.millis, deletingPendingIntent)
		Log.d("NotificationSetup", "${notificationEndLesson.periodData.getShortTitle()} delete scheduled for ${notificationEndLesson.startDateTime}")
	}

	override fun onLoadingError(context: Context, requestId: Int, code: Int?, message: String?) {
		if (receivedManually) return

		createNotificationChannel(context)

		val builder = NotificationCompat.Builder(context, NotificationReceiver.CHANNEL_ID_BREAKINFO)
				.setContentTitle(context.getString(R.string.notifications_text_error_title))
				.setContentText(context.getString(R.string.notifications_text_error_message))
				.setSmallIcon(R.drawable.notification_error)
				.setAutoCancel(true)
				.setCategory(NotificationCompat.CATEGORY_ERROR)

		with(NotificationManagerCompat.from(context)) {
			notify(-1, builder.build())
		}
	}

	private fun createNotificationChannel(context: Context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val name = context.getString(R.string.notifications_channel_backgrounderrors)
			val descriptionText = context.getString(R.string.notifications_channel_backgrounderrors_desc)
			val importance = NotificationManager.IMPORTANCE_MIN
			val channel = NotificationChannel(CHANNEL_ID_BACKGROUNDERRORS, name, importance).apply {
				description = descriptionText
			}
			val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
			notificationManager.createNotificationChannel(channel)
		}
	}
}
