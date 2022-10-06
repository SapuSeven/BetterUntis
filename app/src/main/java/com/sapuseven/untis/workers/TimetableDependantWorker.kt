package com.sapuseven.untis.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.data.timetable.TimegridItem
import com.sapuseven.untis.helpers.config.stringDataStore
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.helpers.timetable.TimetableLoader
import com.sapuseven.untis.models.untis.UntisDate
import com.sapuseven.untis.ui.preferences.decodeStoredTimetableValue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.completeWith
import org.joda.time.LocalDate
import java.lang.ref.WeakReference

abstract class TimetableDependantWorker(
	context: Context,
	params: WorkerParameters
) : CoroutineWorker(context, params) {

	protected suspend fun loadPersonalTimetableElement(
		user: UserDatabase.User
	): Pair<Int, String>? {
		val customPersonalTimetable = decodeStoredTimetableValue(
			applicationContext.stringDataStore(
				user.id,
				"preference_timetable_personal_timetable",
				defaultValue = ""
			).getValue()
		)

		val elemId = customPersonalTimetable?.id ?: user.userData.elemId
		val elemType = customPersonalTimetable?.type ?: user.userData.elemType ?: ""

		return if (TimetableDatabaseInterface.Type.values().find { it.name == elemType } == null)
			null // Anonymous / no custom personal timetable
		else
			elemId to elemType
	}

	protected suspend fun loadTimetable(
		user: UserDatabase.User,
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
	}
}

/**
 * Merges all values from contemporaneous lessons.
 * After this operation, every time period only has a single lesson containing all subjects, teachers, rooms and classes.
 */
internal fun List<TimegridItem>.merged(): List<TimegridItem> = this.groupBy { it.startDateTime }
	.map { it.value.reduce { item1, item2 -> item1.mergeValuesWith(item2); item1 } }

/**
 * Creates a copy of a zipped list with the very last element duplicated into a new Pair whose second element is null.
 */
internal fun <E> List<Pair<E?, E?>>.withLast(): List<Pair<E?, E?>> =
	if (this.isEmpty()) this
	else this.toMutableList().apply { add(Pair(this.last().second, null)) }.toList()
