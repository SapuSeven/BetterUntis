package com.sapuseven.untis.feature.settings.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
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
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sapuseven.compose.protostore.ui.preferences.Preference
import com.sapuseven.compose.protostore.ui.preferences.PreferenceGroup
import com.sapuseven.compose.protostore.ui.preferences.SwitchPreference
import com.sapuseven.untis.feature.settings.R
import com.sapuseven.untis.feature.settings.SettingsViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.jvm.java

@SuppressLint("InlinedApi")
@Composable
fun SettingsCategoryGeneral(viewModel: SettingsViewModel) {
	/* PreferenceGroup(stringResource(id = R.string.feature_settings_preference_category_general_behaviour)) {
	// Not supported, not planned. May be reconsidered if there is demand for this feature.
	SwitchPreference(
		title = { Text(stringResource(R.string.feature_settings_preference_double_tap_to_exit)) },
		settingsDataSource = viewModel.repository,
		value = { it.exitConfirmation },
		onValueChange = { exitConfirmation = it }
	)

	// Not supported. May be reconsidered if there is demand for this feature.
	// Could be implemented by setting flingBehavior on the WeekView HorizontalPager
	SwitchPreference(
		title = { Text(stringResource(R.string.feature_settings_preference_flinging_enable)) },
		settingsDataSource = viewModel.repository,
		value = { it.flingEnable },
		onValueChange = { flingEnable = it }
	)*/

	/* Not supported yet
	PreferenceGroup(stringResource(R.string.feature_settings_preference_category_general_week_display)) {
		WeekRangePreference(
			title = { Text(stringResource(R.string.feature_settings_preference_week_custom_range)) },
			settingsDataSource = viewModel.repository,
			value = { it.weekCustomRangeList.toSet() },
			onValueChange = {
				clearWeekCustomRange()
				addAllWeekCustomRange(it)
			}
		)

		SwitchPreference(
			title = { Text(stringResource(R.string.feature_settings_preference_week_snap_to_days)) },
			summary = { Text(stringResource(R.string.feature_settings_preference_week_snap_to_days_summary)) },
			settingsDataSource = viewModel.repository,
			value = { it.weekSnapToDays },
			onValueChange = { weekSnapToDays = it }
		)

		SliderPreference(
			title = { Text(stringResource(R.string.feature_settings_preference_week_display_length)) },
			summary = { Text(stringResource(R.string.feature_settings_preference_week_display_length_summary)) },
			valueRange = 0f..7f,
			steps = 6,
			enabledCondition = { it.weekSnapToDays },
			showSeekBarValue = true,
			settingsDataSource = viewModel.repository,
			value = { it.weekCustomLength },
			onValueChange = { weekCustomLength = it }
		)
	}*/

	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
		val visible by viewModel.userSettingsDataSource.getSettings().map { it.automuteEnable }
			.collectAsState(initial = false)
		ScheduleExactAlarmInfoMessage(
			visible = visible,
			primaryText = R.string.feature_settings_preference_automute_exact_alarms_unavailable,
			secondaryText = stringResource(
				R.string.feature_settings_preference_automute_exact_alarms_unavailable_desc,
				stringResource(R.string.feature_settings_app_name)
			)
		)
	}

	PreferenceGroup(stringResource(id = R.string.feature_settings_preference_category_general_automute)) {
		val context = LocalContext.current
		val scope = rememberCoroutineScope()
		val permissionLauncher =
			rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
				if (viewModel.autoMuteService.isPermissionGranted()) {
					scope.launch {
						viewModel.userSettingsDataSource.updateSettings {
							automuteEnable = true
						}
						viewModel.autoMuteService.autoMuteEnable()
					}
				}
			}

		LaunchedEffect(Unit) {
			viewModel.userSettingsDataSource.updateSettings {
				automuteEnable = viewModel.autoMuteService.isAutoMuteEnabled()
			}
		}

		SwitchPreference(
			title = { Text(stringResource(R.string.feature_settings_preference_automute_enable)) },
			summary = { Text(stringResource(R.string.feature_settings_preference_automute_enable_summary)) },
			settingsDataSource = viewModel.userSettingsDataSource,
			value = { it.automuteEnable },
			onValueChange = {
				if (it) {
					if (viewModel.autoMuteService.isPermissionGranted()) {
						viewModel.autoMuteService.autoMuteEnable()
						automuteEnable = true
					} else {
						permissionLauncher.launch(Intent(ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
						automuteEnable = false
					}
				} else {
					viewModel.autoMuteService.autoMuteDisable()
					automuteEnable = false
				}
			}
		)

		Preference(
			title = { Text(stringResource(R.string.feature_settings_preference_automute_preferences)) },
			trailingContent = {
				Icon(
					painter = painterResource(R.drawable.settings_external),
					contentDescription = null
				)
			},
			onClick = {
				val uri = "betteruntis://automute/configure".toUri()
					.buildUpon()
					//.appendQueryParameter("userId", viewModel.currentUserId().toString())
					.build()

				context.startActivity(Intent(Intent.ACTION_VIEW, uri))
			}
		)
	}

	PreferenceGroup(stringResource(R.string.feature_settings_preference_category_reports)) {
		Preference(
			title = { Text(stringResource(R.string.feature_settings_preference_reports_info)) },
			summary = { Text(stringResource(R.string.feature_settings_preference_reports_info_desc)) },
			leadingContent = { Icon(painterResource(R.drawable.feature_settings_info), null) }
		)

		SwitchPreference(
			title = { Text(stringResource(R.string.feature_settings_preference_reports_enable)) },
			leadingContent = { Icon(painterResource(R.drawable.feature_settings_general_reports), null) },
			settingsDataSource = viewModel.globalSettingsDataSource,
			value = { it.errorReportingEnable },
			onValueChange = { errorReportingEnable = it }
		)

		SwitchPreference(
			title = { Text(stringResource(R.string.feature_settings_preference_reports_breadcrumbs)) },
			summary = { Text(stringResource(R.string.feature_settings_preference_reports_breadcrumbs_desc)) },
			leadingContent = { Icon(painterResource(R.drawable.feature_settings_general_reports_breadcrumbs), null) },
			settingsDataSource = viewModel.globalSettingsDataSource,
			value = { it.errorReportingEnableBreadcrumbs },
			onValueChange = { errorReportingEnableBreadcrumbs = it },
			enabledCondition = { it.errorReportingEnable }
		)

		/*if (BuildConfig.DEBUG) {
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
		}*/
	}

	PreferenceGroup(stringResource(R.string.feature_settings_preference_category_animations)) {
		SwitchPreference(
			title = { Text(stringResource(R.string.feature_settings_preference_shared_transitions_enable)) },
			summary = { Text(stringResource(R.string.feature_settings_preference_shared_transitions_enable_desc)) },
			leadingContent = { Icon(painterResource(R.drawable.feature_settings_general_animation), null) },
			settingsDataSource = viewModel.userSettingsDataSource,
			value = { it.enableSharedTransitions },
			onValueChange = { enableSharedTransitions = it },
			enabledCondition = { false } // TODO not yet implemented
		)
	}

	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
		val packageName = LocalContext.current.packageName
		val context = LocalContext.current
		Preference(
			title = { Text(text = stringResource(id = R.string.feature_settings_preference_app_language)) },
			leadingContent = { Icon(painterResource(R.drawable.feature_settings_general_language), null) },
			onClick = {
				context.startActivity(
					Intent(
						android.provider.Settings.ACTION_APP_LOCALE_SETTINGS,
						"package:$packageName".toUri()
					)
				)
			}
		)
	}
}
