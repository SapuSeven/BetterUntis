package com.sapuseven.untis.feature.automute

import com.sapuseven.untis.core.domain.worker.TimetableHandler
import com.sapuseven.untis.core.model.timetable.Timetable
import com.sapuseven.untis.core.model.user.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AutoMuteScheduler @Inject constructor(
	private val scheduleAutoMute: ScheduleAutoMuteUseCase,
) : TimetableHandler {

	override suspend fun isEnabled(user: User): Boolean {
		return scheduleAutoMute.isEnabled(user)
	}

	override suspend fun onNewTimetable(user: User, timetable: Timetable) {
		if (!isEnabled(user)) return

		scheduleAutoMute(user, timetable)
	}
}
