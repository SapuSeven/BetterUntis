package com.sapuseven.untis.worker

import android.util.Log
import com.sapuseven.untis.core.domain.cache.FromCache
import com.sapuseven.untis.core.domain.repository.TimetableRepository
import com.sapuseven.untis.core.domain.worker.TimetableActionService
import com.sapuseven.untis.core.domain.worker.TimetableHandler
import com.sapuseven.untis.core.model.user.User
import com.sapuseven.untis.worker.DailyWorker.Companion.TAG_DAILY_WORK
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject
import javax.inject.Singleton

// In :app module
@Singleton
class TimetableActionServiceImpl @Inject constructor(
	private val timetableRepository: TimetableRepository,
	private val handlers: Set<@JvmSuppressWildcards TimetableHandler>,
	private val clock: Clock = Clock.System,
	private val zone: TimeZone = TimeZone.currentSystemDefault(),
) : TimetableActionService {
	override suspend fun triggerActions(user: User) {
		val element = user.element // TODO: Honor personal timetable setting
			?: return // Anonymous / no custom personal timetable

		try {
			val today = clock.todayIn(zone)
			val timetable = timetableRepository.getTimetable(
				user,
				TimetableRepository.TimetableParams(
					element.id,
					element.type,
					today,
					today
				),
				FromCache.NEVER
			).firstOrNull() ?: return

			handlers.forEach { handler ->
				if (handler.isEnabled(user)) {
					try {
						handler.onNewTimetable(user, timetable)
					} catch (e: Exception) {
						Log.e(
							TAG_DAILY_WORK,
							"Handler ${handler::class.simpleName} failed for user ${user.id}",
							e
						)
					}
				}
			}
		} catch (e: Exception) {
			Log.e(TAG_DAILY_WORK, "Timetable loading failed for user ${user.id}", e)
		}
	}
}
