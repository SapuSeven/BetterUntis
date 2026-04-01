package com.sapuseven.untis.feature.automute

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import com.sapuseven.untis.core.data.system.setBest
import com.sapuseven.untis.core.datastore.UserSettingsDataSource
import com.sapuseven.untis.core.domain.service.AutoMuteService
import com.sapuseven.untis.core.model.user.User
import com.sapuseven.untis.feature.automute.receiver.AutoMuteReceiver
import com.sapuseven.untis.feature.automute.receiver.AutoMuteReceiver.Companion.EXTRA_BOOLEAN_MUTE
import com.sapuseven.untis.feature.automute.receiver.AutoMuteReceiver.Companion.EXTRA_INT_ID
import com.sapuseven.untis.feature.automute.receiver.AutoMuteReceiver.Companion.EXTRA_LONG_USER_ID
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Instant
import javax.inject.Inject

class AutoMuteRepository @Inject constructor(
	@param:ApplicationContext private val context: Context,
	private var userSettingsDataSource: UserSettingsDataSource,
	private val autoMuteService: AutoMuteService,
) {
	internal fun scheduleAutoMute(
		userId: Long,
		muteTime: Instant,
		unmuteTime: Instant?,
	) {
		scheduleEvent(userId, muteTime, true)
		unmuteTime?.let { scheduleEvent(userId, it, false) }
	}

	private fun scheduleEvent(
		userId: Long,
		eventTime: Instant,
		mute: Boolean,
	) {
		val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
		val eventId = ("${userId}_${eventTime.epochSeconds}").hashCode()

		val intent =
			Intent(context, AutoMuteReceiver::class.java)
				.putExtra(EXTRA_INT_ID, 0)
				.putExtra(EXTRA_LONG_USER_ID, userId)
				.putExtra(EXTRA_BOOLEAN_MUTE, mute)
		val pendingIntent = PendingIntent.getBroadcast(
			context,
			eventId,
			intent,
			FLAG_IMMUTABLE
		)
		alarmManager.setBest(eventTime, pendingIntent)
	}

	internal suspend fun autoMuteState(
		user: User,
		mute: Boolean,
	) {
		val userSettings = userSettingsDataSource.getSettings(user.id).first()

		if (mute) {
			if (!userSettings.automuteEnable && autoMuteService.isAutoMuteEnabled()) return
			autoMuteService.autoMuteStateOn()
		} else {
			autoMuteService.autoMuteStateOff()
		}
	}

	@SuppressLint("ObsoleteSdkInt")
	internal fun canAutoMute(): Boolean {
		val notificationManager =
			context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
		return (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || notificationManager.isNotificationPolicyAccessGranted)
	}
}
