package com.sapuseven.untis.activities

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.system.Os
import kotlin.collections.Collection
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.WorkManager
import coil.compose.AsyncImage
import com.github.kittinunf.fuel.coroutines.awaitStringResult
import com.github.kittinunf.fuel.httpGet
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.ui.compose.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.LibraryDefaults.libraryColors
import com.mikepenz.aboutlibraries.util.withJson
import com.sapuseven.untis.BuildConfig
import com.sapuseven.untis.R
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.helpers.SerializationUtils.getJSON
import com.sapuseven.untis.models.github.GithubUser
import com.sapuseven.untis.preferences.PreferenceCategory
import com.sapuseven.untis.preferences.PreferenceScreen
import com.sapuseven.untis.preferences.UntisPreferenceDataStore
import com.sapuseven.untis.preferences.dataStorePreferences
import com.sapuseven.untis.receivers.AutoMuteReceiver
import com.sapuseven.untis.receivers.AutoMuteReceiver.Companion.EXTRA_BOOLEAN_MUTE
import com.sapuseven.untis.ui.functional.bottomInsets
import com.sapuseven.untis.ui.preferences.*
import com.sapuseven.untis.workers.AutoMuteSetupWorker
import com.sapuseven.untis.workers.NotificationSetupWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.JsonConfiguration

class SettingsActivity : BaseComposeActivity() {
	companion object {
		const val EXTRA_STRING_PREFERENCE_ROUTE = "com.sapuseven.untis.activities.settings.route"
		const val EXTRA_STRING_PREFERENCE_HIGHLIGHT =
			"com.sapuseven.untis.activities.settings.highlight"

		private const val URL_GITHUB_REPOSITORY = "https://github.com/SapuSeven/BetterUntis"
		private const val URL_WIKI_PROXY = "$URL_GITHUB_REPOSITORY/wiki/Proxy"
	}

	@OptIn(ExperimentalMaterial3Api::class)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		// Navigate to and highlight a preference if requested
		val preferencePath =
			(intent.extras?.getString(EXTRA_STRING_PREFERENCE_ROUTE)) ?: "preferences"
		val preferenceHighlight = (intent.extras?.getString(EXTRA_STRING_PREFERENCE_HIGHLIGHT))

