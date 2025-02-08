package com.sapuseven.untis.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sapuseven.compose.protostore.ui.preferences.SwitchPreference
import com.sapuseven.compose.protostore.ui.utils.LocalListItemColors
import com.sapuseven.untis.R
import com.sapuseven.untis.ui.functional.insetsPaddingValues
import com.sapuseven.untis.ui.pages.settings.GlobalSettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsInfoBottomSheet(
	repository: GlobalSettingsRepository
) {
	val scope = rememberCoroutineScope()
	var bottomSheetVisible by rememberSaveable { mutableStateOf(false) }
	val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
	var saveEnabled by rememberSaveable { mutableStateOf(true) }

	LaunchedEffect(Unit) {
		scope.launch {
			repository.getSettings().first().let { it ->
				if (!it.errorReportingSet) {
					bottomSheetVisible = true
					sheetState.show()
				}
			}
		}
	}

	if (bottomSheetVisible) {
		Box(
			modifier = Modifier.fillMaxSize()
		) {
			ModalBottomSheet(
				onDismissRequest = {
					scope.launch {
						sheetState.hide()
						bottomSheetVisible = false
					}
				},
				sheetState = sheetState,
			) {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.SpaceBetween,
					modifier = Modifier
						.fillMaxWidth()
						.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
				) {
					Text(
						text = stringResource(R.string.main_dialog_reports_title),
						style = MaterialTheme.typography.headlineLarge
					)

					Icon(
						painter = painterResource(id = R.drawable.all_reports_image),
						modifier = Modifier
							.size(72.dp)
							.padding(start = 16.dp, end = 8.dp),
						contentDescription = null
					)
				}

				Text(
					text = stringResource(R.string.main_dialog_reports_info_1),
					modifier = Modifier
						.padding(horizontal = 16.dp)
				)

				Text(
					text = stringResource(R.string.preference_reports_info_desc),
					modifier = Modifier
						.padding(start = 16.dp, end = 16.dp, top = 8.dp)
				)

				Column(
					modifier = Modifier
						.padding(vertical = 16.dp)
				) {
					CompositionLocalProvider(
						LocalListItemColors provides ListItemDefaults.colors(
							containerColor = MaterialTheme.colorScheme.surfaceContainerLow
						)
					) {
						HorizontalDivider()

						SwitchPreference(
							title = { Text(stringResource(R.string.preference_reports_enable)) },
							settingsRepository = repository,
							value = { it.errorReportingEnable },
							onValueChange = { errorReportingEnable = it }
						)

						SwitchPreference(
							title = { Text(stringResource(R.string.preference_reports_breadcrumbs)) },
							summary = { Text(stringResource(R.string.preference_reports_breadcrumbs_desc)) },
							settingsRepository = repository,
							value = { it.errorReportingEnableBreadcrumbs },
							onValueChange = { errorReportingEnableBreadcrumbs = it },
							enabledCondition = { it.errorReportingEnable }
						)

						HorizontalDivider()
					}
				}

				Text(
					text = stringResource(R.string.main_dialog_reports_info_2),
					modifier = Modifier
						.padding(horizontal = 16.dp)
				)

				Row(
					horizontalArrangement = Arrangement.End,
					modifier = Modifier
						.fillMaxWidth()
						.padding(16.dp)
				) {
					Button(
						enabled = saveEnabled,
						onClick = {
							saveEnabled = false
							scope.launch {
								repository.updateSettings {
									errorReportingSet = true
								}
								sheetState.hide()
							}.invokeOnCompletion {
								if (!sheetState.isVisible) {
									bottomSheetVisible = false
								}
							}
						}
					) {
						Text(text = stringResource(R.string.main_dialog_reports_save))
					}
				}
			}

			// Workaround for the modal bottom sheet not covering the nav bar
			Box(
				modifier = Modifier
					.background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
					.fillMaxWidth()
					.height(insetsPaddingValues().calculateBottomPadding())
					.align(Alignment.BottomCenter)
			) {}
		}
	}
}
