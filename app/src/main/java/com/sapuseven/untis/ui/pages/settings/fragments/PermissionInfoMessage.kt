package com.sapuseven.untis.ui.pages.settings.fragments

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import android.provider.Settings.EXTRA_APP_PACKAGE
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sapuseven.untis.BuildConfig
import com.sapuseven.untis.R
import com.sapuseven.untis.ui.common.MessageBubble

@Composable
private fun PermissionInfoMessage(
	currentPermissionState: () -> Boolean,
	requestPermissionIntent: Intent,
	visible: Boolean = true,
	@StringRes primaryText: Int?,
	secondaryText: String
) {
	var isPermissionGranted by rememberSaveable { mutableStateOf(currentPermissionState()) }

	val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
		isPermissionGranted = currentPermissionState()
	}

	AnimatedVisibility(
		visible = visible && !isPermissionGranted,
		enter = fadeIn() + expandVertically(),
		exit = shrinkVertically() + fadeOut()
	) {
		MessageBubble(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 16.dp, vertical = 8.dp)
				.clickable { launcher.launch(requestPermissionIntent) },
			icon = {
				Icon(
					painter = painterResource(id = R.drawable.all_warning),
					contentDescription = stringResource(id = R.string.all_warning)
				)
			},
			messageText = primaryText,
			messageTextRaw = secondaryText
		)
	}
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun ScheduleExactAlarmInfoMessage(
	alarmManager: AlarmManager? = LocalContext.current.getSystemService(Context.ALARM_SERVICE) as? AlarmManager,
	visible: Boolean = true
) {
	PermissionInfoMessage(
		currentPermissionState = { alarmManager?.canScheduleExactAlarms() == true },
		requestPermissionIntent = Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM),
		visible = visible,
		primaryText = R.string.preference_notifications_exact_alarms_unavailable,
		secondaryText = stringResource(R.string.preference_notifications_exact_alarms_unavailable_desc, stringResource(R.string.app_name))
	)
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NotificationsInfoMessage(
	notificationManager: NotificationManager? = LocalContext.current.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager,
	visible: Boolean = true
) {
	PermissionInfoMessage(
		currentPermissionState = { notificationManager?.areNotificationsEnabled() == true },
		requestPermissionIntent = Intent(ACTION_APP_NOTIFICATION_SETTINGS).putExtra(EXTRA_APP_PACKAGE, BuildConfig.APPLICATION_ID),
		visible = visible,
		primaryText = R.string.preference_notifications_unavailable,
		secondaryText = stringResource(R.string.preference_notifications_unavailable_desc, stringResource(R.string.app_name))
	)
}
