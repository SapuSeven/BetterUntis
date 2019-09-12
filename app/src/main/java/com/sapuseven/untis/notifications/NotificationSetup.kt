package com.sapuseven.untis.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.util.Log
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.data.timetable.TimegridItem
import com.sapuseven.untis.helpers.DateTimeUtils
import com.sapuseven.untis.helpers.config.PreferenceManager
import com.sapuseven.untis.helpers.config.PreferenceUtils
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.helpers.timetable.TimetableLoader
import com.sapuseven.untis.interfaces.TimetableDisplay
import com.sapuseven.untis.models.untis.UntisDate
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import java.lang.ref.WeakReference


/**
 * This receiver is responsible for setting up alarms for every notification of the current day.
 */
class NotificationSetup : BroadcastReceiver() {
	private lateinit var timetableDatabaseInterface: TimetableDatabaseInterface
	private var profileUser: UserDatabase.User? = null

	companion object {
		const val EXTRA_LONG_PROFILE_ID = "profile_id"
	}

	override fun onReceive(context: Context, intent: Intent) {
		Log.d("NotificationSetup", "NotificationSetup received")

		val preferenceManager = PreferenceManager(context)
		if (!PreferenceUtils.getPrefBool(preferenceManager, "preference_notifications_enable"))
			return

		loadDatabase(context, 1/*intent.getLongExtra(EXTRA_LONG_PROFILE_ID, -1)*/) // TODO: Add setting to select user
		profileUser?.run { loadTimetable(context) }
	}

	private fun loadDatabase(context: Context, profileId: Long) {
		val userDatabase = UserDatabase.createInstance(context)
		profileUser = userDatabase.getUser(profileId)
		// TODO: Check if user not found and handle error (prevent call to loadTimetable)
		timetableDatabaseInterface = TimetableDatabaseInterface(userDatabase, profileUser?.id ?: -1)
	}

	private fun loadTimetable(context: Context) {
		Log.d("NotificationSetup", "loadTimetable for user ${profileUser?.id}")

		val currentDate = UntisDate.fromLocalDate(LocalDate.now())

		val target = TimetableLoader.TimetableLoaderTarget(currentDate, currentDate, profileUser!!.userData.elemId, profileUser!!.userData.elemType
				?: "STUDENT")

		TimetableLoader(WeakReference(context), object : TimetableDisplay {
			override fun addData(items: List<TimegridItem>, startDate: UntisDate, endDate: UntisDate, timestamp: Long) {
				setupNotifications(context, items)
			}

			override fun onError(requestId: Int, code: Int?, message: String?) {
				// TODO: Handle error
				Log.d("NotificationSetup", "loadTimetable error $code for $requestId: $message")
			}
		}, profileUser!!, timetableDatabaseInterface)
				.load(target, TimetableLoader.FLAG_LOAD_CACHE)
	}

	private fun setupNotifications(context: Context, items: List<TimegridItem>) {
		items.sortedBy { it.startDateTime }.zipWithNext().forEach { item ->
			// TODO: Check if multi-hour lesson notifications are enabled
			Log.d("NotificationSetup", "found ${item.first.periodData.getShortTitle()}")
			val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
			val id = item.first.endDateTime.millisOfDay / 1000

			if (item.second.startDateTime.millisOfDay < LocalDateTime.now().millisOfDay) return@forEach // ignore lessons in the past

			// TODO: Correct extra key names
			val intent = Intent(context, NotificationReceiver::class.java)
					.putExtra("id", id)
					.putExtra("breakEndTime", item.second.startDateTime.toString(DateTimeUtils.shortDisplayableTime()))
					.putExtra("nextSubject", item.second.periodData.getShortTitle())
					.putExtra("nextSubjectLong", item.second.periodData.getLongTitle())
					.putExtra("nextRoom", item.second.periodData.getShortRooms())
					.putExtra("nextRoomLong", item.second.periodData.getLongRooms())
					.putExtra("nextTeacher", item.second.periodData.getShortTeachers())
					.putExtra("nextTeacherLong", item.second.periodData.getLongTeachers())
					.putExtra("nextClass", item.second.periodData.getShortClasses())
					.putExtra("nextClassLong", item.second.periodData.getLongClasses())

			val pendingIntent = PendingIntent.getBroadcast(context, item.first.endDateTime.millisOfDay, intent, 0)
			alarmManager.setExact(AlarmManager.RTC_WAKEUP, item.first.endDateTime.millis, pendingIntent)
			Log.d("NotificationSetup", "${item.first.periodData.getShortTitle()} scheduled for ${item.first.endDateTime}")

			val deletingIntent = Intent(context, NotificationReceiver::class.java)
					.putExtra("id", id)
					.putExtra("clear", true)
			val deletingPendingIntent = PendingIntent.getBroadcast(context, item.first.endDateTime.millisOfDay + 1, deletingIntent, 0)
			alarmManager.setExact(AlarmManager.RTC_WAKEUP, item.second.startDateTime.millis, deletingPendingIntent)
			Log.d("NotificationSetup", "${item.first.periodData.getShortTitle()} delete scheduled for ${item.second.startDateTime}")
		}
	}
}

private val LocalDateTime.millis: Long
	get() = this.toDateTime(DateTimeZone.getDefault()).toInstant().millis
