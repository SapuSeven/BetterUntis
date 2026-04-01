package com.sapuseven.untis.feature.automute.ui

import android.content.Intent
import android.os.Build
import android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sapuseven.compose.protostore.ui.preferences.SliderPreference
import com.sapuseven.compose.protostore.ui.preferences.SwitchPreference
import com.sapuseven.compose.protostore.ui.utils.LocalListItemColors
import com.sapuseven.untis.core.ui.functional.None
import com.sapuseven.untis.feature.automute.R

@RequiresApi(Build.VERSION_CODES.M)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoMuteSettings(
	viewModel: AutoMuteSettingsViewModel = hiltViewModel(),
	onNavigateBack: () -> Unit = {}
) {
	Scaffold(
		topBar = {
			LargeTopAppBar(
				title = {
					Text(
						text = stringResource(R.string.feature_automute_title)
					)
				},
				navigationIcon = {
					IconButton(onClick = onNavigateBack) {
						Icon(
							imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
							contentDescription = stringResource(id = com.sapuseven.untis.core.ui.R.string.all_back)
						)
					}
				}
			)
		},
		contentWindowInsets = WindowInsets.None
	) { innerPadding ->
		val user by viewModel.user.collectAsStateWithLifecycle()

		Column(
			modifier = Modifier
				.padding(innerPadding)
				.fillMaxSize()
				.verticalScroll(rememberScrollState())
		) {
			user?.let {
				Text(
					text = it.name,
					style = MaterialTheme.typography.titleMedium,
					modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 32.dp)
				)
			}

			val permissionLauncher =
				rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
					viewModel.enableAutoMute()
				}

			CompositionLocalProvider(
				LocalListItemColors provides ListItemDefaults.colors(
					containerColor = MaterialTheme.colorScheme.primaryContainer,
					headlineColor = MaterialTheme.colorScheme.onPrimaryContainer,
				)
			) {
				SwitchPreference(
					title = { Text(stringResource(R.string.feature_automute_enable)) },
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
					},
					modifier = Modifier
						.padding(16.dp)
						.clip(RoundedCornerShape(24.dp))
				)
			}

			SwitchPreference(
				title = { Text(stringResource(R.string.feature_automute_cancelled_lessons)) },
				settingsDataSource = viewModel.userSettingsDataSource,
				value = { it.automuteCancelledLessons },
				onValueChange = { automuteCancelledLessons = it },
				enabledCondition = { it.automuteEnable },
			)

			// TODO: Currently not implemented in the new zen-rule based implementation.
			//  Needs to be reconsidered
			/*SwitchPreference(
				title = { Text(stringResource(R.string.feature_automute_allow_priority)) },
				settingsDataSource = viewModel.userSettingsDataSource,
				value = { it.automuteAllowPriority },
				onValueChange = { automuteAllowPriority = it }
			)*/

			SliderPreference(
				valueRange = 0f..20f,
				steps = 19,
				title = { Text(stringResource(R.string.feature_automute_minimum_break_length)) },
				summary = { Text(stringResource(R.string.feature_automute_minimum_break_length_summary)) },
				showSeekBarValue = true,
				settingsDataSource = viewModel.userSettingsDataSource,
				value = { it.automuteMinimumBreakLength },
				onValueChange = { automuteMinimumBreakLength = it },
				enabledCondition = { it.automuteEnable },
			)
		}
	}
}
