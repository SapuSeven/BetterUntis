package com.sapuseven.untis.feature.notifications

import com.sapuseven.untis.core.domain.worker.TimetableHandler
import com.sapuseven.untis.core.model.timetable.Timetable
import com.sapuseven.untis.core.model.user.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
	private val scheduleNotifications: ScheduleNotificationsUseCase,
) : TimetableHandler {

	override suspend fun isEnabled(user: User): Boolean {
		return scheduleNotifications.isEnabled(user)
	}

	override suspend fun onNewTimetable(user: User, timetable: Timetable) {
		if (!isEnabled(user)) return

		scheduleNotifications(user, timetable)
	}
}
