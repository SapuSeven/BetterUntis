package com.sapuseven.untis.ui.pages.settings.fragments

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.sapuseven.compose.protostore.ui.preferences.Preference
import com.sapuseven.compose.protostore.ui.preferences.PreferenceGroup
import com.sapuseven.compose.protostore.ui.preferences.SliderPreference
import com.sapuseven.compose.protostore.ui.preferences.SwitchPreference
import com.sapuseven.untis.BuildConfig
import com.sapuseven.untis.R
import com.sapuseven.untis.ui.pages.settings.SettingsScreenViewModel
import io.sentry.Sentry
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Composable
fun SettingsCategoryGeneral(viewModel: SettingsScreenViewModel) {
	/* PreferenceGroup(stringResource(id = R.string.preference_category_general_behaviour)) {
	// Not supported, not planned. May be reconsidered if there is demand for this feature.
	SwitchPreference(
		title = { Text(stringResource(R.string.preference_double_tap_to_exit)) },
		settingsRepository = viewModel.repository,
		value = { it.exitConfirmation },
		onValueChange = { exitConfirmation = it }
	)

	// Not supported. May be reconsidered if there is demand for this feature.
	// Could be implemented by setting flingBehavior on the WeekView HorizontalPager
	SwitchPreference(
		title = { Text(stringResource(R.string.preference_flinging_enable)) },
		settingsRepository = viewModel.repository,
		value = { it.flingEnable },
		onValueChange = { flingEnable = it }
	)*/

	/* Not supported yet
	PreferenceGroup(stringResource(R.string.preference_category_general_week_display)) {
		WeekRangePreference(
			title = { Text(stringResource(R.string.preference_week_custom_range)) },
			settingsRepository = viewModel.repository,
			value = { it.weekCustomRangeList.toSet() },
			onValueChange = {
				clearWeekCustomRange()
				addAllWeekCustomRange(it)
			}
		)

		SwitchPreference(
			title = { Text(stringResource(R.string.preference_week_snap_to_days)) },
			summary = { Text(stringResource(R.string.preference_week_snap_to_days_summary)) },
			settingsRepository = viewModel.repository,
			value = { it.weekSnapToDays },
			onValueChange = { weekSnapToDays = it }
		)

		SliderPreference(
			title = { Text(stringResource(R.string.preference_week_display_length)) },
			summary = { Text(stringResource(R.string.preference_week_display_length_summary)) },
			valueRange = 0f..7f,
			steps = 6,
			enabledCondition = { it.weekSnapToDays },
			showSeekBarValue = true,
			settingsRepository = viewModel.repository,
			value = { it.weekCustomLength },
			onValueChange = { weekCustomLength = it }
		)
	}*/

	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
		val visible by viewModel.repository.getSettings().map { it.automuteEnable }.collectAsState(initial = false)
		ScheduleExactAlarmInfoMessage(
			visible = visible,
			primaryText = R.string.preference_automute_exact_alarms_unavailable,
			secondaryText = stringResource(
				R.string.preference_automute_exact_alarms_unavailable_desc,
				stringResource(R.string.app_name)
			)
		)
	}

	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
		PreferenceGroup(stringResource(id = R.string.preference_category_general_automute)) {
			val scope = rememberCoroutineScope()

			val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
				if (viewModel.autoMuteService.isPermissionGranted()) {
					scope.launch {
						viewModel.repository.updateSettings {
							automuteEnable = true
						}
						viewModel.autoMuteService.autoMuteEnable()
					}
				}
			}

			LaunchedEffect(Unit) {
				viewModel.repository.updateSettings {
					automuteEnable = viewModel.autoMuteService.isAutoMuteEnabled()
				}
			}

			SwitchPreference(
				title = { Text(stringResource(R.string.preference_automute_enable)) },
				summary = { Text(stringResource(R.string.preference_automute_enable_summary)) },
				settingsRepository = viewModel.repository,
				value = { it.automuteEnable },
				onValueChange = {
					if (it) {
						if (viewModel.autoMuteService.isPermissionGranted()) {
							viewModel.autoMuteService.autoMuteEnable()
							viewModel.autoMuteService.autoMuteStateOn()
							automuteEnable = true
						} else {
							launcher.launch(Intent(ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
							automuteEnable = false
						}
					} else {
						viewModel.autoMuteService.autoMuteStateOff()
						viewModel.autoMuteService.autoMuteDisable()
						automuteEnable = false
					}
				}
			)
			SwitchPreference(
				title = { Text(stringResource(R.string.preference_automute_cancelled_lessons)) },
				enabledCondition = { it.automuteEnable },
				settingsRepository = viewModel.repository,
				value = { it.automuteCancelledLessons },
				onValueChange = { automuteCancelledLessons = it }
			)
			SwitchPreference(
				title = { Text(stringResource(R.string.preference_automute_mute_priority)) },
				enabledCondition = { it.automuteEnable },
				settingsRepository = viewModel.repository,
				value = { it.automuteMutePriority },
				onValueChange = { automuteMutePriority = it }
			)

			SliderPreference(
				valueRange = 0f..20f,
				steps = 19,
				title = { Text(stringResource(R.string.preference_automute_minimum_break_length)) },
				summary = { Text(stringResource(R.string.preference_automute_minimum_break_length_summary)) },
				showSeekBarValue = true,
				enabledCondition = { it.automuteEnable },
				settingsRepository = viewModel.repository,
				value = { it.automuteMinimumBreakLength },
				onValueChange = { automuteMinimumBreakLength = it }
			)
		}
	}

	PreferenceGroup(stringResource(R.string.preference_category_reports)) {
		Preference(
			title = { Text(stringResource(R.string.preference_reports_info)) },
			summary = { Text(stringResource(R.string.preference_reports_info_desc)) },
			leadingContent = {
				Icon(
					painter = painterResource(R.drawable.settings_info),
					contentDescription = null
				)
			}
		)

		SwitchPreference(
			title = { Text(stringResource(R.string.preference_reports_breadcrumbs)) },
			summary = { Text(stringResource(R.string.preference_reports_breadcrumbs_desc)) },
			settingsRepository = viewModel.globalRepository,
			value = { it.errorReportingEnableBreadcrumbs },
			onValueChange = { errorReportingEnableBreadcrumbs = it }
		)

		if (BuildConfig.DEBUG) {
			val context = LocalContext.current
			Preference(
				title = { Text("Send test report") },
				summary = { Text("Sends a report to Sentry to test error reporting") },
				onClick = {
					Sentry.captureException(Exception("Test report"))
					Toast.makeText(
						context,
						"Report has been sent",
						Toast.LENGTH_SHORT
					).show()
				}
			)
		}
	}

	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
		val packageName = LocalContext.current.packageName
		val context = LocalContext.current
		Preference(
			title = { Text(text = stringResource(id = R.string.preference_app_language)) },
			onClick = {
				context.startActivity(
					Intent(
						android.provider.Settings.ACTION_APP_LOCALE_SETTINGS,
						Uri.parse("package:$packageName")
					)
				)
			}
		)
	}
}
