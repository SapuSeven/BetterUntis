package com.sapuseven.untis.ui.activities.settings

import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.google.accompanist.flowlayout.MainAxisAlignment
import com.sapuseven.compose.protostore.ui.preferences.ColorPreference
import com.sapuseven.compose.protostore.ui.preferences.InputPreference
import com.sapuseven.compose.protostore.ui.preferences.ListPreference
import com.sapuseven.compose.protostore.ui.preferences.MultiSelectListPreference
import com.sapuseven.compose.protostore.ui.preferences.NumericInputPreference
import com.sapuseven.compose.protostore.ui.preferences.Preference
import com.sapuseven.compose.protostore.ui.preferences.PreferenceGroup
import com.sapuseven.compose.protostore.ui.preferences.RangeInputPreference
import com.sapuseven.compose.protostore.ui.preferences.SliderPreference
import com.sapuseven.compose.protostore.ui.preferences.SwitchPreference
import com.sapuseven.compose.protostore.ui.preferences.WeekRangePreference
import com.sapuseven.untis.BuildConfig
import com.sapuseven.untis.R
import com.sapuseven.untis.preferences.PreferenceScreen
import com.sapuseven.untis.ui.navigation.AppRoutes
import com.sapuseven.untis.ui.preferences.ElementPickerPreference
import io.sentry.Sentry

