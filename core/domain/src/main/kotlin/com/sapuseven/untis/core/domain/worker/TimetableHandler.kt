package com.sapuseven.untis.core.domain.worker

import com.sapuseven.untis.core.model.timetable.Timetable
import com.sapuseven.untis.core.model.user.User

/**
 * Interface for handling timetable updates.
 * Implementations can perform actions when a new timetable is available for a user.
 * Timetable information (by default) is updated daily, so you can use this to schedule daily tasks that need today's timetable.
 */
interface TimetableHandler {
	/**
	 * This method will be called before any action is performed.
	 * Use this to check preconditions like preferences or permissions.
	 */
	suspend fun isEnabled(user: User): Boolean

	/**
	 * This method will be called when a new timetable is available for the user.
	 * Use this to perform any actions that depend on the latest timetable, such as scheduling notifications or updating widgets.
	 */
	suspend fun onNewTimetable(user: User, timetable: Timetable)
}
