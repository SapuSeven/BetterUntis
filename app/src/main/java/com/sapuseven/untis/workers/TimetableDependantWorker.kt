package com.sapuseven.untis.workers

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sapuseven.untis.api.model.untis.timetable.Period
import com.sapuseven.untis.api.model.untis.timetable.PeriodElement
import com.sapuseven.untis.data.database.entities.User
import com.sapuseven.untis.data.repository.MasterDataRepository
import com.sapuseven.untis.data.repository.TimetableRepository
import com.sapuseven.untis.data.settings.model.UserSettings
import com.sapuseven.untis.models.PeriodItem
import com.sapuseven.untis.models.mergeValuesWith
import com.sapuseven.untis.ui.preferences.toPeriodElement
import crocodile8.universal_cache.FromCache
import kotlinx.coroutines.flow.last
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/*
 * This class provides base functions to load timetables for the current day
 * and process its items.
 */
abstract class TimetableDependantWorker(
	context: Context,
	params: WorkerParameters,
	private val timetableRepository: TimetableRepository,
) : CoroutineWorker(context, params) {
	companion object {
		fun getPersonalTimetableElement(user: User, userSettings: UserSettings): PeriodElement? {
			return userSettings.timetablePersonalTimetable?.toPeriodElement()
				?: user.userData.getPeriodElement()
		}
	}

	protected suspend fun loadTimetable(
		user: User,
		element: PeriodElement,
		fromCache: FromCache
	): List<Period> {
		val currentDate = LocalDate.now()

		// TODO This doesn't take user id into account
		return timetableRepository.timetableSource().get(
			params = TimetableRepository.TimetableParams(
				element.id,
				element.type,
				currentDate,
				currentDate,
			),
			fromCache = fromCache,
			maxAge = 60 * 60 * 1000,
			additionalKey = user.id
		).last()
	}

	internal fun canAutoMute(): Boolean {
		val notificationManager = applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
		return (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || notificationManager.isNotificationPolicyAccessGranted)
	}

	internal fun canPostNotifications(): Boolean {
		val notificationManager = applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
		return (Build.VERSION.SDK_INT < Build.VERSION_CODES.N || notificationManager.areNotificationsEnabled())
	}

	/**
	 * Merges all values from contemporaneous lessons.
	 * After this operation, every time period only has a single lesson containing all subjects, teachers, rooms and classes.
	 */
	internal fun List<Period>.merged(masterDataRepository: MasterDataRepository): List<PeriodItem> = this
		.map { PeriodItem(masterDataRepository, it) }
		.groupBy { it.originalPeriod.startDateTime }
		.map { it.value.reduce { item1, item2 -> item1.mergeValuesWith(item2); item1 } }

	/**
	 * Creates a copy of a zipped list with the very last element duplicated into a new Pair whose second element is null.
	 */
	internal fun <E> List<Pair<E?, E?>>.withLast(): List<Pair<E?, E?>> =
		if (this.isEmpty()) this
		else this.toMutableList().apply { add(Pair(this.last().second, null)) }.toList()
}

internal fun AlarmManager.setBest(time: LocalDateTime, pendingIntent: PendingIntent) {
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && canScheduleExactAlarms()) {
		setExact(
			AlarmManager.RTC_WAKEUP,
			time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
			pendingIntent
		)
	} else {
		setWindow(
			AlarmManager.RTC_WAKEUP,
			time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
			600_000, // 10 minutes
			pendingIntent
		)
	}
}
