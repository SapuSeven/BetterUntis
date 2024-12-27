package com.sapuseven.untis.activities

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.unit.dp
import androidx.work.WorkManager
import coil.compose.AsyncImage
import com.sapuseven.untis.R
import com.sapuseven.untis.data.database.entities.User
import com.sapuseven.untis.models.github.GithubUser
import com.sapuseven.untis.preferences.*
import com.sapuseven.untis.receivers.AutoMuteReceiver
import com.sapuseven.untis.receivers.AutoMuteReceiver.Companion.EXTRA_BOOLEAN_MUTE
import com.sapuseven.untis.workers.AutoMuteSetupWorker
import com.sapuseven.untis.workers.NotificationSetupWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SettingsActivity : BaseComposeActivity() {
	companion object {
		const val EXTRA_STRING_PREFERENCE_ROUTE = "com.sapuseven.untis.activities.settings.route"
		const val EXTRA_STRING_PREFERENCE_HIGHLIGHT =
			"com.sapuseven.untis.activities.settings.highlight"

		private const val URL_GITHUB_REPOSITORY = "https://github.com/SapuSeven/BetterUntis"
		private const val URL_GITHUB_REPOSITORY_API =
			"https://api.github.com/repos/SapuSeven/BetterUntis"
		private const val URL_WIKI_PROXY = "$URL_GITHUB_REPOSITORY/wiki/Proxy"
	}

	@OptIn(ExperimentalMaterial3Api::class)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
	}

	private fun enqueueNotificationSetup(user: User) {
		NotificationSetupWorker.enqueue(
			WorkManager.getInstance(this@SettingsActivity),
			user
		)
	}

	private fun canPostNotifications(): Boolean {
		val notificationManager =
			applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
		return (Build.VERSION.SDK_INT < Build.VERSION_CODES.N || notificationManager.areNotificationsEnabled())
	}

	private fun canScheduleExactAlarms(): Boolean {
		val alarmManager = applicationContext.getSystemService(ALARM_SERVICE) as AlarmManager
		return (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms())
	}

	@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
	@Composable
	fun Contributor(
		githubUser: GithubUser,
		onClick: () -> Unit
	) {
		ListItem(
			modifier = Modifier.clickable(onClick = onClick),
			headlineContent = {
				Text(githubUser.login)
			},
			supportingContent = {
				Text(
					pluralStringResource(
						id = R.plurals.preferences_contributors_contributions,
						count = githubUser.contributions,
						githubUser.contributions
					)
				)
			},
			leadingContent = {
				AsyncImage(
					model = githubUser.avatar_url,
					contentDescription = "UserImage", //TODO: Extract string resource
					modifier = Modifier.size(48.dp)
				)
			}
		)
	}

	private fun updateAutoMutePref(
		user: User,
		scope: CoroutineScope,
		autoMutePref: UntisPreferenceDataStore<Boolean>,
		enable: Boolean = false
	) {
		scope.launch {
			val permissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				(getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).isNotificationPolicyAccessGranted
			} else true


			if (autoMutePref.getValue() && !permissionGranted)
				autoMutePref.saveValue(false)

			if (enable && permissionGranted) {
				autoMutePref.saveValue(true)
				AutoMuteSetupWorker.enqueue(
					WorkManager.getInstance(this@SettingsActivity),
					user
				)
			}
		}
	}

	private fun requestAutoMutePermission(activityLauncher: ActivityResultLauncher<Intent>): Boolean {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			(getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).apply {
				return if (!isNotificationPolicyAccessGranted) {
					activityLauncher.launch(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
					false
				} else true
			}
		} else return true
	}

	private fun clearNotifications() =
		(getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancelAll()


	private fun disableAutoMute() {
		sendBroadcast(
			Intent(applicationContext, AutoMuteReceiver::class.java)
				.putExtra(EXTRA_BOOLEAN_MUTE, false)
		)
	}
}
