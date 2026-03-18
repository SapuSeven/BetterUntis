package com.sapuseven.untis.feature.notifications

import android.util.Log
import com.sapuseven.untis.core.datastore.UserSettingsDataSource
import com.sapuseven.untis.core.domain.timetable.equalsIgnoreTime
import com.sapuseven.untis.core.domain.timetable.isCancelled
import com.sapuseven.untis.core.domain.timetable.merged
import com.sapuseven.untis.core.model.timetable.Period
import com.sapuseven.untis.core.model.timetable.Timetable
import com.sapuseven.untis.core.model.user.User
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

class ScheduleNotificationsUseCase @Inject constructor(
	private val userSettingsDataSource: UserSettingsDataSource,
	private val notificationRepository: NotificationRepository,
	private val clock: Clock = Clock.System,
	private val zone: TimeZone = TimeZone.currentSystemDefault(),
) {
	companion object {
		private val LOG_TAG = ScheduleNotificationsUseCase::class.simpleName
	}

	suspend operator fun invoke(user: User, timetable: Timetable) {
		userSettingsDataSource.getSettings(user.id).first().let { settings ->
			if (!settings.notificationsEnable) return@let

			notificationRepository.setupNotificationChannels()

			var scheduledNotifications = 0

			val preparedItems = timetable.periods
				.filterNot(Period::isCancelled)
				.merged()
				.sortedBy { it.startDateTime }

			if (preparedItems.isEmpty()) {
				Log.d(LOG_TAG, "[user#${user.id}] No notifications to schedule")
				return@let
			}

			preparedItems.first().let { period ->
				if (period.startDateTime <= clock.now().toLocalDateTime(zone)) return@let
				val notificationTime = period.startDateTime.toInstant(zone)
					.minus(settings.notificationsBeforeFirstTime, DateTimeUnit.MINUTE)

				if (settings.notificationsBeforeFirst) {
					notificationRepository.scheduleNotification(
						user.id,
						notificationTime,
						period,
						true
					)
					scheduledNotifications++
				} else {
					notificationRepository.clearNotification(user.id, notificationTime)
				}
			}

			try {
				preparedItems.zipWithNext().forEach { item ->
					val notificationTime = item.first.endDateTime

					if (notificationTime == item.second.startDateTime) {
						Log.d(LOG_TAG, "[user#${user.id}] Notification at $notificationTime skipped (no break until next lesson starts)")
						return@forEach
					}

					if (item.first.equalsIgnoreTime(item.second) && !settings.notificationsInMultiple) {
						notificationRepository.clearNotification(
							user.id, notificationTime.toInstant(zone)
						)
						Log.d(LOG_TAG, "[user#${user.id}] Notification at $notificationTime skipped (multi-hour lesson)")
						return@forEach
					}

					if (item.second.startDateTime <= clock.now().toLocalDateTime(zone)) {
						Log.d(LOG_TAG, "[user#${user.id}] Notification at $notificationTime skipped (break already ended)")
						return@forEach
					}

					notificationRepository.scheduleNotification(
						user.id,
						notificationTime.toInstant(zone),
						item.second
					)
					scheduledNotifications++
				}

				Log.d(LOG_TAG, "[user#${user.id}] Scheduled $scheduledNotifications notifications today.")
			} catch (e: Exception) {
				Log.e(LOG_TAG, "[user#${user.id}] Notifications couldn't be scheduled", e)
			}
		}
	}

	suspend fun isEnabled(user: User): Boolean {
		return userSettingsDataSource.getSettings(user.id).first().notificationsEnable
			&& notificationRepository.canPostNotifications()
	}
}
