package com.sapuseven.untis.ui.pages.settings.fragments

import android.os.Build
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.sapuseven.compose.protostore.ui.preferences.ListPreference
import com.sapuseven.compose.protostore.ui.preferences.NumericInputPreference
import com.sapuseven.compose.protostore.ui.preferences.Preference
import com.sapuseven.compose.protostore.ui.preferences.PreferenceGroup
import com.sapuseven.compose.protostore.ui.preferences.SwitchPreference
import com.sapuseven.untis.R
import com.sapuseven.untis.ui.common.disabled
import com.sapuseven.untis.ui.pages.settings.ScheduleExactAlarmInfoMessage
import com.sapuseven.untis.ui.pages.settings.SettingsScreenViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SettingsCategoryNotifications(viewModel: SettingsScreenViewModel) {
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
		val visible by viewModel.repository.getSettings().map { it.notificationsEnable }.collectAsState(initial = false)
		ScheduleExactAlarmInfoMessage(visible = visible)
	}

	PreferenceGroup(
		title = stringResource(R.string.preference_category_notifications_break)
	) {
		val scope = rememberCoroutineScope()

		val notificationPermissionsState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
			rememberPermissionState(android.Manifest.permission.POST_NOTIFICATIONS) {
				scope.launch {
					viewModel.repository.updateSettings {
						notificationsEnable = true
					}
				}
			}
		else null

		// TODO: This is used to make sure that the setting will match the permission status. Not sure if there is a better way to do this.
		LaunchedEffect(Unit) {
			notificationPermissionsState?.let {
				if (!it.status.isGranted) {
					viewModel.repository.updateSettings {
						notificationsEnable = false
					}
				}
			}
		}

		//val alarmManager = LocalContext.current.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
		/*listOfNotNull(
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager?.canScheduleExactAlarms() != true)
				android.Manifest.permission.SCHEDULE_EXACT_ALARM
			else null,
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)

			else null
		)*/

		SwitchPreference(
			title = { Text(stringResource(R.string.preference_notifications_enable)) },
			summary = { Text(stringResource(R.string.preference_notifications_enable_desc)) },
			settingsRepository = viewModel.repository,
			value = { it.notificationsEnable },
			onValueChange = {
				notificationsEnable = if (it) {
					if (notificationPermissionsState?.status?.isGranted != false) {
						//enqueueNotificationSetup()
						true
					} else {
						notificationPermissionsState.launchPermissionRequest()
						false
					}
				} else {
					//clearNotifications()
					false
				}
			}
		)

		SwitchPreference(
			title = { Text(stringResource(R.string.preference_notifications_multiple)) },
			summary = { Text(stringResource(R.string.preference_notifications_multiple_desc)) },
			enabledCondition = { it.notificationsEnable },
			settingsRepository = viewModel.repository,
			value = { it.notificationsInMultiple },
			onValueChange = {
				//enqueueNotificationSetup()
				notificationsInMultiple = it
			}
		)

		SwitchPreference(
			title = { Text(stringResource(R.string.preference_notifications_first_lesson)) },
			summary = { Text(stringResource(R.string.preference_notifications_first_lesson_desc)) },
			enabledCondition = { it.notificationsEnable },
			settingsRepository = viewModel.repository,
			value = { it.notificationsBeforeFirst },
			onValueChange = {
				//enqueueNotificationSetup()
				notificationsBeforeFirst = it
			}
		)

		NumericInputPreference(
			title = { Text(stringResource(R.string.preference_notifications_first_lesson_time)) },
			unit = stringResource(R.string.preference_notifications_first_lesson_time_unit),
			enabledCondition = { it.notificationsEnable && it.notificationsBeforeFirst },
			settingsRepository = viewModel.repository,
			value = { it.notificationsBeforeFirstTime },
			onValueChange = {
				//enqueueNotificationSetup()
				notificationsBeforeFirstTime = it
			}
		)
	}

	PreferenceGroup(stringResource(id = R.string.preference_category_notifications_visible_fields)) {
		ListPreference(
			title = { Text(stringResource(R.string.all_subjects)) },
			supportingContent = { value, enabled ->
				Text(
					value.second,
					modifier = Modifier.disabled(!enabled)
				)
			},
			leadingContent = {
				Icon(
					painter = painterResource(R.drawable.all_subject),
					contentDescription = null
				)
			},
			entries = stringArrayResource(id = R.array.preference_notifications_visibility_values),
			entryLabels = stringArrayResource(id = R.array.preference_notifications_visibility),
			enabledCondition = { it.notificationsEnable },
			settingsRepository = viewModel.repository,
			value = { it.notificationsVisibilitySubjects },
			onValueChange = { notificationsVisibilitySubjects = it }
		)

		ListPreference(
			title = { Text(stringResource(R.string.all_rooms)) },
			supportingContent = { value, enabled ->
				Text(
					value.second,
					modifier = Modifier.disabled(!enabled)
				)
			},
			leadingContent = {
				Icon(
					painter = painterResource(R.drawable.all_rooms),
					contentDescription = null
				)
			},
			entries = stringArrayResource(id = R.array.preference_notifications_visibility_values),
			entryLabels = stringArrayResource(id = R.array.preference_notifications_visibility),
			enabledCondition = { it.notificationsEnable },
			settingsRepository = viewModel.repository,
			value = { it.notificationsVisibilityRooms },
			onValueChange = { notificationsVisibilityRooms = it }
		)

		ListPreference(
			title = { Text(stringResource(R.string.all_teachers)) },
			supportingContent = { value, enabled ->
				Text(
					value.second,
					modifier = Modifier.disabled(!enabled)
				)
			},
			leadingContent = {
				Icon(
					painter = painterResource(R.drawable.all_teachers),
					contentDescription = null
				)
			},
			entries = stringArrayResource(id = R.array.preference_notifications_visibility_values),
			entryLabels = stringArrayResource(id = R.array.preference_notifications_visibility),
			enabledCondition = { it.notificationsEnable },
			settingsRepository = viewModel.repository,
			value = { it.notificationsVisibilityTeachers },
			onValueChange = { notificationsVisibilityTeachers = it }
		)

		ListPreference(
			title = { Text(stringResource(R.string.all_classes)) },
			supportingContent = { value, enabled ->
				Text(
					value.second,
					modifier = Modifier.disabled(!enabled)
				)
			},
			leadingContent = {
				Icon(
					painter = painterResource(R.drawable.all_classes),
					contentDescription = null
				)
			},
			entries = stringArrayResource(id = R.array.preference_notifications_visibility_values),
			entryLabels = stringArrayResource(id = R.array.preference_notifications_visibility),
			enabledCondition = { it.notificationsEnable },
			settingsRepository = viewModel.repository,
			value = { it.notificationsVisibilityClasses },
			onValueChange = { notificationsVisibilityClasses = it }
		)
	}
	Preference(
		title = { Text(stringResource(R.string.preference_notifications_clear)) },
		onClick = {
			//clearNotifications()
		},
		leadingContent = {
			Icon(
				painter = painterResource(R.drawable.settings_notifications_clear_all),
				contentDescription = null
			)
		}
	)
}
