package com.sapuseven.untis.receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.util.Log
import com.sapuseven.untis.data.timetable.TimegridItem
import com.sapuseven.untis.helpers.config.PreferenceManager
import com.sapuseven.untis.helpers.config.PreferenceUtils
import com.sapuseven.untis.receivers.AutoMuteReceiver.Companion.EXTRA_BOOLEAN_MUTE
import com.sapuseven.untis.receivers.AutoMuteReceiver.Companion.EXTRA_INT_ID
import org.joda.time.LocalDateTime

class AutoMuteSetup : LessonEventSetup() {
	private lateinit var preferenceManager: PreferenceManager

	override fun onReceive(context: Context, intent: Intent) {
		Log.d("AutoMuteSetup", "AutoMuteSetup received")

		preferenceManager = PreferenceManager(context)
		if (PreferenceUtils.getPrefBool(preferenceManager, "preference_automute_enable"))
			super.onReceive(context, intent)
	}

	override fun onLoadingSuccess(context: Context, items: List<TimegridItem>) {
		items.merged().forEach { item ->
			val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
			val id = item.startDateTime.millisOfDay / 1000

			if (item.endDateTime.millisOfDay <= LocalDateTime.now().millisOfDay) return@forEach

			val muteIntent = Intent(context, AutoMuteReceiver::class.java)
					.putExtra(EXTRA_INT_ID, id)
					.putExtra(EXTRA_BOOLEAN_MUTE, true)
			val pendingMuteIntent = PendingIntent.getBroadcast(context, item.startDateTime.millisOfDay, muteIntent, 0)
			alarmManager.setExact(AlarmManager.RTC_WAKEUP, item.startDateTime.millis, pendingMuteIntent)
			Log.d("AutoMuteSetup", "${item.periodData.getShortTitle()} mute scheduled for ${item.startDateTime}")

			val unmuteIntent = Intent(context, AutoMuteReceiver::class.java)
					.putExtra(EXTRA_INT_ID, id)
					.putExtra(EXTRA_BOOLEAN_MUTE, false)
			val pendingUnmuteIntent = PendingIntent.getBroadcast(context, item.endDateTime.millisOfDay, unmuteIntent, 0)
			alarmManager.setExact(AlarmManager.RTC_WAKEUP, item.endDateTime.millis, pendingUnmuteIntent)
			Log.d("AutoMuteSetup", "${item.periodData.getShortTitle()} unmute scheduled for ${item.endDateTime}")
		}
	}

	override fun onLoadingError(context: Context, requestId: Int, code: Int?, message: String?) {}
}
