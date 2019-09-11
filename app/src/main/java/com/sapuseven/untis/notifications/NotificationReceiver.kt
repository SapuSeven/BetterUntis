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
import com.sapuseven.untis.helpers.config.PreferenceUtils

class NotificationReceiver : BroadcastReceiver() {
	override fun onReceive(context: Context, intent: Intent) {
		Log.d("NotificationReceiver", "NotificationReceiver received")

		val preferenceManager = com.sapuseven.untis.helpers.config.PreferenceManager(context)
		if (!PreferenceUtils.getPrefBool(preferenceManager, "preference_notifications_enable")) return

		val clear = intent.getBooleanExtra("clear", false)
		//if (!clear && LocalDateTime.now() > intent.getLocalDateTimeExtra("breakEndTime")) return

		if (clear) {
			Log.d("NotificationReceiver", "Attempting to cancel notification #${intent.getIntExtra("id", -1)}")
			with(NotificationManagerCompat.from(context)) {
				cancel(intent.getIntExtra("id", -1))
			}

		} else {
			createNotificationChannel(context)

			val pIntent = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), 0)
			val breakEndTime = intent.getStringExtra("breakEndTime")
			val title = "Break until $breakEndTime"
			Log.d("NotificationReceiver", "notification delivered: $title")

			val builder = NotificationCompat.Builder(context, "id")
					.setSmallIcon(R.drawable.all_launcher_foreground)
					.setContentTitle(title)
					.setContentText("Hello World!")
					.setPriority(NotificationCompat.PRIORITY_DEFAULT)

			with(NotificationManagerCompat.from(context)) {
				notify(intent.getIntExtra("id", -1), builder.build())
			}

			/*val message = StringBuilder()
			if (prefs.getString("preference_notifications_visibility_subjects", "long") == "long")
				message.append(context.getString(R.string.notification_subjects, intent.getStringExtra("nextSubjectLong")))
			else if (prefs.getString("preference_notifications_visibility_subjects", "long") == "short")
				message.append(context.getString(R.string.notification_subjects, intent.getStringExtra("nextSubject")))

			if (prefs.getString("preference_notifications_visibility_rooms", "short") == "long") {
				if (message.length > 0)
					message.append(" / ")
				message.append(context.getString(R.string.notification_rooms, intent.getStringExtra("nextRoomLong")))
			} else if (prefs.getString("preference_notifications_visibility_rooms", "short") == "short") {
				if (message.length > 0)
					message.append(" / ")
				message.append(context.getString(R.string.notification_rooms, intent.getStringExtra("nextRoom")))
			}

			if (prefs.getString("preference_notifications_visibility_teachers", "short") == "long") {
				if (message.length > 0)
					message.append(" / ")
				message.append(context.getString(R.string.notification_teachers, intent.getStringExtra("nextTeacherLong")))
			} else if (prefs.getString("preference_notifications_visibility_teachers", "short") == "short") {
				if (message.length > 0)
					message.append(" / ")
				message.append(context.getString(R.string.notification_teachers, intent.getStringExtra("nextTeacher")))
			}

			val longMessage = StringBuilder()
			if (prefs.getString("preference_notifications_visibility_subjects", "long") == "long")
				longMessage.append(context.getString(R.string.notification_subjects, intent.getStringExtra("nextSubjectLong")))
			else if (prefs.getString("preference_notifications_visibility_subjects", "long") == "short")
				longMessage.append(context.getString(R.string.notification_subjects, intent.getStringExtra("nextSubject")))

			if (prefs.getString("preference_notifications_visibility_rooms", "short") == "long") {
				if (longMessage.length > 0)
					longMessage.append('\n')
				longMessage.append(context.getString(R.string.notification_rooms, intent.getStringExtra("nextRoomLong")))
			} else if (prefs.getString("preference_notifications_visibility_rooms", "short") == "short") {
				if (longMessage.length > 0)
					longMessage.append('\n')
				longMessage.append(context.getString(R.string.notification_rooms, intent.getStringExtra("nextRoom")))
			}

			if (prefs.getString("preference_notifications_visibility_teachers", "short") == "long") {
				if (longMessage.length > 0)
					longMessage.append('\n')
				longMessage.append(context.getString(R.string.notification_teachers, intent.getStringExtra("nextTeacherLong")))
			} else if (prefs.getString("preference_notifications_visibility_teachers", "short") == "short") {
				if (longMessage.length > 0)
					longMessage.append('\n')
				longMessage.append(context.getString(R.string.notification_teachers, intent.getStringExtra("nextTeacher")))
			}

			val n: Notification
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
				n = Notification.Builder(context)
						.setContentTitle(title)
						.setContentText(message)
						.setSmallIcon(R.drawable.ic_stat_timetable)
						.setContentIntent(pIntent)
						.setStyle(Notification.BigTextStyle().bigText(longMessage))
						.setAutoCancel(false)
						.setOngoing(true)
						.setCategory(Notification.CATEGORY_STATUS)
						.build()
				n.visibility = Notification.VISIBILITY_PUBLIC
			} else {
				n = Notification.Builder(context)
						.setContentTitle(title)
						.setContentText(message)
						.setSmallIcon(R.drawable.ic_stat_timetable)
						.setContentIntent(pIntent)
						.setStyle(Notification.BigTextStyle().bigText(longMessage))
						.setAutoCancel(false)
						.setOngoing(true)
						.build()
			}
			notificationManager.notify(intent.getIntExtra("id", (System.currentTimeMillis() * 0.001).toInt()), n)*/
		}
	}

	private fun createNotificationChannel(context: Context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val name = "channel_name"//context.getString(R.string.channel_name)
			val descriptionText = "channel_description"//context.getString(R.string.channel_description)
			val importance = NotificationManager.IMPORTANCE_DEFAULT
			val channel = NotificationChannel("id", name, importance).apply {
				description = descriptionText
			}
			val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
			notificationManager.createNotificationChannel(channel)
		}
	}
}
