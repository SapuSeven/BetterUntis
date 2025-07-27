package com.sapuseven.untis.ui.pages.settings.fragments

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.toRoute
import com.sapuseven.compose.protostore.ui.preferences.NumericInputPreference
import com.sapuseven.compose.protostore.ui.preferences.PreferenceGroup
import com.sapuseven.compose.protostore.ui.preferences.RangeInputPreference
import com.sapuseven.compose.protostore.ui.preferences.SwitchPreference
import com.sapuseven.untis.R
import com.sapuseven.untis.ui.navigation.AppRoutes
import com.sapuseven.untis.ui.pages.settings.SettingsScreenViewModel
import com.sapuseven.untis.ui.preferences.ElementPickerPreference
import kotlin.math.roundToInt

@Composable
fun SettingsCategoryTimetable(viewModel: SettingsScreenViewModel) {
	val args = viewModel.savedStateHandle.toRoute<AppRoutes.Settings.Timetable>()
	val elements by viewModel.elements.collectAsStateWithLifecycle()

	ElementPickerPreference(
		title = { Text(stringResource(R.string.preference_timetable_personal_timetable)) },
		leadingContent = {
			Icon(
				painter = painterResource(id = R.drawable.settings_account_personal),
				contentDescription = null
			)
		},
		settingsRepository = viewModel.userSettingsRepository,
		value = { it.timetablePersonalTimetable },
		onValueChange = { timetablePersonalTimetable = it },
		elements = elements,
		highlight = args.highlightTitle == R.string.preference_timetable_personal_timetable,
	)

	SwitchPreference(
		title = { Text(stringResource(R.string.preference_timetable_hide_timestamps)) },
		summary = { Text(stringResource(R.string.preference_timetable_hide_timestamps_desc)) },
		leadingContent = {
			Icon(
				painter = painterResource(id = R.drawable.settings_hide_timestamps),
				contentDescription = null
			)
		},
		settingsRepository = viewModel.userSettingsRepository,
		value = { it.timetableHideTimeStamps },
		onValueChange = { timetableHideTimeStamps = it }
	)

	SwitchPreference(
		title = { Text(stringResource(R.string.preference_timetable_hide_cancelled)) },
		leadingContent = {
			Icon(
				painter = painterResource(id = R.drawable.settings_hide_cancelled),
				contentDescription = null
			)
		},
		settingsRepository = viewModel.userSettingsRepository,
		value = { it.timetableHideCancelled },
		onValueChange = { timetableHideCancelled = it }
	)

	SwitchPreference(
		title = { Text(stringResource(R.string.preference_timetable_substitutions_irregular)) },
		summary = { Text(stringResource(R.string.preference_timetable_substitutions_irregular_desc)) },
		leadingContent = {
			Icon(
				painter = painterResource(id = R.drawable.settings_detect_irregular),
				contentDescription = null
			)
		},
		settingsRepository = viewModel.userSettingsRepository,
		value = { it.timetableSubstitutionsIrregular },
		onValueChange = { timetableSubstitutionsIrregular = it }
	)

	SwitchPreference(
		title = { Text(stringResource(R.string.preference_timetable_zoom_gesture)) },
		leadingContent = {
			Icon(
				painter = painterResource(id = R.drawable.settings_zoom_gesture),
				contentDescription = null
			)
		},
		settingsRepository = viewModel.userSettingsRepository,
		value = { it.timetableZoomEnabled },
		onValueChange = { timetableZoomEnabled = it }
	)

	NumericInputPreference(
		title = { Text(stringResource(R.string.preference_timetable_zoom_level)) },
		leadingContent = {
			Icon(
				painter = painterResource(R.drawable.settings_zoom_level),
				contentDescription = null
			)
		},
		unit = "%",
		settingsRepository = viewModel.userSettingsRepository,
		value = { (it.timetableZoomLevel * 100).roundToInt().coerceIn(75, 200) },
		onValueChange = { timetableZoomLevel = it.coerceIn(75, 200) / 100f }
	)

	/* Not supported due to reliability issues
	SwitchPreference(
		title = { Text(stringResource(R.string.preference_timetable_background_irregular)) },
		summary = { Text(stringResource(R.string.preference_timetable_background_irregular_desc)) },
		leadingContent = {
			Icon(
				painter = painterResource(id = R.drawable.settings_background_irregular),
				contentDescription = null
			)
		},
		enabledCondition = { it.timetableSubstitutionsIrregular },
		settingsRepository = viewModel.repository,
		value = { it.timetableBackgroundIrregular },
		onValueChange = { timetableBackgroundIrregular = it }
	)*/

	PreferenceGroup(stringResource(id = R.string.preference_category_display_options)) {
		RangeInputPreference(
			title = { Text(stringResource(R.string.preference_timetable_range)) },
			leadingContent = {
				Icon(
					painter = painterResource(id = R.drawable.settings_timetable_range),
					contentDescription = null
				)
			},
			settingsRepository = viewModel.userSettingsRepository,
			value = { it.timetableRange },
			onValueChange = { timetableRange = it }
		)

		SwitchPreference(
			title = { Text(stringResource(R.string.preference_timetable_range_index_reset)) },
			summary = { Text(stringResource(R.string.preference_timetable_range_index_reset_desc)) },
			leadingContent = {
				Icon(
					painter = painterResource(id = R.drawable.settings_timetable_range_reset),
					contentDescription = null
				)
			},
			settingsRepository = viewModel.userSettingsRepository,
			value = { it.timetableRangeIndexReset },
			onValueChange = { timetableRangeIndexReset = it }
		)

		/*SwitchPreference
		enabled = false,
		key = preference_timetable_range_hide_outside,
		summary = (not implemented),
		title = Hide lessons outside specified range" */
	}

	PreferenceGroup(stringResource(id = R.string.preference_category_timetable_item_appearance)) {
		NumericInputPreference(
			title = { Text(stringResource(R.string.preference_timetable_item_padding)) },
			leadingContent = {
				Icon(
					painter = painterResource(R.drawable.settings_timetable_padding),
					contentDescription = null
				)
			},
			unit = "dp",
			settingsRepository = viewModel.userSettingsRepository,
			value = { it.timetableItemPadding },
			onValueChange = { timetableItemPadding = it }
		)

		NumericInputPreference(
			title = { Text(stringResource(R.string.preference_timetable_item_corner_radius)) },
			leadingContent = {
				Icon(
					painter = painterResource(R.drawable.settings_timetable_rounded_corner),
					contentDescription = null
				)
			},
			unit = "dp",
			settingsRepository = viewModel.userSettingsRepository,
			value = { it.timetableItemCornerRadius },
			onValueChange = { timetableItemCornerRadius = it }
		)
	}

	PreferenceGroup(stringResource(id = R.string.preference_category_timetable_lesson_text)) {
		SwitchPreference(
			title = { Text(stringResource(R.string.preference_timetable_centered_lesson_info)) },
			leadingContent = {
				Icon(
					painter = painterResource(R.drawable.settings_timetable_align_center),
					contentDescription = null
				)
			},
			settingsRepository = viewModel.userSettingsRepository,
			value = { it.timetableCenteredLessonInfo },
			onValueChange = { timetableCenteredLessonInfo = it }
		)

		SwitchPreference(
			title = { Text(stringResource(R.string.preference_timetable_bold_lesson_name)) },
			leadingContent = {
				Icon(
					painter = painterResource(R.drawable.settings_timetable_format_bold),
					contentDescription = null
				)
			},
			settingsRepository = viewModel.userSettingsRepository,
			value = { it.timetableBoldLessonName },
			onValueChange = { timetableBoldLessonName = it }
		)

		NumericInputPreference(
			title = { Text(stringResource(R.string.preference_timetable_lesson_name_font_size)) },
			leadingContent = {
				Icon(
					painter = painterResource(R.drawable.settings_timetable_font_size),
					contentDescription = null
				)
			},
			unit = "sp",
			settingsRepository = viewModel.userSettingsRepository,
			value = { it.timetableLessonNameFontSize },
			onValueChange = { timetableLessonNameFontSize = it }
		)

		NumericInputPreference(
			title = { Text(stringResource(R.string.preference_timetable_lesson_info_font_size)) },
			leadingContent = {
				Icon(
					painter = painterResource(R.drawable.settings_timetable_font_size),
					contentDescription = null
				)
			},
			unit = "sp",
			settingsRepository = viewModel.userSettingsRepository,
			value = { it.timetableLessonInfoFontSize },
			onValueChange = { timetableLessonInfoFontSize = it }
		)
	}
}
