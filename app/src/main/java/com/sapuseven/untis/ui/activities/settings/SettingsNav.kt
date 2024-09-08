package com.sapuseven.untis.ui.activities.settings

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.sapuseven.compose.protostore.ui.Preference
import com.sapuseven.compose.protostore.ui.PreferenceGroup
import com.sapuseven.compose.protostore.ui.SwitchPreference
import com.sapuseven.untis.R
import com.sapuseven.untis.data.settings.model.UserSettings
import com.sapuseven.untis.preferences.PreferenceScreen
import com.sapuseven.untis.ui.navigation.AppRoutes

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
				key = AppRoutes.Settings.Timetable,
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
					settingsRepository = viewModel,
					transform = { it.exitConfirmation },
					onCheckedChange = { exitConfirmation = it }
				)

				SwitchPreference(
					title = { Text(stringResource(R.string.preference_flinging_enable)) },
					settingsRepository = viewModel,
					transform = { it.flingEnable },
					onCheckedChange = { flingEnable = it }
				)
			}
		}

		/*VerticalScrollColumn {

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
				PreferenceCategory(stringResource(id = R.string.preference_category_app_language)) {
					val packageName = LocalContext.current.packageName
					Preference(
						title = { Text(text = stringResource(id = R.string.preference_app_language)) },
						onClick = {

							languageSettingsLauncher.launch(
								Intent(
									android.provider.Settings.ACTION_APP_LOCALE_SETTINGS,
									Uri.parse("package:$packageName")
								)
							)

						},
						dataStore = UntisPreferenceDataStore.emptyDataStore()
					)
				}
			}

			PreferenceCategory(stringResource(R.string.preference_category_general_week_display)) {
				WeekRangePickerPreference(
					title = { Text(stringResource(R.string.preference_week_custom_range)) },
					dataStore = dataStorePreferences.weekCustomRange
				)

				SwitchPreference(
					title = { Text(stringResource(R.string.preference_week_snap_to_days)) },
					summary = { Text(stringResource(R.string.preference_week_snap_to_days_summary)) },
					dataStore = dataStorePreferences.weekSnapToDays
				)

				SliderPreference(
					valueRange = 0f..7f,
					steps = 6,
					title = { Text(stringResource(R.string.preference_week_display_length)) },
					summary = { Text(stringResource(R.string.preference_week_display_length_summary)) },
					dependency = dataStorePreferences.weekSnapToDays,
					showSeekBarValue = true,
					dataStore = dataStorePreferences.weekCustomLength
				)
			}

			PreferenceCategory(stringResource(id = R.string.preference_category_general_automute)) {
				SwitchPreference(
					title = { Text(stringResource(R.string.preference_automute_enable)) },
					summary = { Text(stringResource(R.string.preference_automute_enable_summary)) },
					onCheckedChange = {
						if (it) {
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
						}
					},
					dataStore = dataStorePreferences.automuteEnable
				)
				SwitchPreference(
					title = { Text(stringResource(R.string.preference_automute_cancelled_lessons)) },
					dependency = dataStorePreferences.automuteEnable,
					dataStore = dataStorePreferences.automuteCancelledLessons
				)
				SwitchPreference(
					title = { Text(stringResource(R.string.preference_automute_mute_priority)) },
					dependency = dataStorePreferences.automuteEnable,
					dataStore = dataStorePreferences.automuteMutePriority
				)

				SliderPreference(
					valueRange = 0f..20f,
					steps = 19,
					title = { Text(stringResource(R.string.preference_automute_minimum_break_length)) },
					summary = { Text(stringResource(R.string.preference_automute_minimum_break_length_summary)) },
					showSeekBarValue = true,
					dependency = dataStorePreferences.automuteEnable,
					dataStore = dataStorePreferences.automuteMinimumBreakLength
				)
			}

			PreferenceCategory(stringResource(R.string.preference_category_reports)) {
				Preference(
					title = { Text(stringResource(R.string.preference_reports_info)) },
					summary = { Text(stringResource(R.string.preference_reports_info_desc)) },
					icon = {
						Icon(
							painter = painterResource(R.drawable.settings_info),
							contentDescription = null
						)
					},
					dataStore = UntisPreferenceDataStore.emptyDataStore()
				)

				SwitchPreference(
					title = { Text(stringResource(R.string.preference_reports_breadcrumbs)) },
					summary = { Text(stringResource(R.string.preference_reports_breadcrumbs_desc)) },
					dataStore = UntisPreferenceDataStore(
						reportsDataStore,
						reportsDataStoreBreadcrumbsEnable.first,
						reportsDataStoreBreadcrumbsEnable.second
					)
				)

				if (BuildConfig.DEBUG)
					Preference(
						title = { Text("Send test report") },
						summary = { Text("Sends a report to Sentry to test error reporting") },
						onClick = {
							Sentry.captureException(java.lang.Exception("Test report"))
							Toast.makeText(
								this@SettingsActivity,
								"Report has been sent",
								Toast.LENGTH_SHORT
							).show()
						},
						dataStore = UntisPreferenceDataStore.emptyDataStore()
					)
			}
		}*/
	}
	composable<AppRoutes.Settings.Styling> {
		SettingsScreen(
			navController = navController,
			title = stringResource(id = R.string.preferences_styling)
		) {}

		/*VerticalScrollColumn {
			PreferenceCategory(stringResource(id = R.string.preference_category_styling_colors)) {
				ColorPreference(
					title = { Text(stringResource(R.string.preference_background_future)) },
					showAlphaSlider = true,
					dataStore = dataStorePreferences.backgroundFuture
				)

				ColorPreference(
					title = { Text(stringResource(R.string.preference_background_past)) },
					showAlphaSlider = true,
					dataStore = dataStorePreferences.backgroundPast
				)

				ColorPreference(
					title = { Text(stringResource(R.string.preference_marker)) },
					dataStore = dataStorePreferences.marker
				)
			}

			PreferenceCategory(stringResource(id = R.string.preference_category_styling_backgrounds)) {
				MultiSelectListPreference(
					title = { Text(stringResource(R.string.preference_school_background)) },
					summary = { Text(stringResource(R.string.preference_school_background_desc)) },
					entries = stringArrayResource(id = R.array.preference_schoolcolors_values),
					entryLabels = stringArrayResource(id = R.array.preference_schoolcolors),
					dataStore = dataStorePreferences.schoolBackground
				)

				ColorPreference(
					title = { Text(stringResource(R.string.preference_background_regular)) },
					dependency = dataStorePreferences.schoolBackground.with(
						dependencyValue = { !it.contains("regular") }
					),
					dataStore = dataStorePreferences.backgroundRegular,
					showAlphaSlider = true,
					defaultValueLabel = stringResource(id = R.string.preferences_theme_color)
				)

				ColorPreference(
					title = { Text(stringResource(R.string.preference_background_regular_past)) },
					dependency = dataStorePreferences.schoolBackground.with(
						dependencyValue = { !it.contains("regular") }
					),
					dataStore = dataStorePreferences.backgroundRegularPast,
					showAlphaSlider = true,
					defaultValueLabel = stringResource(id = R.string.preferences_theme_color)
				)

				ColorPreference(
					title = { Text(stringResource(R.string.preference_background_exam)) },
					dependency = dataStorePreferences.schoolBackground.with(
						dependencyValue = { !it.contains("exam") }
					),
					dataStore = dataStorePreferences.backgroundExam,
					showAlphaSlider = true,
					defaultValueLabel = stringResource(id = R.string.preferences_theme_color)
				)

				ColorPreference(
					title = { Text(stringResource(R.string.preference_background_exam_past)) },
					dependency = dataStorePreferences.schoolBackground.with(
						dependencyValue = { !it.contains("exam") }
					),
					dataStore = dataStorePreferences.backgroundExamPast,
					showAlphaSlider = true,
					defaultValueLabel = stringResource(id = R.string.preferences_theme_color)
				)

				ColorPreference(
					title = { Text(stringResource(R.string.preference_background_irregular)) },
					dependency = dataStorePreferences.schoolBackground.with(
						dependencyValue = { !it.contains("irregular") }
					),
					dataStore = dataStorePreferences.backgroundIrregular,
					showAlphaSlider = true,
					defaultValueLabel = stringResource(id = R.string.preferences_theme_color)
				)

				ColorPreference(
					title = { Text(stringResource(R.string.preference_background_irregular_past)) },
					dependency = dataStorePreferences.schoolBackground.with(
						dependencyValue = { !it.contains("irregular") }
					),
					dataStore = dataStorePreferences.backgroundIrregularPast,
					showAlphaSlider = true,
					defaultValueLabel = stringResource(id = R.string.preferences_theme_color)
				)

				ColorPreference(
					title = { Text(stringResource(R.string.preference_background_cancelled)) },
					dependency = dataStorePreferences.schoolBackground.with(
						dependencyValue = { !it.contains("cancelled") }
					),
					dataStore = dataStorePreferences.backgroundCancelled,
					showAlphaSlider = true,
					defaultValueLabel = stringResource(id = R.string.preferences_theme_color)
				)

				ColorPreference(
					title = { Text(stringResource(R.string.preference_background_cancelled_past)) },
					dependency = dataStorePreferences.schoolBackground.with(
						dependencyValue = { !it.contains("cancelled") }
					),
					dataStore = dataStorePreferences.backgroundCancelledPast,
					showAlphaSlider = true,
					defaultValueLabel = stringResource(id = R.string.preferences_theme_color)
				)

				/*ConfirmDialogPreference(
					title = { Text(stringResource(R.string.preference_timetable_colors_reset)) },
					dialogTitle = { Text(stringResource(R.string.preference_dialog_colors_reset_title)) },
					dialogText = { Text(stringResource(R.string.preference_dialog_colors_reset_text)) },
					onConfirm = {
					}
				)*/
			}

			PreferenceCategory(stringResource(id = R.string.preference_category_styling_themes)) {
				ColorPreference(
					title = { Text(stringResource(R.string.preferences_theme_color)) },
					icon = {
						Icon(
							painter = painterResource(R.drawable.settings_timetable_format_paint),
							contentDescription = null
						)
					},
					dataStore = dataStorePreferences.themeColor,
					defaultValueLabel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
						stringResource(id = R.string.preferences_theme_color_system)
					else
						null
				)

				ListPreference(
					title = { Text(stringResource(R.string.preference_dark_theme)) },
					summary = { Text(it.second) },
					icon = {
						Icon(
							painter = painterResource(R.drawable.settings_timetable_brightness_medium),
							contentDescription = null
						)
					},
					entries = stringArrayResource(id = R.array.preference_dark_theme_values),
					entryLabels = stringArrayResource(id = R.array.preference_dark_theme),
					dataStore = dataStorePreferences.darkTheme
				)

				SwitchPreference(
					title = { Text(stringResource(R.string.preference_dark_theme_oled)) },
					summary = { Text(stringResource(R.string.preference_dark_theme_oled_desc)) },
					icon = {
						Icon(
							painter = painterResource(R.drawable.settings_timetable_format_oled),
							contentDescription = null
						)
					},
					dependency = dataStorePreferences.darkTheme,
					dataStore = dataStorePreferences.darkThemeOled
				)
			}
		}*/
	}
	composable<AppRoutes.Settings.Timetable> {
		SettingsScreen(
			navController = navController,
			title = stringResource(id = R.string.preferences_timetable)
		) {}

		/*VerticalScrollColumn {
			ElementPickerPreference(
				title = { Text(stringResource(R.string.preference_timetable_personal_timetable)) },
				icon = {
					Icon(
						painter = painterResource(id = R.drawable.settings_account_personal),
						contentDescription = null
					)
				},
				dataStore = dataStorePreferences.timetablePersonalTimetable,
				timetableDatabaseInterface = timetableDatabaseInterface,
				highlight = preferenceHighlight == "preference_timetable_personal_timetable"
			)

			SwitchPreference(
				title = { Text(stringResource(R.string.preference_timetable_hide_timestamps)) },
				summary = { Text(stringResource(R.string.preference_timetable_hide_timestamps_desc)) },
				icon = {
					Icon(
						painter = painterResource(id = R.drawable.settings_hide_timestamps),
						contentDescription = null
					)
				},
				dataStore = dataStorePreferences.timetableHideTimeStamps
			)

			SwitchPreference(
				title = { Text(stringResource(R.string.preference_timetable_hide_cancelled)) },
				summary = { Text(stringResource(R.string.preference_timetable_hide_cancelled_desc)) },
				icon = {
					Icon(
						painter = painterResource(id = R.drawable.settings_hide_cancelled),
						contentDescription = null
					)
				},
				dataStore = dataStorePreferences.timetableHideCancelled
			)

			SwitchPreference(
				title = { Text(stringResource(R.string.preference_timetable_substitutions_irregular)) },
				summary = { Text(stringResource(R.string.preference_timetable_substitutions_irregular_desc)) },
				icon = {
					Icon(
						painter = painterResource(id = R.drawable.settings_detect_irregular),
						contentDescription = null
					)
				},
				dataStore = dataStorePreferences.timetableSubstitutionsIrregular
			)

			SwitchPreference(
				title = { Text(stringResource(R.string.preference_timetable_background_irregular)) },
				summary = { Text(stringResource(R.string.preference_timetable_background_irregular_desc)) },
				icon = {
					Icon(
						painter = painterResource(id = R.drawable.settings_background_irregular),
						contentDescription = null
					)
				},
				dependency = dataStorePreferences.timetableSubstitutionsIrregular,
				dataStore = dataStorePreferences.timetableBackgroundIrregular
			)

			PreferenceCategory(stringResource(id = R.string.preference_category_display_options)) {
				RangeInputPreference(
					title = { Text(stringResource(R.string.preference_timetable_range)) },
					icon = {
						Icon(
							painter = painterResource(id = R.drawable.settings_timetable_range),
							contentDescription = null
						)
					},
					dataStore = dataStorePreferences.timetableRange
				)

				/*SwitchPreference(
					title = { Text(stringResource(R.string.preference_timetable_range_index_reset)) },
					dependency = dataStorePreferences.timetableRange,
					dataStore = dataStorePreferences.timetableRangeIndexReset
				)*/

				/*SwitchPreference
				enabled = false,
				key = preference_timetable_range_hide_outside,
				summary = (not implemented),
				title = Hide lessons outside specified range" */
			}

			PreferenceCategory(stringResource(id = R.string.preference_category_timetable_item_appearance)) {
				NumericInputPreference(
					title = { Text(stringResource(R.string.preference_timetable_item_padding_overlap)) },
					icon = {
						Icon(
							painter = painterResource(R.drawable.settings_timetable_padding),
							contentDescription = null
						)
					},
					unit = "dp",
					dataStore = dataStorePreferences.timetableItemPaddingOverlap
				)

				NumericInputPreference(
					title = { Text(stringResource(R.string.preference_timetable_item_padding)) },
					icon = {
						Icon(
							painter = painterResource(R.drawable.settings_timetable_padding),
							contentDescription = null
						)
					},
					unit = "dp",
					dataStore = dataStorePreferences.timetableItemPadding
				)

				NumericInputPreference(
					title = { Text(stringResource(R.string.preference_timetable_item_corner_radius)) },
					icon = {
						Icon(
							painter = painterResource(R.drawable.settings_timetable_rounded_corner),
							contentDescription = null
						)
					},
					unit = "dp",
					dataStore = dataStorePreferences.timetableItemCornerRadius
				)
			}

			PreferenceCategory(stringResource(id = R.string.preference_category_timetable_lesson_text)) {
				SwitchPreference(
					title = { Text(stringResource(R.string.preference_timetable_centered_lesson_info)) },
					icon = {
						Icon(
							painter = painterResource(R.drawable.settings_timetable_align_center),
							contentDescription = null
						)
					},
					dataStore = dataStorePreferences.timetableCenteredLessonInfo
				)

				SwitchPreference(
					title = { Text(stringResource(R.string.preference_timetable_bold_lesson_name)) },
					icon = {
						Icon(
							painter = painterResource(R.drawable.settings_timetable_format_bold),
							contentDescription = null
						)
					},
					dataStore = dataStorePreferences.timetableBoldLessonName
				)

				NumericInputPreference(
					title = { Text(stringResource(R.string.preference_timetable_lesson_name_font_size)) },
					icon = {
						Icon(
							painter = painterResource(R.drawable.settings_timetable_font_size),
							contentDescription = null
						)
					},
					unit = "sp",
					dataStore = dataStorePreferences.timetableLessonNameFontSize
				)

				NumericInputPreference(
					title = { Text(stringResource(R.string.preference_timetable_lesson_info_font_size)) },
					icon = {
						Icon(
							painter = painterResource(R.drawable.settings_timetable_font_size),
							contentDescription = null
						)
					},
					unit = "sp",
					dataStore = dataStorePreferences.timetableLessonInfoFontSize
				)
			}
		}*/
	}
	composable<AppRoutes.Settings.Notifications> {
		SettingsScreen(
			navController = navController,
			title = stringResource(id = R.string.preferences_notifications)
		) {}

		/*VerticalScrollColumn {
			SwitchPreference(
				title = { Text(stringResource(R.string.preference_notifications_enable)) },
				summary = { Text(stringResource(R.string.preference_notifications_enable_desc)) },
				/*icon = {
					Icon(
						painter = painterResource(R.drawable.settings_notifications_active),
						contentDescription = null
					)
				},*/
				onCheckedChange = { enable ->
					if (enable) {
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
					}
				},
				dataStore = dataStorePreferences.notificationsEnable
			)

			SwitchPreference(
				title = { Text(stringResource(R.string.preference_notifications_multiple)) },
				summary = { Text(stringResource(R.string.preference_notifications_multiple_desc)) },
				dependency = dataStorePreferences.notificationsEnable,
				onCheckedChange = {
					enqueueNotificationSetup(user)
					it
				},
				dataStore = dataStorePreferences.notificationsInMultiple
			)

			SwitchPreference(
				title = { Text(stringResource(R.string.preference_notifications_first_lesson)) },
				summary = { Text(stringResource(R.string.preference_notifications_first_lesson_desc)) },
				dependency = dataStorePreferences.notificationsEnable,
				onCheckedChange = {
					enqueueNotificationSetup(user)
					it
				},
				dataStore = dataStorePreferences.notificationsBeforeFirst
			)

			NumericInputPreference(
				title = { Text(stringResource(R.string.preference_notifications_first_lesson_time)) },
				unit = stringResource(R.string.preference_notifications_first_lesson_time_unit),
				dependency = dataStorePreferences.notificationsBeforeFirst,
				onChange = {
					enqueueNotificationSetup(user)
				},
				dataStore = dataStorePreferences.notificationsBeforeFirstTime
			)

			Preference(
				title = { Text(stringResource(R.string.preference_notifications_clear)) },
				onClick = { clearNotifications() },
				icon = {
					Icon(
						painter = painterResource(R.drawable.settings_notifications_clear_all),
						contentDescription = null
					)
				},
				dataStore = UntisPreferenceDataStore.emptyDataStore()
			)

			PreferenceCategory(stringResource(id = R.string.preference_category_notifications_visible_fields)) {
				ListPreference(
					title = { Text(stringResource(R.string.all_subjects)) },
					summary = { Text(it.second) },
					icon = {
						Icon(
							painter = painterResource(R.drawable.all_subject),
							contentDescription = null
						)
					},
					entries = stringArrayResource(id = R.array.preference_notifications_visibility_values),
					entryLabels = stringArrayResource(id = R.array.preference_notifications_visibility),
					dependency = dataStorePreferences.notificationsEnable,
					dataStore = dataStorePreferences.notificationsVisibilitySubjects
				)

				ListPreference(
					title = { Text(stringResource(R.string.all_rooms)) },
					summary = { Text(it.second) },
					icon = {
						Icon(
							painter = painterResource(R.drawable.all_rooms),
							contentDescription = null
						)
					},
					entries = stringArrayResource(id = R.array.preference_notifications_visibility_values),
					entryLabels = stringArrayResource(id = R.array.preference_notifications_visibility),
					dependency = dataStorePreferences.notificationsEnable,
					dataStore = dataStorePreferences.notificationsVisibilityRooms
				)

				ListPreference(
					title = { Text(stringResource(R.string.all_teachers)) },
					summary = { Text(it.second) },
					icon = {
						Icon(
							painter = painterResource(R.drawable.all_teachers),
							contentDescription = null
						)
					},
					entries = stringArrayResource(id = R.array.preference_notifications_visibility_values),
					entryLabels = stringArrayResource(id = R.array.preference_notifications_visibility),
					dependency = dataStorePreferences.notificationsEnable,
					dataStore = dataStorePreferences.notificationsVisibilityTeachers
				)

				ListPreference(
					title = { Text(stringResource(R.string.all_classes)) },
					summary = { Text(it.second) },
					icon = {
						Icon(
							painter = painterResource(R.drawable.all_classes),
							contentDescription = null
						)
					},
					entries = stringArrayResource(id = R.array.preference_notifications_visibility_values),
					entryLabels = stringArrayResource(id = R.array.preference_notifications_visibility),
					dependency = dataStorePreferences.notificationsEnable,
					dataStore = dataStorePreferences.notificationsVisibilityClasses
				)
			}
		}*/
	}
	composable<AppRoutes.Settings.Connectivity> {
		SettingsScreen(
			navController = navController,
			title = stringResource(id = R.string.preferences_connectivity)
		) {}

		/*VerticalScrollColumn {
			SwitchPreference(
				title = { Text(stringResource(R.string.preference_connectivity_refresh_in_background)) },
				summary = { Text(stringResource(R.string.preference_connectivity_refresh_in_background_desc)) },
				dataStore = dataStorePreferences.connectivityRefreshInBackground
			)

			PreferenceCategory(stringResource(id = R.string.preference_category_connectivity_proxy)) {
				InputPreference(
					title = { Text(stringResource(R.string.preference_connectivity_proxy_host)) },
					icon = {
						Icon(
							painter = painterResource(R.drawable.settings_connectivity_proxy),
							contentDescription = null
						)
					},
					dataStore = dataStorePreferences.proxyHost
				)

				Preference(
					title = { Text(stringResource(R.string.preference_connectivity_proxy_about)) },
					onClick = {
						openUrl(URL_WIKI_PROXY)
					},
					icon = {
						Icon(
							painter = painterResource(R.drawable.settings_info),
							contentDescription = null
						)
					},
					dataStore = UntisPreferenceDataStore.emptyDataStore()
				)
			}
		}*/
	}
	composable<AppRoutes.Settings.About> {
		SettingsScreen(
			navController = navController,
			title = stringResource(id = R.string.preferences_info)
		) {
			/*Preference(
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
					openUrl("$URL_GITHUB_REPOSITORY/releases")
				},
				icon = {
					Icon(
						painter = painterResource(R.drawable.settings_about_app_icon),
						contentDescription = null
					)
				},
				dataStore = UntisPreferenceDataStore.emptyDataStore()
			)

			PreferenceCategory(stringResource(id = R.string.preference_info_general)) {
				val openDialog = remember { mutableStateOf(false) }

				Preference(
					title = { Text(stringResource(R.string.preference_info_github)) },
					summary = { Text(URL_GITHUB_REPOSITORY) },
					onClick = {
						openUrl(URL_GITHUB_REPOSITORY)
					},
					icon = {
						Icon(
							painter = painterResource(R.drawable.settings_info_github),
							contentDescription = null
						)
					},
					dataStore = UntisPreferenceDataStore.emptyDataStore()
				)

				Preference(
					title = { Text(stringResource(R.string.preference_info_license)) },
					summary = { Text(stringResource(R.string.preference_info_license_desc)) },
					onClick = {
						openUrl("$URL_GITHUB_REPOSITORY/blob/master/LICENSE")
					},
					icon = {
						Icon(
							painter = painterResource(R.drawable.settings_info_github),
							contentDescription = null
						)
					},
					dataStore = UntisPreferenceDataStore.emptyDataStore()
				)

			Preference(
				title = { Text(stringResource(R.string.preference_info_contributors)) },
				summary = { Text(stringResource(R.string.preference_info_contributors_desc)) },
				onClick = {
					//openDialog.value = true
				},
				icon = {
					Icon(
						painter = painterResource(R.drawable.settings_about_contributor),
						contentDescription = null
					)
				},
				dataStore = UntisPreferenceDataStore.emptyDataStore()
			)

			if (openDialog.value) {
				AlertDialog(
					onDismissRequest = {
						openDialog.value = false
					},
					confirmButton = {
						FlowRow(
							modifier = Modifier.padding(all = 8.dp),
							mainAxisAlignment = MainAxisAlignment.End,
							mainAxisSpacing = 8.dp
						) {
							TextButton(
								onClick = {
									openDialog.value = false
									openUrl("https://docs.github.com/en/github/site-policy/github-privacy-statement")
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
				icon = {
					Icon(
						painter = painterResource(R.drawable.settings_about_library),
						contentDescription = null
					)
				},
				dataStore = UntisPreferenceDataStore.emptyDataStore()
			)
			}*/
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
