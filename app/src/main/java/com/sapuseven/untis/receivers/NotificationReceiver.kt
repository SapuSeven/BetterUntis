package com.sapuseven.untis.receivers

import android.Manifest
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.sapuseven.untis.R
import com.sapuseven.untis.activities.MainActivity
import com.sapuseven.untis.data.settings.model.UserSettings
import com.sapuseven.untis.ui.pages.settings.GlobalSettingsRepository
import com.sapuseven.untis.ui.pages.settings.UserSettingsRepository
import com.sapuseven.untis.workers.NotificationSetupWorker.Companion.CHANNEL_ID_BREAKINFO
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalTime
import javax.inject.Inject

@AndroidEntryPoint
class NotificationReceiver @Inject constructor(
	val settingsRepository: UserSettingsRepository
) : BroadcastReceiver() {
	companion object {
		private const val LOG_TAG = "NotificationReceiver"

		const val EXTRA_BOOLEAN_CLEAR = "com.sapuseven.untis.notifications.clear"
		const val EXTRA_BOOLEAN_FIRST = "com.sapuseven.untis.notifications.first"
		const val EXTRA_INT_ID = "com.sapuseven.untis.notifications.id"
		const val EXTRA_INT_BREAK_END_TIME = "com.sapuseven.untis.notifications.breakEndTimeSeconds"
		const val EXTRA_LONG_USER_ID = "com.sapuseven.untis.notifications.userId"
		const val EXTRA_STRING_BREAK_END_TIME = "com.sapuseven.untis.notifications.breakEndTime"
		const val EXTRA_STRING_NEXT_SUBJECT = "com.sapuseven.untis.notifications.nextSubject"
		const val EXTRA_STRING_NEXT_SUBJECT_LONG = "com.sapuseven.untis.notifications.nextSubjectLong"
		const val EXTRA_STRING_NEXT_ROOM = "com.sapuseven.untis.notifications.nextRoom"
		const val EXTRA_STRING_NEXT_ROOM_LONG = "com.sapuseven.untis.notifications.nextRoomLong"
		const val EXTRA_STRING_NEXT_TEACHER = "com.sapuseven.untis.notifications.nextTeacher"
		const val EXTRA_STRING_NEXT_TEACHER_LONG = "com.sapuseven.untis.notifications.nextTeacherLong"
		const val EXTRA_STRING_NEXT_CLASS = "com.sapuseven.untis.notifications.nextClass"
		const val EXTRA_STRING_NEXT_CLASS_LONG = "com.sapuseven.untis.notifications.nextClassLong"
	}

	override fun onReceive(context: Context, intent: Intent) = runBlocking {
		Log.d(LOG_TAG, "NotificationReceiver received")

		val userId = intent.getLongExtra(EXTRA_LONG_USER_ID, -1)
		val settings = settingsRepository.getAllSettings().first()
		val userSettings = settings.userSettingsMap.getOrDefault(userId, settingsRepository.getSettingsDefaults())

		if (intent.getBooleanExtra(EXTRA_BOOLEAN_CLEAR, false)) {
			Log.d(
				LOG_TAG,
				"Attempting to cancel notification #${intent.getIntExtra(EXTRA_INT_ID, -1)}"
			)
			with(NotificationManagerCompat.from(context)) {
				cancel(intent.getIntExtra(EXTRA_INT_ID, -1))
			}
		} else {
			if (!userSettings.notificationsEnable) return@runBlocking // Notifications disabled

			if (LocalTime.ofSecondOfDay(intent.getIntExtra(EXTRA_INT_BREAK_END_TIME, 0).toLong())
					.isBefore(LocalTime.now())
			) return@runBlocking // Break is already over

			val pendingIntent = PendingIntent.getActivity(
				context,
				0,
				Intent(context, MainActivity::class.java),
				FLAG_IMMUTABLE
			) // TODO: Ensure that the appropriate profile is shown

			val title = context.getString(
				if (intent.getBooleanExtra(
						EXTRA_BOOLEAN_FIRST,
						false
					)
				) R.string.notifications_text_first_title else R.string.notifications_text_title,
				intent.getStringExtra(EXTRA_STRING_BREAK_END_TIME)
			)
			val message = buildMessage(
				null,
				intent,
				context.getString(R.string.notifications_text_message_separator),
				userSettings.notificationsVisibilitySubjects,
				userSettings.notificationsVisibilityRooms,
				userSettings.notificationsVisibilityTeachers,
				userSettings.notificationsVisibilityClasses
			)
			val longMessage = buildMessage(
				context,
				intent,
				"\n",
				userSettings.notificationsVisibilitySubjects,
				userSettings.notificationsVisibilityRooms,
				userSettings.notificationsVisibilityTeachers,
				userSettings.notificationsVisibilityClasses
			)

			val builder = NotificationCompat.Builder(context, CHANNEL_ID_BREAKINFO)
				.setContentTitle(title)
				.setContentText(message)
				.setSmallIcon(R.drawable.notification_clock)
				.setContentIntent(pendingIntent)
				.setStyle(NotificationCompat.BigTextStyle().bigText(longMessage))
				.setAutoCancel(false)
				.setOngoing(true)
				.setCategory(NotificationCompat.CATEGORY_STATUS)

			with(NotificationManagerCompat.from(context)) {
				if (ActivityCompat.checkSelfPermission(
						context,
						Manifest.permission.POST_NOTIFICATIONS
					) != PackageManager.PERMISSION_GRANTED
				) {
					Log.e(LOG_TAG, "Notification permission not granted!")
					return@runBlocking
				}
				notify(intent.getIntExtra(EXTRA_INT_ID, -1), builder.build())
			}
			Log.d(LOG_TAG, "Notification delivered: $title")
		}
	}

	private fun buildMessage(
		context: Context?,
		intent: Intent,
		separator: String,
		visibilitySubjects: String,
		visibilityRooms: String,
		visibilityTeachers: String,
		visibilityClasses: String
	) = listOfNotNull(
		if (intent.getStringExtra(EXTRA_STRING_NEXT_SUBJECT)?.isBlank() != false) null else
			when (visibilitySubjects) {
				"short" -> context?.getString(
					R.string.notifications_text_message_subjects,
					intent.getStringExtra(EXTRA_STRING_NEXT_SUBJECT)
				)
					?: intent.getStringExtra(EXTRA_STRING_NEXT_SUBJECT)

				"long" -> context?.getString(
					R.string.notifications_text_message_subjects,
					intent.getStringExtra(EXTRA_STRING_NEXT_SUBJECT_LONG)
				)
					?: intent.getStringExtra(EXTRA_STRING_NEXT_SUBJECT_LONG)

				else -> null
			},
		if (intent.getStringExtra(EXTRA_STRING_NEXT_ROOM)?.isBlank() != false) null else
			when (visibilityRooms) {
				"short" -> context?.getString(
					R.string.notifications_text_message_rooms,
					intent.getStringExtra(EXTRA_STRING_NEXT_ROOM)
				)
					?: intent.getStringExtra(EXTRA_STRING_NEXT_ROOM)

				"long" -> context?.getString(
					R.string.notifications_text_message_rooms,
					intent.getStringExtra(EXTRA_STRING_NEXT_ROOM_LONG)
				)
					?: intent.getStringExtra(EXTRA_STRING_NEXT_ROOM_LONG)

				else -> null
			},
		if (intent.getStringExtra(EXTRA_STRING_NEXT_TEACHER)?.isBlank() != false) null else
			when (visibilityTeachers) {
				"short" -> context?.getString(
					R.string.notifications_text_message_teachers,
					intent.getStringExtra(EXTRA_STRING_NEXT_TEACHER)
				)
					?: intent.getStringExtra(EXTRA_STRING_NEXT_TEACHER)

				"long" -> context?.getString(
					R.string.notifications_text_message_teachers,
					intent.getStringExtra(EXTRA_STRING_NEXT_TEACHER_LONG)
				)
					?: intent.getStringExtra(EXTRA_STRING_NEXT_TEACHER_LONG)

				else -> null
			},
		if (intent.getStringExtra(EXTRA_STRING_NEXT_CLASS)?.isBlank() != false) null else
			when (visibilityClasses) {
				"short" -> context?.getString(
					R.string.notifications_text_message_classes,
					intent.getStringExtra(EXTRA_STRING_NEXT_CLASS)
				)
					?: intent.getStringExtra(EXTRA_STRING_NEXT_CLASS)

				"long" -> context?.getString(
					R.string.notifications_text_message_classes,
					intent.getStringExtra(EXTRA_STRING_NEXT_CLASS_LONG)
				)
					?: intent.getStringExtra(EXTRA_STRING_NEXT_CLASS_LONG)

				else -> null
			}
	).joinToString(separator)
}
