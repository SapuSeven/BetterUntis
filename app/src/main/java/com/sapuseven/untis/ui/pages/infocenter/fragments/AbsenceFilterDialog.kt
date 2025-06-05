package com.sapuseven.untis.ui.pages.infocenter.fragments

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.sapuseven.compose.protostore.ui.preferences.ListPreference
import com.sapuseven.compose.protostore.ui.preferences.SwitchPreference
import com.sapuseven.untis.R
import com.sapuseven.untis.data.repository.UserSettingsRepository
import com.sapuseven.untis.data.settings.model.AbsencesTimeRange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AbsenceFilterDialog(
	settingsRepository: UserSettingsRepository,
	onDismiss: () -> Unit
) {
	var dismissed by rememberSaveable { mutableStateOf(false) }
	fun dismiss() {
		dismissed = true
		onDismiss()
	}
	BackHandler(
		enabled = !dismissed,
	) {
		dismiss()
	}

	Scaffold(
		topBar = {
			CenterAlignedTopAppBar(
				title = { Text(text = stringResource(id = R.string.infocenter_absences_filter)) },
				navigationIcon = {
					IconButton(onClick = {
						dismiss()
					}) {
						Icon(
							imageVector = Icons.Outlined.Close,
							contentDescription = stringResource(id = R.string.all_close)
						)
					}
				}
			)
		}
	) { padding ->
		Box(modifier = Modifier.padding(padding)) {
			Column(
				modifier = Modifier.verticalScroll(rememberScrollState())
			) {
				SwitchPreference(
					title = { Text(text = stringResource(id = R.string.infocenter_absences_filter_only_unexcused)) },
					settingsRepository = settingsRepository,
					value = { it.infocenterAbsencesOnlyUnexcused },
					onValueChange = { infocenterAbsencesOnlyUnexcused = it }
				)
				SwitchPreference(
					title = { Text(text = stringResource(id = R.string.infocenter_absences_filter_sort)) },
					supportingContent = { value, _ ->
						if (value)
							Text(text = stringResource(id = R.string.infocenter_absences_filter_oldest_first))
						else
							Text(text = stringResource(id = R.string.infocenter_absences_filter_newest_first))
					},
					settingsRepository = settingsRepository,
					value = { it.infocenterAbsencesSortReverse },
					onValueChange = { infocenterAbsencesSortReverse = it }
				)
				ListPreference(
					title = { Text(text = stringResource(id = R.string.infocenter_absences_filter_time_ranges)) },
					entries = stringArrayResource(id = R.array.infocenter_absences_list_values),
					entryLabels = stringArrayResource(id = R.array.infocenter_absences_list),
					settingsRepository = settingsRepository,
					value = { it.infocenterAbsencesTimeRange.number.toString() },
					onValueChange = { infocenterAbsencesTimeRange = AbsencesTimeRange.forNumber(it.toInt()) }
				)
			}
		}
	}
}

