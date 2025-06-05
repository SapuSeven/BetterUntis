package com.sapuseven.untis.ui.pages.settings.fragments

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
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
import com.sapuseven.untis.data.repository.UserSettingsRepository
import com.sapuseven.untis.data.repository.withDefault
import com.sapuseven.untis.data.settings.model.DarkTheme
import com.sapuseven.untis.ui.pages.settings.SettingsScreenViewModel

@Composable
fun SettingsCategoryStyling(viewModel: SettingsScreenViewModel) {
	val defaultColors = UserSettingsRepository.getDefaultColors(MaterialTheme.colorScheme)
	
	PreferenceGroup(stringResource(id = R.string.preference_category_styling_colors)) {
		ColorPreference(
			title = { Text(stringResource(R.string.preference_background_future)) },
			showAlphaSlider = true,
			settingsRepository = viewModel.userSettingsRepository,
			value = { it.backgroundFuture.withDefault(it.hasBackgroundFuture(), it.defaultInstanceForType.backgroundFuture) },
			onValueChange = { backgroundFuture = it ?: run { clearBackgroundFuture(); return@ColorPreference }},
			defaultColorLabel = stringResource(R.string.preferences_default_color)
		)

		ColorPreference(
			title = { Text(stringResource(R.string.preference_background_past)) },
			showAlphaSlider = true,
			settingsRepository = viewModel.userSettingsRepository,
			value = { it.backgroundPast.withDefault(it.hasBackgroundPast(), it.defaultInstanceForType.backgroundPast) },
			onValueChange = { backgroundPast = it ?: run { clearBackgroundPast(); return@ColorPreference }},
			defaultColorLabel = stringResource(R.string.preferences_default_color)
		)

		ColorPreference(
			title = { Text(stringResource(R.string.preference_marker)) },
			settingsRepository = viewModel.userSettingsRepository,
			value = { it.marker.withDefault(it.hasMarker(), it.defaultInstanceForType.marker) },
			onValueChange = { marker = it ?: run { clearMarker(); return@ColorPreference }},
			defaultColorLabel = stringResource(R.string.preferences_default_color)
		)
	}

	PreferenceGroup(stringResource(id = R.string.preference_category_styling_backgrounds)) {
		MultiSelectListPreference(
			title = { Text(stringResource(R.string.preference_school_background)) },
			summary = { Text(stringResource(R.string.preference_school_background_desc)) },
			entries = stringArrayResource(id = R.array.preference_schoolcolors_values),
			entryLabels = stringArrayResource(id = R.array.preference_schoolcolors),
			settingsRepository = viewModel.userSettingsRepository,
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
			settingsRepository = viewModel.userSettingsRepository,
			value = { it.backgroundRegular.withDefault(it.hasBackgroundRegular(), defaultColors.backgroundRegular.toArgb()) },
			onValueChange = { backgroundRegular = it ?: run { clearBackgroundRegular(); return@ColorPreference }  },
			showAlphaSlider = true,
			defaultColor = defaultColors.backgroundRegular,
			defaultColorLabel = stringResource(id = R.string.preferences_theme_color)
		)

		ColorPreference(
			title = { Text(stringResource(R.string.preference_background_regular_past)) },
			enabledCondition = {
				!it.schoolBackgroundList.contains("regular")
			},
			settingsRepository = viewModel.userSettingsRepository,
			value = { it.backgroundRegularPast.withDefault(it.hasBackgroundRegularPast(), defaultColors.backgroundRegularPast.toArgb()) },
			onValueChange = { backgroundRegularPast = it ?: run { clearBackgroundRegularPast(); return@ColorPreference }  },
			showAlphaSlider = true,
			defaultColor = defaultColors.backgroundRegularPast,
			defaultColorLabel = stringResource(id = R.string.preferences_theme_color)
		)

		ColorPreference(
			title = { Text(stringResource(R.string.preference_background_exam)) },
			enabledCondition = {
				!it.schoolBackgroundList.contains("exam")
			},
			settingsRepository = viewModel.userSettingsRepository,
			value = { it.backgroundExam.withDefault(it.hasBackgroundExam(), defaultColors.backgroundExam.toArgb()) },
			onValueChange = { backgroundExam = it ?: run { clearBackgroundExam(); return@ColorPreference }  },
			showAlphaSlider = true,
			defaultColor = defaultColors.backgroundExam,
			defaultColorLabel = stringResource(id = R.string.preferences_theme_color)
		)

		ColorPreference(
			title = { Text(stringResource(R.string.preference_background_exam_past)) },
			enabledCondition = {
				!it.schoolBackgroundList.contains("exam")
			},
			settingsRepository = viewModel.userSettingsRepository,
			value = { it.backgroundExamPast.withDefault(it.hasBackgroundExamPast(), defaultColors.backgroundExamPast.toArgb()) },
			onValueChange = { backgroundExamPast = it ?: run { clearBackgroundExamPast(); return@ColorPreference }  },
			showAlphaSlider = true,
			defaultColor = defaultColors.backgroundExamPast,
			defaultColorLabel = stringResource(id = R.string.preferences_theme_color)
		)

		ColorPreference(
			title = { Text(stringResource(R.string.preference_background_irregular)) },
			enabledCondition = {
				!it.schoolBackgroundList.contains("irregular")
			},
			settingsRepository = viewModel.userSettingsRepository,
			value = { it.backgroundIrregular.withDefault(it.hasBackgroundIrregular(), defaultColors.backgroundIrregular.toArgb()) },
			onValueChange = { backgroundIrregular = it ?: run { clearBackgroundIrregular(); return@ColorPreference }  },
			showAlphaSlider = true,
			defaultColor = defaultColors.backgroundIrregular,
			defaultColorLabel = stringResource(id = R.string.preferences_theme_color)
		)

		ColorPreference(
			title = { Text(stringResource(R.string.preference_background_irregular_past)) },
			enabledCondition = {
				!it.schoolBackgroundList.contains("irregular")
			},
			settingsRepository = viewModel.userSettingsRepository,
			value = { it.backgroundIrregularPast.withDefault(it.hasBackgroundIrregularPast(), defaultColors.backgroundIrregularPast.toArgb()) },
			onValueChange = { backgroundIrregularPast = it ?: run { clearBackgroundIrregularPast(); return@ColorPreference }  },
			showAlphaSlider = true,
			defaultColor = defaultColors.backgroundIrregularPast,
			defaultColorLabel = stringResource(id = R.string.preferences_theme_color)
		)

		ColorPreference(
			title = { Text(stringResource(R.string.preference_background_cancelled)) },
			enabledCondition = {
				!it.schoolBackgroundList.contains("cancelled")
			},
			settingsRepository = viewModel.userSettingsRepository,
			value = { it.backgroundCancelled.withDefault(it.hasBackgroundCancelled(), defaultColors.backgroundCancelled.toArgb()) },
			onValueChange = { backgroundCancelled = it ?: run { clearBackgroundCancelled(); return@ColorPreference }  },
			showAlphaSlider = true,
			defaultColor = defaultColors.backgroundCancelled,
			defaultColorLabel = stringResource(id = R.string.preferences_theme_color)
		)

		ColorPreference(
			title = { Text(stringResource(R.string.preference_background_cancelled_past)) },
			enabledCondition = {
				!it.schoolBackgroundList.contains("cancelled")
			},
			settingsRepository = viewModel.userSettingsRepository,
			value = { it.backgroundCancelledPast.withDefault(it.hasBackgroundCancelledPast(), defaultColors.backgroundCancelledPast.toArgb()) },
			onValueChange = { backgroundCancelledPast = it ?: run { clearBackgroundCancelledPast(); return@ColorPreference }  },
			showAlphaSlider = true,
			defaultColor = defaultColors.backgroundCancelledPast,
			defaultColorLabel = stringResource(id = R.string.preferences_theme_color)
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
			settingsRepository = viewModel.userSettingsRepository,
			value = { it.themeColor.withDefault(it.hasThemeColor(), defaultColors.themeColor.toArgb()) },
			onValueChange = {
				themeColor = it ?: run { clearThemeColor(); return@ColorPreference }
			},
			defaultColor = defaultColors.themeColor,
			defaultColorLabel = stringResource(id = R.string.preferences_theme_color_system)
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
			settingsRepository = viewModel.userSettingsRepository,
			value = { it.darkTheme.number.toString() },
			onValueChange = { darkTheme = DarkTheme.forNumber(it.toInt()) }
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
			enabledCondition = { it.darkTheme != DarkTheme.LIGHT },
			settingsRepository = viewModel.userSettingsRepository,
			value = { it.darkThemeOled },
			onValueChange = { darkThemeOled = it }
		)
	}
}
