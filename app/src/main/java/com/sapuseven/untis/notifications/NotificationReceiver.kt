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
		const val CHANNEL_ID_LESSONINFO = "notifications.breakinfo"
		const val CHANNEL_ID_BREAKINFO = "notifications.breakinfo"
	}

	override fun onReceive(context: Context, intent: Intent) {
		Log.d("NotificationReceiver", "NotificationReceiver received")

		val preferenceManager = PreferenceManager(context)
		if (!PreferenceUtils.getPrefBool(preferenceManager, "preference_notifications_enable")) return

		if (intent.hasExtra("breakEndTime")) {
			createNotificationChannel(context)

			val pendingIntent = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), 0)
			val breakEndTime = intent.getStringExtra("breakEndTime")

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
				notify(intent.getIntExtra("id", -1), builder.build())
			}
			Log.d("NotificationReceiver", "notification delivered: $title")
		} else {
			Log.d("NotificationReceiver", "Attempting to cancel notification #${intent.getIntExtra("id", -1)}")
			with(NotificationManagerCompat.from(context)) {
				cancel(intent.getIntExtra("id", -1))
			}
		}
	}

	// TODO: Find a way to deal with empty values
	// TODO: Add parameter to optionally only include the raw values instead of the string templates (for the short message)
	private fun buildMessage(context: Context, intent: Intent, preferenceManager: PreferenceManager, separator: String) = listOfNotNull(
			when (PreferenceUtils.getPrefString(preferenceManager, "preference_notifications_visibility_subjects")) {
				"short" -> context.getString(R.string.notification_classes, intent.getStringExtra("nextSubject"))
				"long" -> context.getString(R.string.notification_subjects, intent.getStringExtra("nextSubjectLong"))
				else -> null
			},
			when (PreferenceUtils.getPrefString(preferenceManager, "preference_notifications_visibility_rooms")) {
				"short" -> context.getString(R.string.notification_classes, intent.getStringExtra("nextRoom"))
				"long" -> context.getString(R.string.notification_rooms, intent.getStringExtra("nextRoomLong"))
				else -> null
			},
			when (PreferenceUtils.getPrefString(preferenceManager, "preference_notifications_visibility_teachers")) {
				"short" -> context.getString(R.string.notification_classes, intent.getStringExtra("nextTeacher"))
				"long" -> context.getString(R.string.notification_teachers, intent.getStringExtra("nextTeacherLong"))
				else -> null
			},
			when (PreferenceUtils.getPrefString(preferenceManager, "preference_notifications_visibility_classes")) {
				"short" -> context.getString(R.string.notification_classes, intent.getStringExtra("nextClass"))
				"long" -> context.getString(R.string.notification_classes, intent.getStringExtra("nextClassLong"))
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
