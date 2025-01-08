package com.sapuseven.untis.workers

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import com.sapuseven.untis.data.database.UserDatabase
import com.sapuseven.untis.data.database.entities.User
import com.sapuseven.untis.helpers.config.booleanDataStore
import com.sapuseven.untis.helpers.config.stringDataStore
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.helpers.timetable.TimetableLoader
import com.sapuseven.untis.ui.preferences.decodeStoredTimetableValue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.completeWith
import org.joda.time.LocalDate
import java.lang.ref.WeakReference

/*
 * This class provides base functions to load timetables for the current day.
 */
abstract class TimetableDependantWorker(
	context: Context,
	params: WorkerParameters
) : CoroutineWorker(context, params) {
	companion object {
		suspend fun loadPersonalTimetableElement(
			user: User,
			context: Context
		): Pair<Int, String>? {
			val customPersonalTimetable = decodeStoredTimetableValue(
				context.stringDataStore(
					user.id,
					"preference_timetable_personal_timetable",
					defaultValue = ""
				).getValue()
			)

			return null;// TODO
			/*val elemId = customPersonalTimetable?.id ?: user.userData.elemId
			val elemType = customPersonalTimetable?.type ?: user.userData.elemType ?: ElementType.SUBJECT

			return if (ElementType.values()
					.find { it.name == elemType } == null
			)
				null // Anonymous / no custom personal timetable
			else
				elemId to elemType*/
		}
	}

	/*protected suspend fun loadTimetable(
		user: User,
		timetableDatabaseInterface: TimetableDatabaseInterface,
		timetableElement: Pair<Int, String>,
		skipCache: Boolean = false
	): TimetableLoader.TimetableItems {
		val proxyHost = applicationContext.stringDataStore(
			user.id,
			"preference_connectivity_proxy_host",
			defaultValue = ""
		).getValue()

		val currentDate = UntisDate.fromLocalDate(LocalDate.now())

		val target = TimetableLoader.TimetableLoaderTarget(
			currentDate,
			currentDate,
			timetableElement.first,
			timetableElement.second
		)

		return CompletableDeferred<TimetableLoader.TimetableItems>().apply {
			TimetableLoader(
				context = WeakReference(applicationContext),
				user = user,
				timetableDatabaseInterface = timetableDatabaseInterface
			).loadAsync(target, proxyHost, loadFromCache = !skipCache, loadFromServer = skipCache) {
				completeWith(kotlin.Result.success(it))
			}
			completeWith(kotlin.Result.failure(Exception("Timetable loading failed")))
		}.await()
	}*/

	internal fun canAutoMute(): Boolean {
		val notificationManager =
			applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
		return (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || notificationManager.isNotificationPolicyAccessGranted)
	}

	internal fun canPostNotifications(): Boolean {
		val notificationManager =
			applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
		return (Build.VERSION.SDK_INT < Build.VERSION_CODES.N || notificationManager.areNotificationsEnabled())
	}

	internal fun canScheduleExactAlarms(): Boolean {
		return false
		/*TODO val alarmManager =
			applicationContext.getSystemService(ComponentActivity.ALARM_SERVICE) as AlarmManager
		return (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms())*/
	}

	internal suspend fun disablePreference(key: String) {
		Room.databaseBuilder(
			applicationContext,
			UserDatabase::class.java, "users"
		).build().userDao().getAll().map { it.id }.forEach { userId ->
			val pref = applicationContext.booleanDataStore(userId, key)
			pref.saveValue(false)
		}
	}
}

/**
 * Merges all values from contemporaneous lessons.
 * After this operation, every time period only has a single lesson containing all subjects, teachers, rooms and classes.
 */
//internal fun List<TimegridItem>.merged(): List<TimegridItem> = this.groupBy { it.startDateTime }
	//.map { it.value.reduce { item1, item2 -> item1.mergeValuesWith(item2); item1 } }

/**
 * Creates a copy of a zipped list with the very last element duplicated into a new Pair whose second element is null.
 */
internal fun <E> List<Pair<E?, E?>>.withLast(): List<Pair<E?, E?>> =
	if (this.isEmpty()) this
	else this.toMutableList().apply { add(Pair(this.last().second, null)) }.toList()