		setContent {
			AppTheme(navBarInset = false) {
				withUser { user ->
					val navController = rememberNavController()
					var title by remember { mutableStateOf<String?>(null) }

					val autoMutePref = dataStorePreferences.automuteEnable
					val scope = rememberCoroutineScope()
					val systemSettingsLauncher =
						rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
							updateAutoMutePref(user, scope, autoMutePref, true)
						}
					updateAutoMutePref(user, scope, autoMutePref)

					Scaffold(
						topBar = {
							CenterAlignedTopAppBar(
								title = {
									Text(
										title
											?: stringResource(id = R.string.activity_title_settings)
									)
								},
								navigationIcon = {
									IconButton(onClick = { if (!navController.navigateUp()) finish() }) {
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
							NavHost(navController, startDestination = preferencePath) {
								composable("preferences") {
									title = null

									VerticalScrollColumn {
										PreferenceScreen(
											key = "preferences_general",
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
											key = "preferences_styling",
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
											key = "preferences_timetable",
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
											key = "preferences_notifications",
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
											key = "preferences_connectivity",
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
											key = "preferences_info",
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

								composable("preferences_general") {
									title = stringResource(id = R.string.preferences_general)

									VerticalScrollColumn {
										PreferenceCategory(stringResource(id = R.string.preference_category_general_behaviour)) {
											SwitchPreference(
												title = { Text(stringResource(R.string.preference_double_tap_to_exit)) },
												dataStore = dataStorePreferences.doubleTapToExit
											)

											SwitchPreference(
												title = { Text(stringResource(R.string.preference_flinging_enable)) },
												dataStore = dataStorePreferences.flingEnable
											)
										}

										PreferenceCategory(stringResource(R.string.preference_category_general_week_display)) {
											SwitchPreference(
												title = { Text(stringResource(R.string.preference_week_snap_to_days)) },
												summary = { Text(stringResource(R.string.preference_week_snap_to_days_summary)) },
												dataStore = dataStorePreferences.weekSnapToDays
											)

											WeekRangePickerPreference(
												title = { Text(stringResource(R.string.preference_week_custom_range)) },
												dataStore = dataStorePreferences.weekCustomRange
											)

											SliderPreference(
												valueRange = 0f..7f,
												steps = 6,
												title = { Text(stringResource(R.string.preference_week_display_length)) },
												summary = { Text(stringResource(R.string.preference_week_display_length_summary)) },
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
																systemSettingsLauncher
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
														it
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

										// TODO: Extract string resources
										PreferenceCategory("Error Reporting") {
											SwitchPreference(
												title = { Text("Enable additional error messages") },
												summary = { Text("This is used for non-critical background errors") },
												dataStore = dataStorePreferences.additionalErrorMessages
											)

											Preference(
												title = { Text("View logged errors") },
												summary = { Text("Crash logs and non-critical background errors") },
												onClick = { /*TODO*/ },
												dataStore = UntisPreferenceDataStore.emptyDataStore()
											)
										}
									}
								}

								composable("preferences_styling") {
									title = stringResource(id = R.string.preferences_styling)

									VerticalScrollColumn {
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
									}
								}

								composable("preferences_timetable") {
									title = stringResource(id = R.string.preferences_timetable)

									VerticalScrollColumn {
										ElementPickerPreference(
											title = { Text(stringResource(R.string.preference_timetable_personal_timetable)) },
											dataStore = dataStorePreferences.timetablePersonalTimetable,
											timetableDatabaseInterface = timetableDatabaseInterface,
											highlight = preferenceHighlight == "preference_timetable_personal_timetable"
										)

										SwitchPreference(
											title = { Text(stringResource(R.string.preference_timetable_hide_time_stamps)) },
											summary = { Text(stringResource(R.string.preference_timetable_hide_time_stamps_desc)) },
											dataStore = dataStorePreferences.timetableHideTimeStamps
										)

										SwitchPreference(
											title = { Text(stringResource(R.string.preference_timetable_hide_cancelled)) },
											summary = { Text(stringResource(R.string.preference_timetable_hide_cancelled_desc)) },
											dataStore = dataStorePreferences.timetableHideCancelled
										)

										SwitchPreference(
											title = { Text(stringResource(R.string.preference_timetable_substitutions_irregular)) },
											summary = { Text(stringResource(R.string.preference_timetable_substitutions_irregular_desc)) },
											dataStore = dataStorePreferences.timetableSubstitutionsIrregular
										)

										SwitchPreference(
											title = { Text(stringResource(R.string.preference_timetable_background_irregular)) },
											summary = { Text(stringResource(R.string.preference_timetable_background_irregular_desc)) },
											dependency = dataStorePreferences.timetableSubstitutionsIrregular,
											dataStore = dataStorePreferences.timetableBackgroundIrregular
										)

										PreferenceCategory(stringResource(id = R.string.preference_category_timetable_range)) {
											RangeInputPreference(
												title = { Text(stringResource(R.string.preference_timetable_range)) },
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
									}
								}

								composable("preferences_notifications") {
									title = stringResource(id = R.string.preferences_notifications)

									VerticalScrollColumn {
										SwitchPreference(
											title = { Text(stringResource(R.string.preference_notifications_enable)) },
											summary = { Text(stringResource(R.string.preference_notifications_enable_desc)) },
											/*icon = {
												Icon(
													painter = painterResource(R.drawable.settings_notifications_active),
													contentDescription = null
												)
											},*/
											onCheckedChange = {
												if (it)
													NotificationSetupWorker.enqueue(
														WorkManager.getInstance(this@SettingsActivity),
														user
													)
												else
													clearNotifications()
												it
											},
											dataStore = dataStorePreferences.notificationsEnable
										)

										SwitchPreference(
											title = { Text(stringResource(R.string.preference_notifications_multiple)) },
											summary = { Text(stringResource(R.string.preference_notifications_multiple_desc)) },
											dependency = dataStorePreferences.notificationsEnable,
											onCheckedChange = {
												NotificationSetupWorker.enqueue(
													WorkManager.getInstance(this@SettingsActivity),
													user
												)
												it
											},
											dataStore = dataStorePreferences.notificationsInMultiple
										)

										SwitchPreference(
											title = { Text(stringResource(R.string.preference_notifications_first_lesson)) },
											summary = { Text(stringResource(R.string.preference_notifications_first_lesson_desc)) },
											dependency = dataStorePreferences.notificationsEnable,
											onCheckedChange = {
												NotificationSetupWorker.enqueue(
													WorkManager.getInstance(this@SettingsActivity),
													user
												)
												it
											},
											dataStore = dataStorePreferences.notificationsBeforeFirst
										)

										NumericInputPreference(
											title = { Text(stringResource(R.string.preference_notifications_first_lesson_time)) },
											unit = stringResource(R.string.preference_notifications_first_lesson_time_unit),
											dependency = dataStorePreferences.notificationsBeforeFirst,
											onChange = {
												NotificationSetupWorker.enqueue(
													WorkManager.getInstance(this@SettingsActivity),
													user
												)
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
									}
								}

								composable("preferences_connectivity") {
									title = stringResource(id = R.string.preferences_connectivity)

									VerticalScrollColumn {
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
													startActivity(
														Intent(
															Intent.ACTION_VIEW,
															Uri.parse(URL_WIKI_PROXY)
														)
													)
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
									}
								}

								composable("preferences_info") {
									title = stringResource(id = R.string.preferences_info)

									VerticalScrollColumn {
										Preference(
											title = { Text(stringResource(R.string.app_name)) },
											summary = { Text(stringResource(R.string.app_desc)) },
											onClick = { /*TODO*/ },
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
												title = { Text(stringResource(R.string.preference_info_app_version)) },
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
													startActivity(
														Intent(
															Intent.ACTION_VIEW,
															Uri.parse("$URL_GITHUB_REPOSITORY/releases")
														)
													)
												},
												icon = {
													Icon(
														painter = painterResource(R.drawable.settings_info),
														contentDescription = null
													)
												},
												dataStore = UntisPreferenceDataStore.emptyDataStore()
											)

											Preference(
												title = { Text(stringResource(R.string.preference_info_github)) },
												summary = { Text(URL_GITHUB_REPOSITORY) },
												onClick = {
													startActivity(
														Intent(
															Intent.ACTION_VIEW,
															Uri.parse(URL_GITHUB_REPOSITORY)
														)
													)
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
													startActivity(
														Intent(
															Intent.ACTION_VIEW,
															Uri.parse("$URL_GITHUB_REPOSITORY/blob/master/LICENSE")
														)
													)
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
														  openDialog.value = true
												},
												icon = {
													Icon(
														painter = painterResource(R.drawable.settings_about_contributor),
														contentDescription = null
													)
												},
												dataStore = UntisPreferenceDataStore.emptyDataStore()
											)

											if(openDialog.value){
												AlertDialog(
													onDismissRequest = {
																	   openDialog.value = false
																	   },
													confirmButton = {
														Row(
															modifier = Modifier.padding(all = 8.dp),
															horizontalArrangement = Arrangement.SpaceAround
														) {
															TextButton(
																onClick = {
																	openDialog.value = false
																	startActivity(
																		Intent(
																			Intent.ACTION_VIEW, Uri.parse(
																				"https://docs.github.com/en/github/site-policy/github-privacy-statement"
																			)
																		)
																	)
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
												onClick = { navController.navigate("about_libs") },
												icon = {
													Icon(
														painter = painterResource(R.drawable.settings_about_library),
														contentDescription = null
													)
												},
												dataStore = UntisPreferenceDataStore.emptyDataStore()
											)
										}
									}
								}
								composable("about_libs"){
									title = stringResource(id = R.string.preference_info_libraries)

									val colors = libraryColors(
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
										Modifier
											.fillMaxSize()
											.padding(bottom = 48.dp),
										librariesBlock = { ctx ->
											Libs.Builder().withJson(ctx, R.raw.about_libs).build()
										},
										colors = colors
									)
								}
								composable("contributors"){
									title = stringResource(id = R.string.preference_info_contributors)

									var userList by remember { mutableStateOf(listOf<GithubUser>()) }
									val error = remember { mutableStateOf(true) }
									var string by remember { mutableStateOf(getString(R.string.loading)) }

									scope.launch {
										"https://api.github.com/repos/sapuseven/betteruntis/contributors"
											.httpGet()
											.awaitStringResult()
											.fold({ data ->
												userList = getJSON().decodeFromString<List<GithubUser>>(data)
												error.value = false
											}, {
												string = getString(R.string.loading_failed)
												error.value = true
											})
									}

									if (!error.value){
										LazyColumn(modifier = Modifier
											.fillMaxHeight()
											.padding(bottom = 48.dp)){
											this.items(userList){
												Contributor(githubUser = it)
											}
										}
									} else {
										Box(modifier = Modifier
											.fillMaxWidth()
											.height(68.dp)
											.padding(start = 16.dp), Alignment.CenterStart)
										{
											Row() {
												Icon(painter = painterResource(id = R.drawable.settings_about_contributor), contentDescription = "")
												Spacer(modifier = Modifier.width(8.dp))
												Text(text = string)
												
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	@OptIn(ExperimentalComposeUiApi::class)
	@Composable
	fun Contributor(
		githubUser: GithubUser
	) {
		Box(modifier = Modifier
			.fillMaxWidth()
			.clickable {
				startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(githubUser.html_url)))
			}
			.height(68.dp)
			.padding(start = 16.dp), Alignment.CenterStart)
		{
			Row() {
				AsyncImage(model = githubUser.avatar_url, contentDescription = "UserImage", //TODO: Extract string resource
					Modifier
						.height(48.dp)
						.width(48.dp))
				Spacer(modifier = Modifier.width(8.dp))
				Column() {
					Text(text = githubUser.login, fontWeight = FontWeight.Bold)
					Text(text = pluralStringResource(id = R.plurals.preferences_contributors_contributions, count = githubUser.contributions, githubUser.contributions))
				}
			}
		}
	}

	private fun updateAutoMutePref(
		user: UserDatabase.User,
		scope: CoroutineScope,
		autoMutePref: UntisPreferenceDataStore<Boolean>,
		enable: Boolean = false
	) {
		scope.launch {
			val permissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
				(getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).isNotificationPolicyAccessGranted
			} else true


			if (autoMutePref.getValue() && !permissionGranted)
				autoMutePref.saveValue(false)

			if (enable && permissionGranted) {
				autoMutePref.saveValue(true)
				AutoMuteSetupWorker.enqueue(
					WorkManager.getInstance(this@SettingsActivity),
					user
				)
			}
		}
	}

	private fun requestAutoMutePermission(activityLauncher: ActivityResultLauncher<Intent>): Boolean {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			(getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).apply {
				return if (!isNotificationPolicyAccessGranted) {
					activityLauncher.launch(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
					false
				} else true
			}
		} else return true
	}

	private fun clearNotifications() =
		(getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancelAll()

	@Composable
	private fun VerticalScrollColumn(content: @Composable ColumnScope.() -> Unit) {
		Column(
			modifier = Modifier
				.verticalScroll(rememberScrollState())
				.bottomInsets(),
			content = content
		)
	}

	private fun disableAutoMute() {
		sendBroadcast(
			Intent(applicationContext, AutoMuteReceiver::class.java)
				.putExtra(EXTRA_BOOLEAN_MUTE, false)
		)
	}
}
