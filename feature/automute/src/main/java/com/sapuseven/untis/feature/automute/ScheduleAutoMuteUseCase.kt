package com.sapuseven.untis.feature.automute

import android.util.Log
import com.sapuseven.untis.core.datastore.UserSettingsDataSource
import com.sapuseven.untis.core.domain.timetable.isCancelled
import com.sapuseven.untis.core.domain.timetable.merged
import com.sapuseven.untis.core.domain.timetable.subjects
import com.sapuseven.untis.core.domain.timetable.toShortString
import com.sapuseven.untis.core.domain.timetable.withLast
import com.sapuseven.untis.core.model.timetable.Timetable
import com.sapuseven.untis.core.model.user.User
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

class ScheduleAutoMuteUseCase @Inject constructor(
	private val userSettingsDataSource: UserSettingsDataSource,
	private val autoMuteRepository: AutoMuteRepository,
	private val clock: Clock = Clock.System,
	private val zone: TimeZone = TimeZone.currentSystemDefault(),
) {
	companion object {
		private val LOG_TAG = ScheduleAutoMuteUseCase::class.simpleName
	}

	suspend operator fun invoke(user: User, timetable: Timetable) {
		userSettingsDataSource.getSettings(user.id).first().let { settings ->
			if (!settings.automuteEnable) return@let

			val preparedItems = timetable.periods
				.merged()
				.sortedBy { it.startDateTime }
				.zipWithNext()
				.withLast()
				.map {
					// Map each lesson to the length of the subsequent break in minutes
					it.first to it.second?.startDateTime?.toInstant(zone)
						?.minus(it.first.endDateTime.toInstant(zone))
						?.inWholeMinutes
				}

			if (preparedItems.isEmpty()) {
				Log.d(LOG_TAG, "[user#${user.id}] No auto mute events to schedule")
				return@let
			}

			try {
				preparedItems.forEach {
					it.first.let { item ->
						if (item.endDateTime <= clock.now().toLocalDateTime(zone)) {
							Log.d(LOG_TAG, "[user#${user.id}] Auto mute skipped (lesson is in the past)")
							return@forEach
						}

						if (item.isCancelled() && !settings.automuteCancelledLessons) {
							Log.d(LOG_TAG, "[user#${user.id}] Auto mute skipped (lesson is cancelled)")
							return@forEach
						}

						val skipUnmute = (it.second != null && it.second!! < settings.automuteMinimumBreakLength)

						autoMuteRepository.scheduleAutoMute(
							user.id,
							item.startDateTime.toInstant(zone),
							item.endDateTime.toInstant(zone).takeIf { !skipUnmute }
						)
						Log.d(LOG_TAG,
						"[user#${user.id}] ${item.subjects.toShortString()} mute scheduled for ${item.startDateTime} (skipUnmute: $skipUnmute)"
						)
					}
				}
			} catch (e: Exception) {
				Log.e(LOG_TAG, "[user#${user.id}] Auto mute events couldn't be scheduled", e)
			}
		}
	}

	suspend fun isEnabled(user: User): Boolean {
		return userSettingsDataSource.getSettings(user.id).first().automuteEnable
			&& autoMuteRepository.canAutoMute()
	}
}
