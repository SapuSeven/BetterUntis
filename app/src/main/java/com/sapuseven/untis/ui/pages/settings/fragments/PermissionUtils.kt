package com.sapuseven.untis.ui.pages.settings.fragments

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
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
import com.sapuseven.untis.R
import com.sapuseven.untis.ui.common.MessageBubble


@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun ScheduleExactAlarmInfoMessage(
	context: Context = LocalContext.current,
	alarmManager: AlarmManager? = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager,
	visible: Boolean = true
) {
	var canScheduleExactAlarms by rememberSaveable { mutableStateOf(alarmManager?.canScheduleExactAlarms() == true) }

	val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
		canScheduleExactAlarms = alarmManager?.canScheduleExactAlarms() == true
	}

	AnimatedVisibility(
		visible = visible && !canScheduleExactAlarms,
		enter = fadeIn() + expandVertically(),
		exit = shrinkVertically() + fadeOut()
	) {
		MessageBubble(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 16.dp, vertical = 8.dp)
				.clickable {
					launcher.launch(Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
				},
			icon = {
				Icon(
					painter = painterResource(id = R.drawable.all_warning),
					contentDescription = stringResource(id = R.string.all_warning)
				)
			},
			messageText = R.string.preference_notifications_exact_alarms_unavailable,
			messageTextRaw = stringResource(
				R.string.preference_notifications_exact_alarms_unavailable_desc, stringResource(
					R.string.app_name)
			)
		)
	}
}
