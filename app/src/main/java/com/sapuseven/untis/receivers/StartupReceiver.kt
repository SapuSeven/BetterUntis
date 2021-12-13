package com.sapuseven.untis.receivers

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.helpers.config.PreferenceHelper
import com.sapuseven.untis.receivers.LessonEventSetup.Companion.EXTRA_LONG_PROFILE_ID
import org.joda.time.DateTime

class StartupReceiver : BroadcastReceiver() {
	// TODO: This gets called twice on starting the app. While this doesn't impair functionality, it is still unwanted behaviour.
	@SuppressLint("UnsafeProtectedBroadcastReceiver")
	override fun onReceive(context: Context, intent: Intent) {
		Log.d("StartupReceiver", "StartupReceiver received")
		val preferences = PreferenceHelper(context)
		val users = UserDatabase.createInstance(context)

		users.getAllUsers().forEach { user ->
			preferences.loadProfile(user.id!!)

			val dateTime = DateTime().withTime(2, 0, 0, 0)

			val broadcasts = mutableListOf<Intent>()

			if (preferences.get<Boolean>("preference_notifications_enable"))
				broadcasts.add(Intent(context, NotificationSetup::class.java).apply {
					putExtra(EXTRA_LONG_PROFILE_ID, user.id)
					putExtra(NotificationSetup.EXTRA_BOOLEAN_MANUAL, intent.getBooleanExtra(NotificationSetup.EXTRA_BOOLEAN_MANUAL, false))
				})

			if (preferences.get<Boolean>("preference_automute_enable"))
				broadcasts.add(Intent(context, AutoMuteSetup::class.java).apply {
					putExtra(EXTRA_LONG_PROFILE_ID, user.id)
				})

			broadcasts.forEach {
				val pendingIntent = PendingIntent.getBroadcast(context, 0, it, PendingIntent.FLAG_IMMUTABLE)
				val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
				am.setInexactRepeating(AlarmManager.RTC_WAKEUP, dateTime.millis, AlarmManager.INTERVAL_DAY, pendingIntent)

				context.sendBroadcast(it)
			}
		}
	}
}