fun NavGraphBuilder.SettingsNav(
	navController: NavHostController
) {
	composable<AppRoutes.Settings.Categories> {
		SettingsScreen(navController = navController, title = null) {
			PreferenceScreen(
				key = AppRoutes.Settings.General,
				title = { Text(stringResource(id = R.string.preferences_general)) },
				icon = {
					Icon(
						painter = painterResource(id = R.drawable.settings_general),
						contentDescription = null
					)
				},
				navController = navController
			)

			PreferenceScreen(
				key = AppRoutes.Settings.Styling,
				title = { Text(stringResource(id = R.string.preferences_styling)) },
				icon = {
					Icon(
						painter = painterResource(id = R.drawable.settings_styling),
						contentDescription = null
					)
				},
				navController = navController
			)

			PreferenceScreen(
				key = AppRoutes.Settings.Timetable(),
				title = { Text(stringResource(id = R.string.preferences_timetable)) },
				icon = {
					Icon(
						painter = painterResource(id = R.drawable.settings_timetable),
						contentDescription = null
					)
				},
				navController = navController
			)

			PreferenceScreen(
				key = AppRoutes.Settings.Notifications,
				title = { Text(stringResource(id = R.string.preferences_notifications)) },
				icon = {
					Icon(
						painter = painterResource(id = R.drawable.settings_notifications),
						contentDescription = null
					)
				},
				navController = navController
			)

			PreferenceScreen(
				key = AppRoutes.Settings.Connectivity,
				title = { Text(stringResource(id = R.string.preferences_connectivity)) },
				icon = {
					Icon(
						painter = painterResource(id = R.drawable.settings_connectivity),
						contentDescription = null
					)
				},
				navController = navController
			)

			PreferenceScreen(
				key = AppRoutes.Settings.About,
				title = { Text(stringResource(id = R.string.preferences_info)) },
				icon = {
					Icon(
						painter = painterResource(id = R.drawable.settings_info),
						contentDescription = null
					)
				},
				navController = navController
			)
		}
	}
	composable<AppRoutes.Settings.General> {
		SettingsScreen(
			navController = navController,
			title = stringResource(id = R.string.preferences_general)
		) { viewModel ->
			PreferenceGroup(stringResource(id = R.string.preference_category_general_behaviour)) {
				SwitchPreference(
					title = { Text(stringResource(R.string.preference_double_tap_to_exit)) },
					settingsRepository = viewModel.repository,
					value = { it.exitConfirmation },
					onValueChange = { exitConfirmation = it }
				)

				SwitchPreference(
					title = { Text(stringResource(R.string.preference_flinging_enable)) },
					settingsRepository = viewModel.repository,
					value = { it.flingEnable },
					onValueChange = { flingEnable = it }
				)
			}

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
				PreferenceGroup(stringResource(id = R.string.preference_category_app_language)) {
					val packageName = LocalContext.current.packageName
					Preference(
						title = { Text(text = stringResource(id = R.string.preference_app_language)) },
						onClick = {
							/*TODO languageSettingsLauncher.launch(
								Intent(
									android.provider.Settings.ACTION_APP_LOCALE_SETTINGS,
									Uri.parse("package:$packageName")
								)
							)*/
						}
					)
				}
			}

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
			}

			PreferenceGroup(stringResource(id = R.string.preference_category_general_automute)) {
				SwitchPreference(
					title = { Text(stringResource(R.string.preference_automute_enable)) },
					summary = { Text(stringResource(R.string.preference_automute_enable_summary)) },
					settingsRepository = viewModel.repository,
					value = { it.automuteEnable },
					onValueChange = {
						automuteEnable = it
						/*TODO if (it) {
							if (requestAutoMutePermission(
									autoMuteSettingsLauncher
								)
							) {
								AutoMuteSetupWorker.enqueue(
									WorkManager.getInstance(this@SettingsActivity),
									user
								)
								true
							} else false
						} else {
							disableAutoMute()
							false
						}*/
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
		}
	}
	composable<AppRoutes.Settings.Styling> {
		SettingsScreen(
			navController = navController,
			title = stringResource(id = R.string.preferences_styling)
		) { viewModel ->
			PreferenceGroup(stringResource(id = R.string.preference_category_styling_colors)) {
				ColorPreference(
					title = { Text(stringResource(R.string.preference_background_future)) },
					showAlphaSlider = true,
					settingsRepository = viewModel.repository,
					value = { it.backgroundFuture },
					onValueChange = { backgroundFuture = it }
				)

				ColorPreference(
					title = { Text(stringResource(R.string.preference_background_past)) },
					showAlphaSlider = true,
					settingsRepository = viewModel.repository,
					value = { it.backgroundPast },
					onValueChange = { backgroundPast = it }
				)

				ColorPreference(
					title = { Text(stringResource(R.string.preference_marker)) },
					settingsRepository = viewModel.repository,
					value = { it.marker },
					onValueChange = { marker = it }
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

				// TODO Implement
				/*ConfirmDialogPreference(
					title = { Text(stringResource(R.string.preference_timetable_colors_reset)) },
					dialogTitle = { Text(stringResource(R.string.preference_dialog_colors_reset_title)) },
					dialogText = { Text(stringResource(R.string.preference_dialog_colors_reset_text)) },
					onConfirm = {
					}
				)*/
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
					value = { it.themeColor },
					onValueChange = { themeColor = it },
					/*defaultValueLabel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
						stringResource(id = R.string.preferences_theme_color_system)
					else
						null*/
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
	}
	composable<AppRoutes.Settings.Timetable> {
		SettingsScreen(
			navController = navController,
			title = stringResource(id = R.string.preferences_timetable)
		) { viewModel ->
			val args = viewModel.savedStateHandle.toRoute<AppRoutes.Settings.Timetable>()

			ElementPickerPreference(
				title = { Text(stringResource(R.string.preference_timetable_personal_timetable)) },
				leadingContent = {
					Icon(
						painter = painterResource(id = R.drawable.settings_account_personal),
						contentDescription = null
					)
				},
				settingsRepository = viewModel.repository,
				value = { it.timetablePersonalTimetable },
				onValueChange = { timetablePersonalTimetable = it },
				elementPicker = viewModel.elementPicker,
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
				settingsRepository = viewModel.repository,
				value = { it.timetableHideTimeStamps },
				onValueChange = { timetableHideTimeStamps = it }
			)

			SwitchPreference(
				title = { Text(stringResource(R.string.preference_timetable_hide_cancelled)) },
				summary = { Text(stringResource(R.string.preference_timetable_hide_cancelled_desc)) },
				leadingContent = {
					Icon(
						painter = painterResource(id = R.drawable.settings_hide_cancelled),
						contentDescription = null
					)
				},
				settingsRepository = viewModel.repository,
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
				settingsRepository = viewModel.repository,
				value = { it.timetableSubstitutionsIrregular },
				onValueChange = { timetableSubstitutionsIrregular = it }
			)

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
			)

			PreferenceGroup(stringResource(id = R.string.preference_category_display_options)) {
				RangeInputPreference(
					title = { Text(stringResource(R.string.preference_timetable_range)) },
					leadingContent = {
						Icon(
							painter = painterResource(id = R.drawable.settings_timetable_range),
							contentDescription = null
						)
					},
					settingsRepository = viewModel.repository,
					value = { it.timetableRange },
					onValueChange = { timetableRange = it }
				)

				/*SwitchPreference(
					title = { Text(stringResource(R.string.preference_timetable_range_index_reset)) },
					enabledCondition = { it.timetableRange },
					settingsRepository = viewModel.repository,
value = { it.timetableRangeIndexReset },
onValueChange = { timetableRangeIndexReset = it }
				)*/

				/*SwitchPreference
				enabled = false,
				key = preference_timetable_range_hide_outside,
				summary = (not implemented),
				title = Hide lessons outside specified range" */
			}

			PreferenceGroup(stringResource(id = R.string.preference_category_timetable_item_appearance)) {
				NumericInputPreference(
					title = { Text(stringResource(R.string.preference_timetable_item_padding_overlap)) },
					leadingContent = {
						Icon(
							painter = painterResource(R.drawable.settings_timetable_padding),
							contentDescription = null
						)
					},
					unit = "dp",
					settingsRepository = viewModel.repository,
					value = { it.timetableItemPaddingOverlap },
					onValueChange = { timetableItemPaddingOverlap = it }
				)

				NumericInputPreference(
					title = { Text(stringResource(R.string.preference_timetable_item_padding)) },
					leadingContent = {
						Icon(
							painter = painterResource(R.drawable.settings_timetable_padding),
							contentDescription = null
						)
					},
					unit = "dp",
					settingsRepository = viewModel.repository,
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
					settingsRepository = viewModel.repository,
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
					settingsRepository = viewModel.repository,
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
					settingsRepository = viewModel.repository,
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
					settingsRepository = viewModel.repository,
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
					settingsRepository = viewModel.repository,
					value = { it.timetableLessonInfoFontSize },
					onValueChange = { timetableLessonInfoFontSize = it }
				)
			}

		}
	}
	composable<AppRoutes.Settings.Notifications> {
		SettingsScreen(
			navController = navController,
			title = stringResource(id = R.string.preferences_notifications)
		) { viewModel ->
			SwitchPreference(
				title = { Text(stringResource(R.string.preference_notifications_enable)) },
				summary = { Text(stringResource(R.string.preference_notifications_enable_desc)) },
				/*leadingContent = {
					Icon(
						painter = painterResource(R.drawable.settings_notifications_active),
						contentDescription = null
					)
				},*/
				settingsRepository = viewModel.repository,
				value = { it.notificationsEnable },
				onValueChange = {
					/*TODO if (it) {
						if (!canScheduleExactAlarms()) {
							dialogScheduleExactAlarms = true
							false
						} else if (!canPostNotifications()) {
							// TODO: This may not be backwards compatible
							requestNotificationPermissionLauncher.launch(
								Manifest.permission.POST_NOTIFICATIONS
							)
							false
						} else {
							enqueueNotificationSetup(user)
							true
						}
					} else {
						clearNotifications()
						false
					}*/
					notificationsEnable = it
				}
			)

			SwitchPreference(
				title = { Text(stringResource(R.string.preference_notifications_multiple)) },
				summary = { Text(stringResource(R.string.preference_notifications_multiple_desc)) },
				enabledCondition = { it.notificationsEnable },
				settingsRepository = viewModel.repository,
				value = { it.notificationsInMultiple },
				onValueChange = {
					//enqueueNotificationSetup(user)
					notificationsInMultiple = it
				}
			)

			SwitchPreference(
				title = { Text(stringResource(R.string.preference_notifications_first_lesson)) },
				summary = { Text(stringResource(R.string.preference_notifications_first_lesson_desc)) },
				enabledCondition = { it.notificationsEnable },
				settingsRepository = viewModel.repository,
				value = { it.notificationsBeforeFirst },
				onValueChange = {
					//enqueueNotificationSetup(user)
					notificationsBeforeFirst = it
				}
			)

			NumericInputPreference(
				title = { Text(stringResource(R.string.preference_notifications_first_lesson_time)) },
				unit = stringResource(R.string.preference_notifications_first_lesson_time_unit),
				enabledCondition = { it.notificationsBeforeFirst },
				settingsRepository = viewModel.repository,
				value = { it.notificationsBeforeFirstTime },
				onValueChange = {
					//enqueueNotificationSetup(user)
					notificationsBeforeFirstTime = it
				}
			)

			Preference(
				title = { Text(stringResource(R.string.preference_notifications_clear)) },
				onClick = {
					//clearNotifications()
				},
				leadingContent = {
					Icon(
						painter = painterResource(R.drawable.settings_notifications_clear_all),
						contentDescription = null
					)
				}
			)

			PreferenceGroup(stringResource(id = R.string.preference_category_notifications_visible_fields)) {
				ListPreference(
					title = { Text(stringResource(R.string.all_subjects)) },
					supportingContent = { value, _ -> Text(value.second) },
					leadingContent = {
						Icon(
							painter = painterResource(R.drawable.all_subject),
							contentDescription = null
						)
					},
					entries = stringArrayResource(id = R.array.preference_notifications_visibility_values),
					entryLabels = stringArrayResource(id = R.array.preference_notifications_visibility),
					enabledCondition = { it.notificationsEnable },
					settingsRepository = viewModel.repository,
					value = { it.notificationsVisibilitySubjects },
					onValueChange = { notificationsVisibilitySubjects = it }
				)

				ListPreference(
					title = { Text(stringResource(R.string.all_rooms)) },
					supportingContent = { value, _ -> Text(value.second) },
					leadingContent = {
						Icon(
							painter = painterResource(R.drawable.all_rooms),
							contentDescription = null
						)
					},
					entries = stringArrayResource(id = R.array.preference_notifications_visibility_values),
					entryLabels = stringArrayResource(id = R.array.preference_notifications_visibility),
					enabledCondition = { it.notificationsEnable },
					settingsRepository = viewModel.repository,
					value = { it.notificationsVisibilityRooms },
					onValueChange = { notificationsVisibilityRooms = it }
				)

				ListPreference(
					title = { Text(stringResource(R.string.all_teachers)) },
					supportingContent = { value, _ -> Text(value.second) },
					leadingContent = {
						Icon(
							painter = painterResource(R.drawable.all_teachers),
							contentDescription = null
						)
					},
					entries = stringArrayResource(id = R.array.preference_notifications_visibility_values),
					entryLabels = stringArrayResource(id = R.array.preference_notifications_visibility),
					enabledCondition = { it.notificationsEnable },
					settingsRepository = viewModel.repository,
					value = { it.notificationsVisibilityTeachers },
					onValueChange = { notificationsVisibilityTeachers = it }
				)

				ListPreference(
					title = { Text(stringResource(R.string.all_classes)) },
					supportingContent = { value, _ -> Text(value.second) },
					leadingContent = {
						Icon(
							painter = painterResource(R.drawable.all_classes),
							contentDescription = null
						)
					},
					entries = stringArrayResource(id = R.array.preference_notifications_visibility_values),
					entryLabels = stringArrayResource(id = R.array.preference_notifications_visibility),
					enabledCondition = { it.notificationsEnable },
					settingsRepository = viewModel.repository,
					value = { it.notificationsVisibilityClasses },
					onValueChange = { notificationsVisibilityClasses = it }
				)
			}
		}
	}
	composable<AppRoutes.Settings.Connectivity> {
		SettingsScreen(
			navController = navController,
			title = stringResource(id = R.string.preferences_connectivity)
		) { viewModel ->
			SwitchPreference(
				title = { Text(stringResource(R.string.preference_connectivity_refresh_in_background)) },
				summary = { Text(stringResource(R.string.preference_connectivity_refresh_in_background_desc)) },
				settingsRepository = viewModel.repository,
				value = { it.connectivityRefreshInBackground },
				onValueChange = { connectivityRefreshInBackground = it }
			)

			PreferenceGroup(stringResource(id = R.string.preference_category_connectivity_proxy)) {
				InputPreference(
					title = { Text(stringResource(R.string.preference_connectivity_proxy_host)) },
					leadingContent = {
						Icon(
							painter = painterResource(R.drawable.settings_connectivity_proxy),
							contentDescription = null
						)
					},
					settingsRepository = viewModel.repository,
					value = { it.proxyHost },
					onValueChange = { proxyHost = it }
				)

				Preference(
					title = { Text(stringResource(R.string.preference_connectivity_proxy_about)) },
					onClick = {
						//openUrl(URL_WIKI_PROXY)
					},
					leadingContent = {
						Icon(
							painter = painterResource(R.drawable.settings_info),
							contentDescription = null
						)
					}
				)
			}

		}
	}

	composable<AppRoutes.Settings.About> {
		SettingsScreen(
			navController = navController,
			title = stringResource(id = R.string.preferences_info)
		) { viewModel ->
			Preference(
				title = { Text(stringResource(R.string.app_name)) },
				summary = {
					Text(
						stringResource(
							R.string.preference_info_app_version_desc,
							BuildConfig.VERSION_NAME,
							BuildConfig.VERSION_CODE
						)
					)
				},
				onClick = {
					//openUrl("$URL_GITHUB_REPOSITORY/releases")
				},
				leadingContent = {
					Icon(
						painter = painterResource(R.drawable.settings_about_app_icon),
						contentDescription = null
					)
				}
			)

			PreferenceGroup(stringResource(id = R.string.preference_info_general)) {
				val openDialog = remember { mutableStateOf(false) }

				Preference(
					title = { Text(stringResource(R.string.preference_info_github)) },
					summary = {
						//Text(URL_GITHUB_REPOSITORY)
					},
					onClick = {
						//openUrl(URL_GITHUB_REPOSITORY)
					},
					leadingContent = {
						Icon(
							painter = painterResource(R.drawable.settings_info_github),
							contentDescription = null
						)
					}
				)

				Preference(
					title = { Text(stringResource(R.string.preference_info_license)) },
					summary = { Text(stringResource(R.string.preference_info_license_desc)) },
					onClick = {
						//openUrl("$URL_GITHUB_REPOSITORY/blob/master/LICENSE")
					},
					leadingContent = {
						Icon(
							painter = painterResource(R.drawable.settings_info_github),
							contentDescription = null
						)
					}
				)

				Preference(
					title = { Text(stringResource(R.string.preference_info_contributors)) },
					summary = { Text(stringResource(R.string.preference_info_contributors_desc)) },
					onClick = {
						//openDialog.value = true
					},
					leadingContent = {
						Icon(
							painter = painterResource(R.drawable.settings_about_contributor),
							contentDescription = null
						)
					}
				)

				if (openDialog.value) {
					AlertDialog(
						onDismissRequest = {
							openDialog.value = false
						},
						confirmButton = {
							// TODO Migrate to Material FlowRow?
							com.google.accompanist.flowlayout.FlowRow(
								modifier = Modifier.padding(all = 8.dp),
								mainAxisAlignment = MainAxisAlignment.End,
								mainAxisSpacing = 8.dp
							) {
								TextButton(
									onClick = {
										openDialog.value = false
										//openUrl("https://docs.github.com/en/github/site-policy/github-privacy-statement")
									}
								) {
									Text(text = stringResource(id = R.string.preference_info_privacy_policy))
								}
								TextButton(
									onClick = {
										openDialog.value = false
									}
								) {
									Text(text = stringResource(id = R.string.all_cancel))
								}
								TextButton(
									onClick = {
										openDialog.value = false
										navController.navigate("contributors")
									}
								) {
									Text(text = stringResource(id = R.string.all_ok))
								}
							}
						},
						title = {
							Text(text = stringResource(id = R.string.preference_info_privacy))
						},
						text = {
							Text(stringResource(id = R.string.preference_info_privacy_desc))
						}
					)
				}

				Preference(
					title = { Text(stringResource(R.string.preference_info_libraries)) },
					summary = { Text(stringResource(R.string.preference_info_libraries_desc)) },
					onClick = { navController.navigate(AppRoutes.Settings.About.Libraries) },
					leadingContent = {
						Icon(
							painter = painterResource(R.drawable.settings_about_library),
							contentDescription = null
						)
					}
				)
			}
		}
	}
	composable<AppRoutes.Settings.About.Libraries> {
		SettingsScreen(
			navController = navController,
			title = stringResource(id = R.string.preference_info_libraries)
		) {}

		/*val colors = libraryColors(
		backgroundColor = colorScheme!!.background,
		contentColor = colorScheme!!.onBackground,
		badgeBackgroundColor = colorScheme!!.primary,
		badgeContentColor = colorScheme!!.onPrimary
	)
	/*
	* The about libraries (android library from mikepenz)
	* use a custom library file stored in R.raw.about_libs.
	* To modify the shown libraries edit the JSON file
	* about_libs.json
	*/
	LibrariesContainer(
		Modifier.fillMaxSize(),
		contentPadding = insetsPaddingValues(),
		colors = colors
	)*/
	}
	composable<AppRoutes.Settings.About.Contributors> {
		SettingsScreen(
			navController = navController,
			title = stringResource(id = R.string.preference_info_contributors)
		) {}

		/*var userList by remember { mutableStateOf(listOf<GithubUser>()) }
		val error = remember { mutableStateOf(true) }
		var loadingText by remember { mutableStateOf(getString(R.string.loading)) }

		LaunchedEffect(Unit) {
			scope.launch {
				"$URL_GITHUB_REPOSITORY_API/contributors"
					.httpGet()
					.awaitStringResult()
					.fold({ data ->
						userList = getJSON().decodeFromString(data)
						error.value = false
					}, {
						loadingText = getString(R.string.loading_failed)
						error.value = true
					})
			}
		}

		if (!error.value) {
			LazyColumn(
				modifier = Modifier
					.fillMaxHeight(),
				contentPadding = insetsPaddingValues()
			) {
				this.items(userList) {
					Contributor(
						githubUser = it,
						onClick = { openUrl(it.html_url) })
				}
			}
		} else {
			ListItem(
				headlineContent = {
					Text(loadingText)
				},
				leadingContent = {
					Icon(
						painter = painterResource(id = R.drawable.settings_about_contributor),
						contentDescription = ""
					)
				}
			)
		}*/
	}
}
