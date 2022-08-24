package com.sapuseven.untis.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.*
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sapuseven.untis.BuildConfig
import com.sapuseven.untis.R
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.helpers.config.*
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.preferences.PreferenceCategory
import com.sapuseven.untis.preferences.PreferenceScreen
import com.sapuseven.untis.preferences.UntisPreferenceDataStore
import com.sapuseven.untis.preferences.preference.*
import com.sapuseven.untis.ui.theme.AppTheme

val Context.dataStore: DataStore<Preferences> by androidx.datastore.preferences.preferencesDataStore(
	name = "preferences"
)

class SettingsActivity : BaseComposeActivity() {
	companion object {
		const val EXTRA_LONG_PROFILE_ID = "com.sapuseven.untis.activities.profileid"
		const val EXTRA_STRING_PREFERENCE_ROUTE = "com.sapuseven.untis.activities.settings.route"
		const val EXTRA_STRING_PREFERENCE_HIGHLIGHT =
			"com.sapuseven.untis.activities.settings.highlight"

		private const val URL_GITHUB_REPOSITORY = "https://github.com/SapuSeven/BetterUntis"
		private const val URL_WIKI_PROXY = "$URL_GITHUB_REPOSITORY/wiki/Proxy"
	}

	@OptIn(ExperimentalMaterial3Api::class)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val profileId = (intent.extras?.getLong(EXTRA_LONG_PROFILE_ID)) ?: -1 // TODO
		val preferencePath =
			(intent.extras?.getString(EXTRA_STRING_PREFERENCE_ROUTE)) ?: "preferences"
		val preferenceHighlight = (intent.extras?.getString(EXTRA_STRING_PREFERENCE_HIGHLIGHT))

		val userDatabase = UserDatabase.createInstance(this)
		val timetableDatabaseInterface = TimetableDatabaseInterface(userDatabase, profileId)

