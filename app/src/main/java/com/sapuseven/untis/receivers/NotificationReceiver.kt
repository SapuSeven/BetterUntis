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
import com.sapuseven.untis.api.model.untis.MessageOfDay
import com.sapuseven.untis.data.repository.UserSettingsRepository
import com.sapuseven.untis.data.settings.model.NotificationVisibility
import com.sapuseven.untis.data.settings.model.UserSettings
import com.sapuseven.untis.domain.GetMessagesOfDayUseCase
import com.sapuseven.untis.workers.NotificationSetupWorker.Companion.CHANNEL_ID_BREAKINFO
import com.sapuseven.untis.workers.NotificationSetupWorker.Companion.CHANNEL_ID_FIRSTLESSON
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalTime
import javax.inject.Inject

@AndroidEntryPoint
class NotificationReceiver : BroadcastReceiver() {
	@Inject
	lateinit var userSettingsRepository: UserSettingsRepository

	@Inject
	lateinit var getMessages: GetMessagesOfDayUseCase

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
		val userSettings = userSettingsRepository.getSettings(userId).first()

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

			val isFirst = intent.getBooleanExtra(EXTRA_BOOLEAN_FIRST, false)

			sendBreakNotification(context, intent, userSettings)

			if (isFirst) {
				// TODO This doesn't take user id into account
				getMessages().first().fold(
					onSuccess = { messages ->
						if (messages.isNotEmpty()) {
							sendMessagesNotification(context, messages)
						}
					},
					onFailure = { exception ->
						//sendErrorNotification(context)
					}
				)
			}
		}
	}

	private fun sendMessagesNotification(context: Context, messages: List<MessageOfDay>) {
		val builder = NotificationCompat.Builder(context, CHANNEL_ID_FIRSTLESSON)
			.setContentTitle(context.getString(R.string.notifications_text_motd_title))
			.setContentText(context.getString(R.string.notifications_text_motd_message, messages.size))
			.setSmallIcon(R.drawable.infocenter_messages)
			//TODO .setContentIntent(pendingIntent)
			.setStyle(NotificationCompat.BigTextStyle().bigText(messages.joinToString("\n") { it.subject }))
			.setAutoCancel(false)
			.setCategory(NotificationCompat.CATEGORY_EMAIL)

		sendNotification(context, builder, 1)
	}

	private fun sendBreakNotification(context: Context, intent: Intent, userSettings: UserSettings) {
		val isFirst = intent.getBooleanExtra(EXTRA_BOOLEAN_FIRST, false)

		val title = context.getString(
			if (isFirst) R.string.notifications_text_first_title else R.string.notifications_text_title,
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

		// Action on notification click
		// TODO: Ensure that the appropriate profile is shown
		val pendingIntent = PendingIntent.getActivity(
			context,
			0,
			Intent(context, MainActivity::class.java),
			FLAG_IMMUTABLE
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

		if (sendNotification(context, builder, intent.getIntExtra(EXTRA_INT_ID, -1)))
			Log.d(LOG_TAG, "Break notification delivered: $title")
	}

	private fun sendNotification(context: Context, builder: NotificationCompat.Builder, id: Int): Boolean {
		with(NotificationManagerCompat.from(context)) {
			if (ActivityCompat.checkSelfPermission(
					context,
					Manifest.permission.POST_NOTIFICATIONS
				) != PackageManager.PERMISSION_GRANTED
			) {
				Log.e(LOG_TAG, "Notification permission not granted!")
				return false
			}
			notify(id, builder.build())
			return true
		}
	}

	private fun buildMessage(
		context: Context?,
		intent: Intent,
		separator: String,
		visibilitySubjects: NotificationVisibility,
		visibilityRooms: NotificationVisibility,
		visibilityTeachers: NotificationVisibility,
		visibilityClasses: NotificationVisibility
	) = listOfNotNull(
		if (intent.getStringExtra(EXTRA_STRING_NEXT_SUBJECT)?.isBlank() != false) null else
			when (visibilitySubjects) {
				NotificationVisibility.SHORT -> context?.getString(
					R.string.notifications_text_message_subjects,
					intent.getStringExtra(EXTRA_STRING_NEXT_SUBJECT)
				)
					?: intent.getStringExtra(EXTRA_STRING_NEXT_SUBJECT)

				NotificationVisibility.LONG -> context?.getString(
					R.string.notifications_text_message_subjects,
					intent.getStringExtra(EXTRA_STRING_NEXT_SUBJECT_LONG)
				)
					?: intent.getStringExtra(EXTRA_STRING_NEXT_SUBJECT_LONG)

				else -> null
			},
		if (intent.getStringExtra(EXTRA_STRING_NEXT_ROOM)?.isBlank() != false) null else
			when (visibilityRooms) {
				NotificationVisibility.SHORT -> context?.getString(
					R.string.notifications_text_message_rooms,
					intent.getStringExtra(EXTRA_STRING_NEXT_ROOM)
				)
					?: intent.getStringExtra(EXTRA_STRING_NEXT_ROOM)

				NotificationVisibility.LONG -> context?.getString(
					R.string.notifications_text_message_rooms,
					intent.getStringExtra(EXTRA_STRING_NEXT_ROOM_LONG)
				)
					?: intent.getStringExtra(EXTRA_STRING_NEXT_ROOM_LONG)

				else -> null
			},
		if (intent.getStringExtra(EXTRA_STRING_NEXT_TEACHER)?.isBlank() != false) null else
			when (visibilityTeachers) {
				NotificationVisibility.SHORT -> context?.getString(
					R.string.notifications_text_message_teachers,
					intent.getStringExtra(EXTRA_STRING_NEXT_TEACHER)
				)
					?: intent.getStringExtra(EXTRA_STRING_NEXT_TEACHER)

				NotificationVisibility.LONG -> context?.getString(
					R.string.notifications_text_message_teachers,
					intent.getStringExtra(EXTRA_STRING_NEXT_TEACHER_LONG)
				)
					?: intent.getStringExtra(EXTRA_STRING_NEXT_TEACHER_LONG)

				else -> null
			},
		if (intent.getStringExtra(EXTRA_STRING_NEXT_CLASS)?.isBlank() != false) null else
			when (visibilityClasses) {
				NotificationVisibility.SHORT -> context?.getString(
					R.string.notifications_text_message_classes,
					intent.getStringExtra(EXTRA_STRING_NEXT_CLASS)
				)
					?: intent.getStringExtra(EXTRA_STRING_NEXT_CLASS)

				NotificationVisibility.LONG -> context?.getString(
					R.string.notifications_text_message_classes,
					intent.getStringExtra(EXTRA_STRING_NEXT_CLASS_LONG)
				)
					?: intent.getStringExtra(EXTRA_STRING_NEXT_CLASS_LONG)

				else -> null
			}
	).joinToString(separator)
}
