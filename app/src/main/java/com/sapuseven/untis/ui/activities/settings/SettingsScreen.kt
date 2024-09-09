package com.sapuseven.untis.ui.activities.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.sapuseven.untis.R
import com.sapuseven.untis.ui.common.AppScaffold
import com.sapuseven.untis.ui.common.VerticalScrollColumn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
	navController: NavHostController,
	title: String?,
	viewModel: SettingsScreenViewModel = hiltViewModel(),
	content: @Composable (SettingsScreenViewModel) -> Unit
) {
	val colorScheme = MaterialTheme.colorScheme
	LaunchedEffect(Unit) {
		viewModel.setColorScheme(colorScheme)
	}

	/*val autoMutePref = dataStorePreferences.automuteEnable
	val scope = rememberCoroutineScope()
	val autoMuteSettingsLauncher =
		rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
			updateAutoMutePref(user, scope, autoMutePref, true)
		}
	updateAutoMutePref(user, scope, autoMutePref)

	var dialogScheduleExactAlarms by remember { mutableStateOf(false) }

	val notificationPref = dataStorePreferences.notificationsEnable
	val notificationSettingsLauncher =
		rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
			if (canPostNotifications())
				scope.launch {
					notificationPref.saveValue(true)
					enqueueNotificationSetup(user)
				}
		}
	val requestNotificationPermissionLauncher =
		rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
			if (isGranted)
				scope.launch {
					notificationPref.saveValue(true)
					enqueueNotificationSetup(user)
				}
			else
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
					notificationSettingsLauncher.launch(
						Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
							.putExtra(
								android.provider.Settings.EXTRA_APP_PACKAGE,
								BuildConfig.APPLICATION_ID
							)
					)
				}
		}
	val alarmSettingsLauncher =
		rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
			if (canScheduleExactAlarms())
				scope.launch {
					notificationPref.saveValue(true)
					enqueueNotificationSetup(user)
				}
		}

	val languageSettingsLauncher =
		rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {}*/

	AppScaffold(
		topBar = {
			CenterAlignedTopAppBar(
				title = {
					Text(
						title
							?: stringResource(id = R.string.activity_title_settings)
					)
				},
				navigationIcon = {
					IconButton(onClick = { navController.navigateUp() }) {
						Icon(
							imageVector = Icons.Outlined.ArrowBack,
							contentDescription = stringResource(id = R.string.all_back)
						)
					}
				}
			)
		}
	) { innerPadding ->
		Box(
			modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
		) {
			VerticalScrollColumn {
				content(viewModel)
			}
		}
	}

	/*if (dialogScheduleExactAlarms)
		AlertDialog(
			onDismissRequest = {
				dialogScheduleExactAlarms = false
			},
			title = {
				Text(text = stringResource(R.string.preference_dialog_permission_alarms_title))
			},
			text = {
				Text(text = stringResource(R.string.preference_dialog_permission_alarms_text))
			},
			dismissButton = {
				TextButton(onClick = {
					dialogScheduleExactAlarms = false
				}) {
					Text(text = stringResource(id = R.string.all_cancel))
				}
			},
			confirmButton = {
				TextButton(onClick = {
					dialogScheduleExactAlarms = false
					alarmSettingsLauncher.launch(
						Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
					)
				}) {
					Text(text = stringResource(R.string.all_dialog_open_settings))
				}
			}
		)*/
}
