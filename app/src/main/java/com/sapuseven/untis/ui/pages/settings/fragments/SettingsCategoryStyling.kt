package com.sapuseven.untis.ui.pages.settings.fragments

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.sapuseven.compose.protostore.ui.preferences.ColorPreference
import com.sapuseven.compose.protostore.ui.preferences.ConfirmDialogPreference
import com.sapuseven.compose.protostore.ui.preferences.ListPreference
import com.sapuseven.compose.protostore.ui.preferences.MultiSelectListPreference
import com.sapuseven.compose.protostore.ui.preferences.PreferenceGroup
import com.sapuseven.compose.protostore.ui.preferences.SwitchPreference
import com.sapuseven.untis.R
import com.sapuseven.untis.ui.pages.settings.SettingsScreenViewModel

@Composable
fun SettingsCategoryStyling(viewModel: SettingsScreenViewModel) {
	PreferenceGroup(stringResource(id = R.string.preference_category_styling_colors)) {
		ColorPreference(
			title = { Text(stringResource(R.string.preference_background_future)) },
			showAlphaSlider = true,
			settingsRepository = viewModel.repository,
			value = { it.backgroundFuture },
			onValueChange = { backgroundFuture = it },
			defaultValueLabel = stringResource(R.string.preferences_default_color)
		)

		ColorPreference(
			title = { Text(stringResource(R.string.preference_background_past)) },
			showAlphaSlider = true,
			settingsRepository = viewModel.repository,
			value = { it.backgroundPast },
			onValueChange = { backgroundPast = it },
			defaultValueLabel = stringResource(R.string.preferences_default_color)
		)

		ColorPreference(
			title = { Text(stringResource(R.string.preference_marker)) },
			settingsRepository = viewModel.repository,
			value = { it.marker },
			onValueChange = { marker = it },
			defaultValueLabel = stringResource(R.string.preferences_default_color)
		)
	}

	PreferenceGroup(stringResource(id = R.string.preference_category_styling_backgrounds)) {
		MultiSelectListPreference(
			title = { Text(stringResource(R.string.preference_school_background)) },
			summary = { Text(stringResource(R.string.preference_school_background_desc)) },
			entries = stringArrayResource(id = R.array.preference_schoolcolors_values),
			entryLabels = stringArrayResource(id = R.array.preference_schoolcolors),
			settingsRepository = viewModel.repository,
			value = { it.schoolBackgroundList.toSet() },
			onValueChange = {
				clearSchoolBackground()
				addAllSchoolBackground(it)
			}
		)

		ColorPreference(
			title = { Text(stringResource(R.string.preference_background_regular)) },
			enabledCondition = {
				!it.schoolBackgroundList.contains("regular")
			},
			settingsRepository = viewModel.repository,
			value = { it.backgroundRegular },
			onValueChange = { backgroundRegular = it },
			showAlphaSlider = true,
			defaultValueLabel = stringResource(id = R.string.preferences_theme_color)
		)

		ColorPreference(
			title = { Text(stringResource(R.string.preference_background_regular_past)) },
			enabledCondition = {
				!it.schoolBackgroundList.contains("regular")
			},
			settingsRepository = viewModel.repository,
			value = { it.backgroundRegularPast },
			onValueChange = { backgroundRegularPast = it },
			showAlphaSlider = true,
			defaultValueLabel = stringResource(id = R.string.preferences_theme_color)
		)

		ColorPreference(
			title = { Text(stringResource(R.string.preference_background_exam)) },
			enabledCondition = {
				!it.schoolBackgroundList.contains("exam")
			},
			settingsRepository = viewModel.repository,
			value = { it.backgroundExam },
			onValueChange = { backgroundExam = it },
			showAlphaSlider = true,
			defaultValueLabel = stringResource(id = R.string.preferences_theme_color)
		)

		ColorPreference(
			title = { Text(stringResource(R.string.preference_background_exam_past)) },
			enabledCondition = {
				!it.schoolBackgroundList.contains("exam")
			},
			settingsRepository = viewModel.repository,
			value = { it.backgroundExamPast },
			onValueChange = { backgroundExamPast = it },
			showAlphaSlider = true,
			defaultValueLabel = stringResource(id = R.string.preferences_theme_color)
		)

		ColorPreference(
			title = { Text(stringResource(R.string.preference_background_irregular)) },
			enabledCondition = {
				!it.schoolBackgroundList.contains("irregular")
			},
			settingsRepository = viewModel.repository,
			value = { it.backgroundIrregular },
			onValueChange = { backgroundIrregular = it },
			showAlphaSlider = true,
			defaultValueLabel = stringResource(id = R.string.preferences_theme_color)
		)

		ColorPreference(
			title = { Text(stringResource(R.string.preference_background_irregular_past)) },
			enabledCondition = {
				!it.schoolBackgroundList.contains("irregular")
			},
			settingsRepository = viewModel.repository,
			value = { it.backgroundIrregularPast },
			onValueChange = { backgroundIrregularPast = it },
			showAlphaSlider = true,
			defaultValueLabel = stringResource(id = R.string.preferences_theme_color)
		)

		ColorPreference(
			title = { Text(stringResource(R.string.preference_background_cancelled)) },
			enabledCondition = {
				!it.schoolBackgroundList.contains("cancelled")
			},
			settingsRepository = viewModel.repository,
			value = { it.backgroundCancelled },
			onValueChange = { backgroundCancelled = it },
			showAlphaSlider = true,
			defaultValueLabel = stringResource(id = R.string.preferences_theme_color)
		)

		ColorPreference(
			title = { Text(stringResource(R.string.preference_background_cancelled_past)) },
			enabledCondition = {
				!it.schoolBackgroundList.contains("cancelled")
			},
			settingsRepository = viewModel.repository,
			value = { it.backgroundCancelledPast },
			onValueChange = { backgroundCancelledPast = it },
			showAlphaSlider = true,
			defaultValueLabel = stringResource(id = R.string.preferences_theme_color)
		)

		ConfirmDialogPreference(
			title = { Text(stringResource(R.string.preference_timetable_colors_reset)) },
			summary = { Text(stringResource(R.string.preference_timetable_colors_reset_desc)) },
			leadingContent = {
				Icon(
					painter = painterResource(R.drawable.settings_reset),
					contentDescription = null
				)
			},
			dialogTitle = { Text(stringResource(R.string.preference_dialog_colors_reset_title)) },
			dialogText = { Text(stringResource(R.string.preference_dialog_colors_reset_text)) },
			onConfirm = {
				viewModel.resetColors()
			}
		)
	}

	PreferenceGroup(stringResource(id = R.string.preference_category_styling_themes)) {
		ColorPreference(
			title = { Text(stringResource(R.string.preferences_theme_color)) },
			leadingContent = {
				Icon(
					painter = painterResource(R.drawable.settings_timetable_format_paint),
					contentDescription = null
				)
			},
			settingsRepository = viewModel.repository,
			value = {
				if (it.customThemeColor)
					it.themeColor
				else
					viewModel.repository.getSettingsDefaults().themeColor
			},
			onValueChange = {
				customThemeColor = it != viewModel.repository.getSettingsDefaults().themeColor
				themeColor = it
			},
			defaultValueLabel = stringResource(id = R.string.preferences_theme_color_system)
		)

		ListPreference(
			title = { Text(stringResource(R.string.preference_dark_theme)) },
			supportingContent = { value, _ -> Text(value.second) },
			leadingContent = {
				Icon(
					painter = painterResource(R.drawable.settings_timetable_brightness_medium),
					contentDescription = null
				)
			},
			entries = stringArrayResource(id = R.array.preference_dark_theme_values),
			entryLabels = stringArrayResource(id = R.array.preference_dark_theme),
			settingsRepository = viewModel.repository,
			value = { it.darkTheme },
			onValueChange = { darkTheme = it }
		)

		SwitchPreference(
			title = { Text(stringResource(R.string.preference_dark_theme_oled)) },
			summary = { Text(stringResource(R.string.preference_dark_theme_oled_desc)) },
			leadingContent = {
				Icon(
					painter = painterResource(R.drawable.settings_timetable_format_oled),
					contentDescription = null
				)
			},
			enabledCondition = { it.darkTheme != "off" },
			settingsRepository = viewModel.repository,
			value = { it.darkThemeOled },
			onValueChange = { darkThemeOled = it }
		)
	}
}
