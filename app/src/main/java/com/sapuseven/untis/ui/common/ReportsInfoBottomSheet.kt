package com.sapuseven.untis.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.sapuseven.untis.R
import com.sapuseven.untis.activities.reportsDataStore
import com.sapuseven.untis.activities.reportsDataStoreBreadcrumbsEnable
import com.sapuseven.untis.preferences.UntisPreferenceDataStore
import com.sapuseven.untis.ui.functional.insetsPaddingValues
import com.sapuseven.untis.ui.preferences.SwitchPreference
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsInfoBottomSheet(reportsDataStore: DataStore<Preferences> = LocalContext.current.reportsDataStore) {
	val scope = rememberCoroutineScope()
	var bottomSheetVisible by rememberSaveable { mutableStateOf(false) }
	val bottomSheetState = rememberStandardBottomSheetState(initialValue = SheetValue.Expanded)
	var saveEnabled by rememberSaveable { mutableStateOf(true) }

	LaunchedEffect(Unit) {
		if (reportsDataStore.data.map { prefs ->
				prefs[reportsDataStoreBreadcrumbsEnable.first]
			}.first() == null) {
			bottomSheetVisible = true
			bottomSheetState.show()
		}
	}

	if (bottomSheetVisible) {
		Box(
			modifier = Modifier.fillMaxSize()
		) {
			ModalBottomSheet(
				onDismissRequest = { bottomSheetVisible = false },
				sheetState = bottomSheetState,
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
					Divider()

					SwitchPreference(
						title = { Text(stringResource(R.string.preference_reports_breadcrumbs)) },
						summary = { Text(stringResource(R.string.preference_reports_breadcrumbs_desc)) },
						dataStore = UntisPreferenceDataStore(
							reportsDataStore,
							reportsDataStoreBreadcrumbsEnable.first,
							reportsDataStoreBreadcrumbsEnable.second
						)
					)

					Divider()
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
								reportsDataStore.edit { prefs ->
									prefs[reportsDataStoreBreadcrumbsEnable.first] =
										prefs[reportsDataStoreBreadcrumbsEnable.first]
											?: reportsDataStoreBreadcrumbsEnable.second
								}

								bottomSheetState.hide()
							}.invokeOnCompletion {
								if (!bottomSheetState.isVisible) {
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
