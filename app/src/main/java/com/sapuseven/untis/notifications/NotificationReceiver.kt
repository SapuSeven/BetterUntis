package com.sapuseven.untis.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.sapuseven.untis.R
import com.sapuseven.untis.activities.MainActivity
import com.sapuseven.untis.helpers.config.PreferenceManager
import com.sapuseven.untis.helpers.config.PreferenceUtils

class NotificationReceiver : BroadcastReceiver() {
	companion object {
		const val CHANNEL_ID_BREAKINFO = "notifications.breakinfo"

		const val EXTRA_INT_ID = "com.sapuseven.untis.notifications.id"
		const val EXTRA_BOOLEAN_CLEAR = "com.sapuseven.untis.notifications.clear"
		const val EXTRA_STRING_BREAK_END_TIME = "com.sapuseven.untis.notifications.breakEndTime"
		const val EXTRA_STRING_NEXT_SUBJECT = "com.sapuseven.untis.notifications.nextSubject"
		const val EXTRA_STRING_NEXT_SUBJECT_LONG = "com.sapuseven.untis.notifications.nextSubjectLong"
		const val EXTRA_STRING_NEXT_ROOM = "com.sapuseven.untis.notifications.nextRoom"
		const val EXTRA_STRING_NEXT_ROOM_LONG = "com.sapuseven.untis.notifications.nextRoomLong"
		const val EXTRA_STRING_NEXT_TEACHER = "com.sapuseven.untis.notifications.nextTeacher"
		const val EXTRA_STRING_NEXT_TEACHER_LONG = "com.sapuseven.untis.notifications.nextTeacherLong"
		const val EXTRA_STRING_NEXT_CLASS = "com.sapuseven.untis.notifications.nextClass"
		const val EXTRA_STRING_NEXT_CLASS_LONG = "com.sapuseven.untis.notifications.nextClassLong"
	}

	override fun onReceive(context: Context, intent: Intent) {
		Log.d("NotificationReceiver", "NotificationReceiver received")

		val preferenceManager = PreferenceManager(context)
		if (!PreferenceUtils.getPrefBool(preferenceManager, "preference_notifications_enable")) return

		if (intent.hasExtra(EXTRA_STRING_BREAK_END_TIME)) {
			createNotificationChannel(context)

			val pendingIntent = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), 0)
			val breakEndTime = intent.getStringExtra(EXTRA_STRING_BREAK_END_TIME)

			val title = "Break until $breakEndTime" // TODO: Extract string
			val message = buildMessage(context, intent, preferenceManager, " / ") // TODO: Extract string
			val longMessage = buildMessage(context, intent, preferenceManager, "\n")

			val builder = NotificationCompat.Builder(context, CHANNEL_ID_BREAKINFO)
					.setContentTitle(title)
					.setContentText(message)
					.setSmallIcon(R.drawable.notification_clock)
					.setContentIntent(pendingIntent)
					.setStyle(NotificationCompat.BigTextStyle().bigText(longMessage))
					.setAutoCancel(false)
					.setOngoing(true)
					.setCategory(NotificationCompat.CATEGORY_STATUS)

			with(NotificationManagerCompat.from(context)) {
				notify(intent.getIntExtra(EXTRA_INT_ID, -1), builder.build())
			}
			Log.d("NotificationReceiver", "notification delivered: $title")
		} else {
			Log.d("NotificationReceiver", "Attempting to cancel notification #${intent.getIntExtra(EXTRA_INT_ID, -1)}")
			with(NotificationManagerCompat.from(context)) {
				cancel(intent.getIntExtra(EXTRA_INT_ID, -1))
			}
		}
	}

	// TODO: Find a way to deal with empty values
	// TODO: Add parameter to optionally only include the raw values instead of the string templates (for the short message)
	private fun buildMessage(context: Context, intent: Intent, preferenceManager: PreferenceManager, separator: String) = listOfNotNull(
			when (PreferenceUtils.getPrefString(preferenceManager, "preference_notifications_visibility_subjects")) {
				"short" -> context.getString(R.string.notification_subjects, intent.getStringExtra(EXTRA_STRING_NEXT_SUBJECT))
				"long" -> context.getString(R.string.notification_subjects, intent.getStringExtra(EXTRA_STRING_NEXT_SUBJECT_LONG))
				else -> null
			},
			when (PreferenceUtils.getPrefString(preferenceManager, "preference_notifications_visibility_rooms")) {
				"short" -> context.getString(R.string.notification_rooms, intent.getStringExtra(EXTRA_STRING_NEXT_ROOM))
				"long" -> context.getString(R.string.notification_rooms, intent.getStringExtra(EXTRA_STRING_NEXT_ROOM_LONG))
				else -> null
			},
			when (PreferenceUtils.getPrefString(preferenceManager, "preference_notifications_visibility_teachers")) {
				"short" -> context.getString(R.string.notification_teachers, intent.getStringExtra(EXTRA_STRING_NEXT_TEACHER))
				"long" -> context.getString(R.string.notification_teachers, intent.getStringExtra(EXTRA_STRING_NEXT_TEACHER_LONG))
				else -> null
			},
			when (PreferenceUtils.getPrefString(preferenceManager, "preference_notifications_visibility_classes")) {
				"short" -> context.getString(R.string.notification_classes, intent.getStringExtra(EXTRA_STRING_NEXT_CLASS))
				"long" -> context.getString(R.string.notification_classes, intent.getStringExtra(EXTRA_STRING_NEXT_CLASS_LONG))
				else -> null
			}
	).joinToString(separator)

	private fun createNotificationChannel(context: Context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val name = context.getString(R.string.notifications_channel_breakinfo)
			val descriptionText = context.getString(R.string.notifications_channel_breakinfo_desc)
			val importance = NotificationManager.IMPORTANCE_LOW
			val channel = NotificationChannel(CHANNEL_ID_BREAKINFO, name, importance).apply {
				description = descriptionText
			}
			val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
			notificationManager.createNotificationChannel(channel)
		}
	}
}