		setContent {
			AppTheme {
				val navController = rememberNavController()
				var title by remember { mutableStateOf<String?>(null) }

				Scaffold(
					topBar = {
						CenterAlignedTopAppBar(
							title = {
								Text(
									title ?: stringResource(id = R.string.activity_title_settings)
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

								Column {
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

								Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
									PreferenceCategory(stringResource(id = R.string.preference_category_general_behaviour)) {
										SwitchPreference(
											title = { Text(stringResource(R.string.preference_double_tap_to_exit)) },
											dataStore = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = booleanPreferenceKey(
													profileId,
													"preference_double_tap_to_exit"
												),
												defaultValue = booleanResource(R.bool.preference_double_tap_to_exit_default)
											)
										)

										SwitchPreference(
											title = { Text(stringResource(R.string.preference_flinging_enable)) },
											dataStore = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = booleanPreferenceKey(
													profileId,
													"preference_fling_enable"
												),
												defaultValue = booleanResource(R.bool.preference_fling_enable_default)
											)
										)
									}

									PreferenceCategory(stringResource(R.string.preference_category_general_week_display)) {
										SwitchPreference(
											title = { Text(stringResource(R.string.preference_week_snap_to_days)) },
											summary = { Text(stringResource(R.string.preference_week_snap_to_days_summary)) },
											dataStore = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = booleanPreferenceKey(
													profileId,
													"preference_week_snap_to_days"
												),
												defaultValue = booleanResource(R.bool.preference_week_snap_to_days_default)
											)
										)

										WeekRangePickerPreference(
											title = { Text(stringResource(R.string.preference_week_custom_range)) },
											dataStore = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = stringSetPreferenceKey(
													profileId,
													"preference_week_custom_range"
												),
												defaultValue = emptySet()
											)
										)

										SliderPreference(
											valueRange = 0f..7f,
											steps = 6,
											title = { Text(stringResource(R.string.preference_week_display_length)) },
											summary = { Text(stringResource(R.string.preference_week_display_length_summary)) },
											showSeekBarValue = true,
											dataStore = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = floatPreferenceKey(
													profileId,
													"preference_week_custom_display_length"
												),
												defaultValue = 0f
											)
										)
									}


									PreferenceCategory(stringResource(id = R.string.preference_category_general_automute)) {
										SwitchPreference(
											title = { Text(stringResource(R.string.preference_automute_enable)) },
											summary = { Text(stringResource(R.string.preference_automute_enable_summary)) },
											dataStore = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = booleanPreferenceKey(
													profileId,
													"preference_automute_enable"
												),
												defaultValue = booleanResource(R.bool.preference_automute_enable_default)
											)
										)
										SwitchPreference(
											title = { Text(stringResource(R.string.preference_automute_cancelled_lessons)) },
											dependency = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = booleanPreferenceKey(
													profileId,
													"preference_automute_enable"
												),
												defaultValue = booleanResource(R.bool.preference_automute_enable_default)
											),
											dataStore = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = booleanPreferenceKey(
													profileId,
													"preference_automute_cancelled_lessons"
												),
												defaultValue = booleanResource(R.bool.preference_automute_cancelled_lessons_default)
											)
										)
										SwitchPreference(
											title = { Text(stringResource(R.string.preference_automute_mute_priority)) },
											dependency = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = booleanPreferenceKey(
													profileId,
													"preference_automute_enable"
												),
												defaultValue = booleanResource(R.bool.preference_automute_enable_default)
											),
											dataStore = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = booleanPreferenceKey(
													profileId,
													"preference_automute_mute_priority"
												),
												defaultValue = booleanResource(R.bool.preference_automute_mute_priority_default)
											)
										)

										SliderPreference(
											valueRange = 0f..20f,
											steps = 19,
											title = { Text(stringResource(R.string.preference_automute_minimum_break_length)) },
											summary = { Text(stringResource(R.string.preference_automute_minimum_break_length_summary)) },
											showSeekBarValue = true,
											dependency = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = booleanPreferenceKey(
													profileId,
													"preference_automute_enable"
												),
												defaultValue = booleanResource(R.bool.preference_automute_enable_default)
											),
											dataStore = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = floatPreferenceKey(
													profileId,
													"preference_automute_minimum_break_length"
												),
												defaultValue = integerResource(id = R.integer.preference_automute_minimum_break_length_default).toFloat()
											)
										)
									}

									// TODO: Extract string resources
									PreferenceCategory("Error Reporting") {
										SwitchPreference(
											title = { Text("Enable additional error messages") },
											summary = { Text("This is used for non-critical background errors") },
											dataStore = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = booleanPreferenceKey(
													profileId,
													"preference_additional_error_messages"
												),
												defaultValue = booleanResource(R.bool.preference_additional_error_messages_default)
											)
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

								Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
									PreferenceCategory(stringResource(id = R.string.preference_category_styling_colors)) {
										SwitchPreference(
											title = { Text(stringResource(R.string.preference_timetable_item_text_light)) },
											dataStore = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = booleanPreferenceKey(
													profileId,
													"preference_timetable_item_text_light"
												),
												defaultValue = booleanResource(R.bool.preference_timetable_item_text_light_default)
											)
										)

										ColorPreference(
											title = { Text(stringResource(R.string.preference_background_future)) },
											showAlphaSlider = true,
											dataStore = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = intPreferenceKey(
													profileId,
													"preference_background_future"
												),
												defaultValue = integerResource(R.integer.preference_background_future_default)
											)
										)

										ColorPreference(
											title = { Text(stringResource(R.string.preference_background_past)) },
											showAlphaSlider = true,
											dataStore = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = intPreferenceKey(
													profileId,
													"preference_background_past"
												),
												defaultValue = integerResource(R.integer.preference_background_past_default)
											)
										)

										ColorPreference(
											title = { Text(stringResource(R.string.preference_marker)) },
											dataStore = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = intPreferenceKey(
													profileId,
													"preference_marker"
												),
												defaultValue = integerResource(R.integer.preference_marker_default)
											)
										)
									}

									PreferenceCategory(stringResource(id = R.string.preference_category_styling_backgrounds)) {
										MultiSelectListPreference(
											title = { Text(stringResource(R.string.preference_school_background)) },
											summary = { Text(stringResource(R.string.preference_school_background_desc)) },
											entries = stringArrayResource(id = R.array.preference_schoolcolors_values),
											entryLabels = stringArrayResource(id = R.array.preference_schoolcolors),
											dataStore = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = stringSetPreferenceKey(
													profileId,
													"preference_school_background"
												),
												defaultValue = emptySet()
											)
										)

										SwitchPreference(
											title = { Text(stringResource(R.string.preference_use_theme_background)) },
											dependency = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = stringSetPreferenceKey(
													profileId,
													"preference_school_background"
												),
												defaultValue = emptySet(),
												dependencyValue = { !it.contains("regular") }
											),
											dataStore = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = booleanPreferenceKey(
													profileId,
													"preference_use_theme_background"
												),
												defaultValue = booleanResource(R.bool.preference_use_theme_background_default)
											)
										)

										ColorPreference(
											title = { Text(stringResource(R.string.preference_background_regular)) },
											dependency = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = stringSetPreferenceKey(
													profileId,
													"preference_school_background"
												),
												defaultValue = emptySet(),
												dependencyValue = { !it.contains("regular") },
												subDependency = UntisPreferenceDataStore(
													dataStore = dataStore,
													prefKey = booleanPreferenceKey(
														profileId,
														"preference_use_theme_background"
													),
													defaultValue = booleanResource(R.bool.preference_use_theme_background_default),
													dependencyValue = { !it }
												)
											),
											dataStore = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = intPreferenceKey(
													profileId,
													"preference_background_regular"
												),
												defaultValue = integerResource(R.integer.preference_background_regular_default)
											)
										)

										ColorPreference(
											title = { Text(stringResource(R.string.preference_background_regular_past)) },
											dependency = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = stringSetPreferenceKey(
													profileId,
													"preference_school_background"
												),
												defaultValue = emptySet(),
												dependencyValue = { !it.contains("regular") },
												subDependency = UntisPreferenceDataStore(
													dataStore = dataStore,
													prefKey = booleanPreferenceKey(
														profileId,
														"preference_use_theme_background"
													),
													defaultValue = booleanResource(R.bool.preference_use_theme_background_default),
													dependencyValue = { !it }
												)
											),
											dataStore = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = intPreferenceKey(
													profileId,
													"preference_background_regular_past"
												),
												defaultValue = integerResource(R.integer.preference_background_regular_past_default)
											)
										)

										ColorPreference(
											title = { Text(stringResource(R.string.preference_background_exam)) },
											dependency = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = stringSetPreferenceKey(
													profileId,
													"preference_school_background"
												),
												defaultValue = emptySet(),
												dependencyValue = { !it.contains("exam") }
											),
											dataStore = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = intPreferenceKey(
													profileId,
													"preference_background_exam"
												),
												defaultValue = integerResource(R.integer.preference_background_exam_default)
											)
										)

										ColorPreference(
											title = { Text(stringResource(R.string.preference_background_exam_past)) },
											dependency = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = stringSetPreferenceKey(
													profileId,
													"preference_school_background"
												),
												defaultValue = emptySet(),
												dependencyValue = { !it.contains("exam") }
											),
											dataStore = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = intPreferenceKey(
													profileId,
													"preference_background_exam_past"
												),
												defaultValue = integerResource(R.integer.preference_background_exam_past_default)
											)
										)

										ColorPreference(
											title = { Text(stringResource(R.string.preference_background_irregular)) },
											dependency = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = stringSetPreferenceKey(
													profileId,
													"preference_school_background"
												),
												defaultValue = emptySet(),
												dependencyValue = { !it.contains("irregular") }
											),
											dataStore = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = intPreferenceKey(
													profileId,
													"preference_background_irregular"
												),
												defaultValue = integerResource(R.integer.preference_background_irregular_default)
											)
										)

										ColorPreference(
											title = { Text(stringResource(R.string.preference_background_irregular_past)) },
											dependency = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = stringSetPreferenceKey(
													profileId,
													"preference_school_background"
												),
												defaultValue = emptySet(),
												dependencyValue = { !it.contains("irregular") }
											),
											dataStore = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = intPreferenceKey(
													profileId,
													"preference_background_irregular_past"
												),
												defaultValue = integerResource(R.integer.preference_background_irregular_past_default)
											)
										)

										ColorPreference(
											title = { Text(stringResource(R.string.preference_background_cancelled)) },
											dependency = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = stringSetPreferenceKey(
													profileId,
													"preference_school_background"
												),
												defaultValue = emptySet(),
												dependencyValue = { !it.contains("cancelled") }
											),
											dataStore = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = intPreferenceKey(
													profileId,
													"preference_background_cancelled"
												),
												defaultValue = integerResource(R.integer.preference_background_cancelled_default)
											)
										)

										ColorPreference(
											title = { Text(stringResource(R.string.preference_background_cancelled_past)) },
											dependency = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = stringSetPreferenceKey(
													profileId,
													"preference_school_background"
												),
												defaultValue = emptySet(),
												dependencyValue = { !it.contains("cancelled") }
											),
											dataStore = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = intPreferenceKey(
													profileId,
													"preference_background_cancelled_past"
												),
												defaultValue = integerResource(R.integer.preference_background_cancelled_past_default)
											)
										)

										Preference(
											title = { Text(stringResource(R.string.preference_timetable_colors_reset)) },
											onClick = { /*TODO*/ },
											dataStore = UntisPreferenceDataStore.emptyDataStore()
										)
									}

									PreferenceCategory(stringResource(id = R.string.preference_category_styling_themes)) {
										ListPreference(
											title = { Text(stringResource(R.string.preference_theme)) },
											summary = { Text(it.second) },
											icon = {
												Icon(
													painter = painterResource(R.drawable.settings_timetable_format_paint),
													contentDescription = null
												)
											},
											entries = stringArrayResource(id = R.array.preference_theme_values),
											entryLabels = stringArrayResource(id = R.array.preference_themes),
											dataStore = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = stringPreferenceKey(
													profileId,
													"preference_theme"
												),
												defaultValue = stringResource(R.string.preference_theme_default)
											)
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
											dataStore = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = stringPreferenceKey(
													profileId,
													"preference_dark_theme"
												),
												defaultValue = stringResource(R.string.preference_dark_theme_default)
											)
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
											dependency = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = stringPreferenceKey(
													profileId,
													"preference_dark_theme"
												),
												defaultValue = stringResource(R.string.preference_dark_theme_default),
												dependencyValue = { it != "off" }
											),
											dataStore = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = booleanPreferenceKey(
													profileId,
													"preference_dark_theme_oled"
												),
												defaultValue = booleanResource(R.bool.preference_dark_theme_oled_default)
											)
										)
									}
								}
							}

							composable("preferences_timetable") {
								title = stringResource(id = R.string.preferences_timetable)

								Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
									ElementPickerPreference(
										title = { Text(stringResource(R.string.preference_timetable_personal_timetable)) },
										dataStore = UntisPreferenceDataStore(
											dataStore = dataStore,
											prefKey = stringPreferenceKey(
												profileId,
												"preference_timetable_personal_timetable"
											),
											defaultValue = ""
										),
										timetableDatabaseInterface = timetableDatabaseInterface,
										highlight = preferenceHighlight == "preference_timetable_personal_timetable"
									)

									SwitchPreference(
										title = { Text(stringResource(R.string.preference_timetable_hide_time_stamps)) },
										summary = { Text(stringResource(R.string.preference_timetable_hide_time_stamps_desc)) },
										dataStore = UntisPreferenceDataStore(
											dataStore = dataStore,
											prefKey = booleanPreferenceKey(
												profileId,
												"preference_timetable_hide_time_stamps"
											),
											defaultValue = booleanResource(R.bool.preference_timetable_hide_time_stamps_default)
										)
									)

									SwitchPreference(
										title = { Text(stringResource(R.string.preference_timetable_hide_cancelled)) },
										summary = { Text(stringResource(R.string.preference_timetable_hide_cancelled_desc)) },
										dataStore = UntisPreferenceDataStore(
											dataStore = dataStore,
											prefKey = booleanPreferenceKey(
												profileId,
												"preference_timetable_hide_cancelled"
											),
											defaultValue = booleanResource(R.bool.preference_timetable_hide_cancelled_default)
										)
									)

									SwitchPreference(
										title = { Text(stringResource(R.string.preference_timetable_substitutions_irregular)) },
										summary = { Text(stringResource(R.string.preference_timetable_substitutions_irregular_desc)) },
										dataStore = UntisPreferenceDataStore(
											dataStore = dataStore,
											prefKey = booleanPreferenceKey(
												profileId,
												"preference_timetable_substitutions_irregular"
											),
											defaultValue = booleanResource(R.bool.preference_timetable_substitutions_irregular_default)
										)
									)

									SwitchPreference(
										title = { Text(stringResource(R.string.preference_timetable_background_irregular)) },
										summary = { Text(stringResource(R.string.preference_timetable_background_irregular_desc)) },
										dependency = UntisPreferenceDataStore(
											dataStore = dataStore,
											prefKey = booleanPreferenceKey(
												profileId,
												"preference_timetable_substitutions_irregular"
											),
											defaultValue = booleanResource(R.bool.preference_timetable_substitutions_irregular_default)
										),
										dataStore = UntisPreferenceDataStore(
											dataStore = dataStore,
											prefKey = booleanPreferenceKey(
												profileId,
												"preference_timetable_background_irregular"
											),
											defaultValue = booleanResource(R.bool.preference_timetable_background_irregular_default)
										)
									)

									PreferenceCategory(stringResource(id = R.string.preference_category_timetable_range)) {
										RangeInputPreference(
											title = { Text(stringResource(R.string.preference_timetable_range)) },
											dataStore = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = stringPreferenceKey(
													profileId,
													"preference_timetable_range"
												),
												defaultValue = ""
											)
										)

										SwitchPreference(
											title = { Text(stringResource(R.string.preference_timetable_range_index_reset)) },
											dependency = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = stringPreferenceKey(
													profileId,
													"preference_timetable_range"
												),
												defaultValue = ""
											),
											dataStore = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = booleanPreferenceKey(
													profileId,
													"preference_timetable_range_index_reset"
												),
												defaultValue = booleanResource(R.bool.preference_timetable_range_index_reset_default)
											)
										)

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
											dataStore = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = intPreferenceKey(
													profileId,
													"preference_timetable_item_padding_overlap"
												),
												defaultValue = integerResource(R.integer.preference_timetable_item_padding_overlap_default)
											)
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
											dataStore = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = intPreferenceKey(
													profileId,
													"preference_timetable_item_padding"
												),
												defaultValue = integerResource(R.integer.preference_timetable_item_padding_default)
											)
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
											dataStore = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = intPreferenceKey(
													profileId,
													"preference_timetable_item_corner_radius"
												),
												defaultValue = integerResource(R.integer.preference_timetable_item_corner_radius_default)
											)
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
											dataStore = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = booleanPreferenceKey(
													profileId,
													"preference_timetable_centered_lesson_info"
												),
												defaultValue = booleanResource(R.bool.preference_timetable_centered_lesson_info_default)
											)
										)

										SwitchPreference(
											title = { Text(stringResource(R.string.preference_timetable_bold_lesson_name)) },
											icon = {
												Icon(
													painter = painterResource(R.drawable.settings_timetable_format_bold),
													contentDescription = null
												)
											},
											dataStore = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = booleanPreferenceKey(
													profileId,
													"preference_timetable_bold_lesson_name"
												),
												defaultValue = booleanResource(R.bool.preference_timetable_bold_lesson_name_default)
											)
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
											dataStore = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = intPreferenceKey(
													profileId,
													"preference_timetable_lesson_name_font_size"
												),
												defaultValue = integerResource(R.integer.preference_timetable_lesson_name_font_size_default)
											)
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
											dataStore = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = intPreferenceKey(
													profileId,
													"preference_timetable_lesson_info_font_size"
												),
												defaultValue = integerResource(R.integer.preference_timetable_lesson_info_font_size_default)
											)
										)
									}
								}
							}

							composable("preferences_notifications") {
								title = stringResource(id = R.string.preferences_notifications)

								Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
									SwitchPreference(
										title = { Text(stringResource(R.string.preference_notifications_enable)) },
										summary = { Text(stringResource(R.string.preference_notifications_enable_desc)) },
										/*icon = {
											Icon(
												painter = painterResource(R.drawable.settings_notifications_active),
												contentDescription = null
											)
										},*/
										dataStore = UntisPreferenceDataStore(
											dataStore = dataStore,
											prefKey = booleanPreferenceKey(
												profileId,
												"preference_notifications_enable"
											),
											defaultValue = booleanResource(R.bool.preference_notifications_enable_default)
										)
									)

									SwitchPreference(
										title = { Text(stringResource(R.string.preference_notifications_multiple)) },
										summary = { Text(stringResource(R.string.preference_notifications_multiple_desc)) },
										dependency = UntisPreferenceDataStore(
											dataStore = dataStore,
											prefKey = booleanPreferenceKey(
												profileId,
												"preference_notifications_enable"
											),
											defaultValue = booleanResource(R.bool.preference_notifications_enable_default)
										),
										dataStore = UntisPreferenceDataStore(
											dataStore = dataStore,
											prefKey = booleanPreferenceKey(
												profileId,
												"preference_notifications_in_multiple"
											),
											defaultValue = booleanResource(R.bool.preference_notifications_in_multiple_default)
										)
									)

									SwitchPreference(
										title = { Text(stringResource(R.string.preference_notifications_first_lesson)) },
										summary = { Text(stringResource(R.string.preference_notifications_first_lesson_desc)) },
										dependency = UntisPreferenceDataStore(
											dataStore = dataStore,
											prefKey = booleanPreferenceKey(
												profileId,
												"preference_notifications_enable"
											),
											defaultValue = booleanResource(R.bool.preference_notifications_enable_default)
										),
										dataStore = UntisPreferenceDataStore(
											dataStore = dataStore,
											prefKey = booleanPreferenceKey(
												profileId,
												"preference_notifications_before_first"
											),
											defaultValue = booleanResource(R.bool.preference_notifications_before_first_default)
										)
									)

									NumericInputPreference(
										title = { Text(stringResource(R.string.preference_notifications_first_lesson_time)) },
										unit = stringResource(R.string.preference_notifications_first_lesson_time_unit),
										dependency = UntisPreferenceDataStore(
											dataStore = dataStore,
											prefKey = booleanPreferenceKey(
												profileId,
												"preference_notifications_before_first"
											),
											defaultValue = booleanResource(R.bool.preference_notifications_before_first_default)
										),
										dataStore = UntisPreferenceDataStore(
											dataStore = dataStore,
											prefKey = intPreferenceKey(
												profileId,
												"preference_notifications_before_first_time"
											),
											defaultValue = integerResource(R.integer.preference_notifications_before_first_time_default)
										)
									)

									Preference(
										title = { Text(stringResource(R.string.preference_notifications_clear)) },
										onClick = { /*TODO*/ },
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
											dependency = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = booleanPreferenceKey(
													profileId,
													"preference_notifications_enable"
												),
												defaultValue = booleanResource(R.bool.preference_notifications_enable_default)
											),
											dataStore = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = stringPreferenceKey(
													profileId,
													"preference_notifications_visibility_subjects"
												),
												defaultValue = stringResource(R.string.preference_notifications_visibility_subjects_default)
											)
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
											dependency = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = booleanPreferenceKey(
													profileId,
													"preference_notifications_enable"
												),
												defaultValue = booleanResource(R.bool.preference_notifications_enable_default)
											),
											dataStore = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = stringPreferenceKey(
													profileId,
													"preference_notifications_visibility_rooms"
												),
												defaultValue = stringResource(R.string.preference_notifications_visibility_rooms_default)
											)
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
											dependency = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = booleanPreferenceKey(
													profileId,
													"preference_notifications_enable"
												),
												defaultValue = booleanResource(R.bool.preference_notifications_enable_default)
											),
											dataStore = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = stringPreferenceKey(
													profileId,
													"preference_notifications_visibility_teachers"
												),
												defaultValue = stringResource(R.string.preference_notifications_visibility_teachers_default)
											)
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
											dependency = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = booleanPreferenceKey(
													profileId,
													"preference_notifications_enable"
												),
												defaultValue = booleanResource(R.bool.preference_notifications_enable_default)
											),
											dataStore = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = stringPreferenceKey(
													profileId,
													"preference_notifications_visibility_classes"
												),
												defaultValue = stringResource(R.string.preference_notifications_visibility_classes_default)
											)
										)
									}
								}
							}

							composable("preferences_connectivity") {
								title = stringResource(id = R.string.preferences_connectivity)

								Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
									SwitchPreference(
										title = { Text(stringResource(R.string.preference_connectivity_refresh_in_background)) },
										summary = { Text(stringResource(R.string.preference_connectivity_refresh_in_background_desc)) },
										dataStore = UntisPreferenceDataStore(
											dataStore = dataStore,
											prefKey = booleanPreferenceKey(
												profileId,
												"preference_connectivity_refresh_in_background"
											),
											defaultValue = booleanResource(R.bool.preference_connectivity_refresh_in_background_default)
										)
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
											dataStore = UntisPreferenceDataStore(
												dataStore = dataStore,
												prefKey = stringPreferenceKey(
													profileId,
													"preference_connectivity_proxy_host"
												),
												defaultValue = ""
											)
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

								Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
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
											onClick = { /*TODO*/ },
											icon = {
												Icon(
													painter = painterResource(R.drawable.settings_about_contributor),
													contentDescription = null
												)
											},
											dataStore = UntisPreferenceDataStore.emptyDataStore()
										)

										Preference(
											title = { Text(stringResource(R.string.preference_info_libraries)) },
											summary = { Text(stringResource(R.string.preference_info_libraries_desc)) },
											onClick = { /*TODO*/ },
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
						}
					}
				}
			}
		}
	}

	/*class PreferencesFragment : PreferenceFragmentCompat() {
		companion object {
			const val FRAGMENT_TAG = "preference_fragment"
			const val DIALOG_FRAGMENT_TAG = "preference_dialog_fragment"
		}

		private var profileId: Long = 0

		override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
			profileId = arguments?.getLong(EXTRA_LONG_PROFILE_ID) ?: 0
			if (profileId == 0L) {
				MaterialAlertDialogBuilder(requireContext())
					.setMessage("Invalid profile ID")
					.setPositiveButton("Exit") { _, _ ->
						activity?.finish()
					}
					.show()
			} else {
				preferenceManager.sharedPreferencesName = "preferences_$profileId"

				setPreferencesFromResource(R.xml.preferences, rootKey)

				when (rootKey) {
					"preferences_general" -> {
						findPreference<SeekBarPreference>("preference_week_custom_display_length")?.apply {
							max =
								findPreference<WeekRangePickerPreference>("preference_week_custom_range")?.getPersistedStringSet(
									emptySet()
								)?.size?.zeroToNull
									?: this.max
						}

						findPreference<com.sapuseven.untis.preferences.SwitchPreference>("preference_automute_enable")?.setOnPreferenceChangeListener { _, newValue ->
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && newValue == true) {
								(activity?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).apply {
									if (!isNotificationPolicyAccessGranted) {
										startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
										return@setOnPreferenceChangeListener false
									}
								}
							}
							true
						}

						findPreference<Preference>("preference_errors")?.setOnPreferenceClickListener {
							startActivity(Intent(context, ErrorsActivity::class.java))
							true
						}
					}
					"preferences_styling" -> {
						findPreference<MultiSelectListPreference>("preference_school_background")?.apply {
							setOnPreferenceChangeListener { _, newValue ->
								if (newValue !is Set<*>) return@setOnPreferenceChangeListener false

								refreshColorPreferences(newValue)

								true
							}

							refreshColorPreferences(values)
						}

						listOf(
							"preference_theme",
							"preference_dark_theme",
							"preference_dark_theme_oled"
						).forEach { key ->
							findPreference<Preference>(key)?.setOnPreferenceChangeListener { _, _ ->
								activity?.recreate()
								true
							}
						}

						findPreference<Preference>("preference_timetable_colors_reset")?.setOnPreferenceClickListener {
							MaterialAlertDialogBuilder(requireContext())
								.setTitle(R.string.preference_dialog_colors_reset_title)
								.setMessage(R.string.preference_dialog_colors_reset_text)
								.setPositiveButton(R.string.preference_timetable_colors_reset_button_positive) { _, _ ->
									preferenceManager.sharedPreferences?.edit()?.apply {
										listOf(
											"preference_background_regular",
											"preference_background_regular_past",
											"preference_background_exam",
											"preference_background_exam_past",
											"preference_background_irregular",
											"preference_background_irregular_past",
											"preference_background_cancelled",
											"preference_background_cancelled_past"
										).forEach {
											remove(it)
										}
										apply()
									}
									activity?.recreate()
								}
								.setNegativeButton(R.string.all_cancel) { _, _ -> }
								.show()
							true
						}
					}
					"preferences_connectivity" ->
						findPreference<Preference>("preference_connectivity_proxy_about")?.setOnPreferenceClickListener {
							startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(WIKI_URL_PROXY)))
							true
						}

					"preferences_notifications" -> {
						findPreference<Preference>("preference_notifications_enable")?.setOnPreferenceChangeListener { _, newValue ->
							if (newValue == false) clearNotifications()
							true
						}
						findPreference<Preference>("preference_notifications_clear")?.setOnPreferenceClickListener {
							clearNotifications()
							true
						}
					}
					"preferences_info" -> {
						findPreference<Preference>("preference_info_app_version")?.apply {
							val pInfo =
								requireContext().packageManager.getPackageInfo(
									requireContext().packageName,
									0
								)
							summary = requireContext().getString(
								R.string.preference_info_app_version_desc,
								pInfo.versionName,
								PackageInfoCompat.getLongVersionCode(pInfo)
							)
							setOnPreferenceClickListener {
								startActivity(
									Intent(
										Intent.ACTION_VIEW,
										Uri.parse("$REPOSITORY_URL_GITHUB/releases")
									)
								)
								true
							}
						}
						findPreference<Preference>("preference_info_github")?.apply {
							summary = REPOSITORY_URL_GITHUB
							setOnPreferenceClickListener {
								startActivity(
									Intent(
										Intent.ACTION_VIEW,
										Uri.parse(REPOSITORY_URL_GITHUB)
									)
								)
								true
							}
						}
						findPreference<Preference>("preference_info_license")?.setOnPreferenceClickListener {
							startActivity(
								Intent(
									Intent.ACTION_VIEW,
									Uri.parse("$REPOSITORY_URL_GITHUB/blob/master/LICENSE")
								)
							)
							true
						}
					}
					"preferences_contributors" -> {
						MaterialAlertDialogBuilder(requireContext())
							.setTitle(R.string.preference_info_privacy)
							.setMessage(R.string.preference_info_privacy_desc)
							.setPositiveButton(android.R.string.ok) { _, _ ->
								GlobalScope.launch(Dispatchers.Main) {
									"https://api.github.com/repos/sapuseven/betteruntis/contributors"
										.httpGet()
										.awaitStringResult()
										.fold({ data ->
											showContributorList(true, data)
										}, {
											showContributorList(false)
										})
								}
							}
							.setNegativeButton(android.R.string.cancel) { _, _ ->
								parentFragmentManager.popBackStackImmediate()
							}
							.setNeutralButton(R.string.preference_info_privacy_policy) { _, _ ->
								parentFragmentManager.popBackStackImmediate()
								startActivity(
									Intent(
										Intent.ACTION_VIEW, Uri.parse(
											"https://docs.github.com/en/github/site-policy/github-privacy-statement"
										)
									)
								)
							}
							.show()
					}
				}
			}
		}

		private suspend fun showContributorList(success: Boolean, data: String = "") {
			val preferenceScreen = this.preferenceScreen
			val indicator = findPreference<Preference>("preferences_contributors_indicator")
			if (success) {
				val contributors = getJSON().decodeFromString<List<GithubUser>>(data)

				indicator?.let { preferenceScreen.removePreference(it) }

				contributors.forEach { user ->
					context?.let {
						preferenceScreen.addPreference(
							Preference(it).apply {
								GlobalScope.launch(Dispatchers.Main) {
									icon = loadProfileImage(user.avatar_url, resources)
								}
								title = user.login
								summary = resources.getQuantityString(
									R.plurals.preferences_contributors_contributions,
									user.contributions,
									user.contributions
								)
								setOnPreferenceClickListener {
									startActivity(
										Intent(
											Intent.ACTION_VIEW,
											Uri.parse(user.html_url)
										)
									)
									true
								}
							}
						)
					}
				}
			} else {
				indicator?.title = resources.getString(R.string.loading_failed)
			}
		}

		private suspend fun loadProfileImage(avatarUrl: String, resources: Resources): Drawable? {
			return avatarUrl
				.httpGet()
				.awaitByteArrayResult()
				.fold({
					BitmapDrawable(resources, BitmapFactory.decodeByteArray(it, 0, it.size))
				}, { null })
		}

		private fun clearNotifications() =
			(context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancelAll()

		private fun refreshColorPreferences(newValue: Set<*>) {
			val regularColors = listOf(
				"preference_background_regular",
				"preference_background_regular_past",
				"preference_use_theme_background"
			)
			val irregularColors =
				listOf("preference_background_irregular", "preference_background_irregular_past")
			val cancelledColors =
				listOf("preference_background_cancelled", "preference_background_cancelled_past")
			val examColors = listOf("preference_background_exam", "preference_background_exam_past")

			regularColors.forEach {
				findPreference<Preference>(it)?.isEnabled = !newValue.contains("regular")
			}
			irregularColors.forEach {
				findPreference<Preference>(it)?.isEnabled = !newValue.contains("irregular")
			}
			cancelledColors.forEach {
				findPreference<Preference>(it)?.isEnabled = !newValue.contains("cancelled")
			}
			examColors.forEach {
				findPreference<Preference>(it)?.isEnabled = !newValue.contains("exam")
			}
		}

		override fun onPreferenceTreeClick(preference: Preference): Boolean {
			if (preference is ElementPickerPreference) {
				val userDatabase = UserDatabase.createInstance(requireContext())
				val timetableDatabaseInterface = TimetableDatabaseInterface(userDatabase, profileId)

				ElementPickerDialog.newInstance(
					timetableDatabaseInterface,
					ElementPickerDialog.Companion.ElementPickerDialogConfig(
						TimetableDatabaseInterface.Type.valueOf(preference.getSavedType())
					),
					object : ElementPickerDialog.ElementPickerDialogListener {
						override fun onDialogDismissed(dialog: DialogInterface?) {
							// ignore
						}

						override fun onPeriodElementClick(
							fragment: Fragment,
							element: PeriodElement?,
							useOrgId: Boolean
						) {
							preference.setElement(
								element,
								element?.let {
									timetableDatabaseInterface.getShortName(
										it.id,
										TimetableDatabaseInterface.Type.valueOf(it.type)
									)
								} ?: "")
							(fragment as DialogFragment).dismiss()
						}

						override fun onPositiveButtonClicked(dialog: ElementPickerDialog) {
							// positive button not used
						}
					}
				).show(requireFragmentManager(), "elementPicker")
			}

			return true
		}

		override fun onDisplayPreferenceDialog(preference: Preference) {
			fragmentManager?.let { manager ->
				if (manager.findFragmentByTag(DIALOG_FRAGMENT_TAG) != null) return

				when (preference) {
					is AlertPreference -> {
						val f: DialogFragment = AlertPreferenceDialog.newInstance(preference.key)
						f.setTargetFragment(this, 0)
						f.show(manager, DIALOG_FRAGMENT_TAG)
					}
					is WeekRangePickerPreference -> {
						val f: DialogFragment =
							WeekRangePickerPreferenceDialog.newInstance(preference.key) { positiveResult, selectedDays ->
								val visibleDaysPreference =
									findPreference<SeekBarPreference>("preference_week_custom_display_length")
								if (positiveResult) {
									visibleDaysPreference?.max = selectedDays.zeroToNull ?: 7
									visibleDaysPreference?.value = min(
										visibleDaysPreference?.value
											?: 0, selectedDays.zeroToNull ?: 7
									)
								}
							}
						f.setTargetFragment(this, 0)
						f.show(manager, DIALOG_FRAGMENT_TAG)
					}
					else -> super.onDisplayPreferenceDialog(preference)
				}
			}
		}
	}

	@Composable
	fun FragmentContainer(
		modifier: Modifier = Modifier,
		fragmentManager: FragmentManager,
		commit: FragmentTransaction.(containerId: Int) -> Unit
	) {
		val containerId by rememberSaveable { mutableStateOf(View.generateViewId()) }
		var initialized by rememberSaveable { mutableStateOf(false) }
		AndroidView(
			modifier = modifier,
			factory = { context ->
				FragmentContainerView(context)
					.apply { id = containerId }
			},
			update = { view ->
				if (!initialized) {
					fragmentManager.commit { commit(view.id) }
					initialized = true
				} else {
					fragmentManager.onContainerAvailable(view)
				}
			}
		)
	}

	// Access to package-private method in FragmentManager through reflection
	private fun FragmentManager.onContainerAvailable(view: FragmentContainerView) {
		val method = FragmentManager::class.java.getDeclaredMethod(
			"onContainerAvailable",
			FragmentContainerView::class.java
		)
		method.isAccessible = true
		method.invoke(this, view)
	}*/
}
