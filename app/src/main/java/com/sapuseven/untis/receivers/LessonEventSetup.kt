package com.sapuseven.untis.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.data.timetable.TimegridItem
import com.sapuseven.untis.helpers.config.PreferenceManager
import com.sapuseven.untis.helpers.config.PreferenceUtils
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.helpers.timetable.TimetableLoader
import com.sapuseven.untis.interfaces.TimetableDisplay
import com.sapuseven.untis.models.untis.UntisDate
import com.sapuseven.untis.preferences.ElementPickerPreference
import org.joda.time.LocalDate
import java.lang.ref.WeakReference

/**
 * A broadcast receiver template that sets up broadcasts that fire on start and end of any lesson on the current day.
 */
abstract class LessonEventSetup : BroadcastReceiver() {
	private lateinit var profileUser: UserDatabase.User
	private lateinit var timetableDatabaseInterface: TimetableDatabaseInterface
	private lateinit var preferenceManager: PreferenceManager

	companion object {
		const val EXTRA_LONG_PROFILE_ID = "com.sapuseven.untis.receivers.profileid"
	}

	override fun onReceive(context: Context, intent: Intent) {
		preferenceManager = PreferenceManager(context)

		loadDatabase(context, intent.getLongExtra(EXTRA_LONG_PROFILE_ID, 0))
		if (::profileUser.isInitialized) loadTimetable(context)
	}


	private fun loadDatabase(context: Context, profileId: Long) {
		val userDatabase = UserDatabase.createInstance(context)
		userDatabase.getUser(profileId)?.let {
			profileUser = it
			timetableDatabaseInterface = TimetableDatabaseInterface(userDatabase, it.id ?: -1)
		}
	}

	private fun loadTimetable(context: Context) {
		Log.d("NotificationSetup", "loadTimetable for user ${profileUser.id}")

		val currentDate = UntisDate.fromLocalDate(LocalDate.now())

		val targetTimetable = createPersonalTimetable()
		targetTimetable?.let {
			val target = TimetableLoader.TimetableLoaderTarget(currentDate, currentDate, it.second, it.first)
			val proxyHost = preferenceManager.defaultPrefs.getString("preference_connectivity_proxy_host", null)
			lateinit var timetableLoader: TimetableLoader
			timetableLoader = TimetableLoader(WeakReference(context), object : TimetableDisplay {
				override fun addTimetableItems(items: List<TimegridItem>, startDate: UntisDate, endDate: UntisDate, timestamp: Long) {
					onLoadingSuccess(context, items)
				}

				override fun onTimetableLoadingError(requestId: Int, code: Int?, message: String?) {
					when (code) {
						TimetableLoader.CODE_CACHE_MISSING -> timetableLoader.repeat(requestId, TimetableLoader.FLAG_LOAD_SERVER, proxyHost)
						else -> {
							onLoadingError(context, requestId, code, message)
						}
					}
				}
			}, profileUser, timetableDatabaseInterface)
			timetableLoader.load(target, TimetableLoader.FLAG_LOAD_CACHE, proxyHost)
		}
	}

	private fun createPersonalTimetable(): Pair<String, Int>? {
		@Suppress("RemoveRedundantQualifierName")
		val customType = TimetableDatabaseInterface.Type.valueOf(PreferenceUtils.getPrefString(
				preferenceManager,
				"preference_timetable_personal_timetable${ElementPickerPreference.KEY_SUFFIX_TYPE}",
				TimetableDatabaseInterface.Type.SUBJECT.toString()
		) ?: TimetableDatabaseInterface.Type.SUBJECT.toString())

		if (customType === TimetableDatabaseInterface.Type.SUBJECT) {
			profileUser.userData.elemType?.let { type ->
				return type to profileUser.userData.elemId
			} ?: run {
				return null
			}
		} else {
			val customId = preferenceManager.defaultPrefs.getInt("preference_timetable_personal_timetable${ElementPickerPreference.KEY_SUFFIX_ID}", -1)
			return customType.toString() to customId
		}
	}

	abstract fun onLoadingSuccess(context: Context, items: List<TimegridItem>)

	abstract fun onLoadingError(context: Context, requestId: Int, code: Int?, message: String?)
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
