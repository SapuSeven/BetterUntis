package com.sapuseven.untis.preferences

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import com.sapuseven.untis.activities.BaseComposeActivity
import com.sapuseven.untis.helpers.config.*
import com.sapuseven.untis.ui.preferences.materialColors

val BaseComposeActivity.dataStorePreferences: DataStorePreferences
	@Composable
	get() = DataStorePreferences(
		doubleTapToExit = this.booleanDataStore("preference_double_tap_to_exit"),
		flingEnable = this.booleanDataStore("preference_fling_enable"),
		weekSnapToDays = this.booleanDataStore("preference_week_snap_to_days"),
		weekCustomRange = this.stringSetDataStore(
			"preference_week_custom_range",
			defaultValue = emptySet()
		),
		weekCustomLength = this.floatDataStore(
			"preference_week_custom_display_length",
			defaultValue = 0f
		),
		automuteEnable = this.booleanDataStore("preference_automute_enable"),
		automuteCancelledLessons = this.booleanDataStore("preference_automute_cancelled_lessons"),
		automuteMutePriority = this.booleanDataStore("preference_automute_mute_priority"),
		automuteMinimumBreakLength = this.floatDataStore("preference_automute_minimum_break_length"),
		additionalErrorMessages = this.booleanDataStore("preference_additional_error_messages"),
		timetableItemTextLight = this.booleanDataStore("preference_timetable_item_text_light"),
		backgroundFuture = this.intDataStore("preference_background_future"),
		backgroundPast = this.intDataStore("preference_background_past"),
		marker = this.intDataStore("preference_marker"),
		backgroundRegular = this.intDataStore(
			"preference_background_regular",
			defaultValue = MaterialTheme.colorScheme.primary.toArgb()
		),
		backgroundRegularPast = this.intDataStore(
			"preference_background_regular_past",
			defaultValue = MaterialTheme.colorScheme.primary.copy(alpha = .7f).toArgb()
		),
		backgroundExam = this.intDataStore(
			"preference_background_exam",
			defaultValue = MaterialTheme.colorScheme.error.toArgb()
		),
		backgroundExamPast = this.intDataStore(
			"preference_background_exam_past",
			defaultValue = MaterialTheme.colorScheme.error.copy(alpha = .7f).toArgb()
		),
		backgroundIrregular = this.intDataStore(
			"preference_background_irregular",
			defaultValue = MaterialTheme.colorScheme.tertiary.toArgb()
		),
		backgroundIrregularPast = this.intDataStore(
			"preference_background_irregular_past",
			defaultValue = MaterialTheme.colorScheme.tertiary.copy(alpha = .7f).toArgb()
		),
		backgroundCancelled = this.intDataStore(
			"preference_background_cancelled",
			defaultValue = MaterialTheme.colorScheme.secondary.toArgb()
		),
		backgroundCancelledPast = this.intDataStore(
			"preference_background_cancelled_past",
			defaultValue = MaterialTheme.colorScheme.secondary.copy(alpha = .7f).toArgb()
		),
		themeColor = this.intDataStore(
			"preference_theme_color",
			defaultValue = MaterialTheme.colorScheme.primary.toArgb()
		),
		darkTheme = this.stringDataStore(
			"preference_dark_theme",
			dependencyValue = { it != "off" }),
		darkThemeOled = this.booleanDataStore("preference_dark_theme_oled"),
		timetablePersonalTimetable = this.stringDataStore(
			"preference_timetable_personal_timetable",
			defaultValue = ""
		),
		timetableHideTimeStamps = this.booleanDataStore("preference_timetable_hide_time_stamps"),
		timetableHideCancelled = this.booleanDataStore("preference_timetable_hide_cancelled"),
		timetableSubstitutionsIrregular = this.booleanDataStore("preference_timetable_substitutions_irregular"),
		timetableBackgroundIrregular = this.booleanDataStore("preference_timetable_background_irregular"),
		timetableRange = this.stringDataStore("preference_timetable_range", defaultValue = ""),
		timetableRangeIndexReset = this.booleanDataStore("preference_timetable_range_index_reset"),
		timetableItemPaddingOverlap = this.intDataStore("preference_timetable_item_padding_overlap"),
		timetableItemPadding = this.intDataStore("preference_timetable_item_padding"),
		timetableItemCornerRadius = this.intDataStore("preference_timetable_item_corner_radius"),
		timetableCenteredLessonInfo = this.booleanDataStore("preference_timetable_centered_lesson_info"),
		timetableBoldLessonName = this.booleanDataStore("preference_timetable_bold_lesson_name"),
		timetableLessonNameFontSize = this.intDataStore("preference_timetable_lesson_name_font_size"),
		timetableLessonInfoFontSize = this.intDataStore("preference_timetable_lesson_info_font_size"),
		notificationsEnable = this.booleanDataStore("preference_notifications_enable"),
		notificationsInMultiple = this.booleanDataStore("preference_notifications_in_multiple"),
		notificationsBeforeFirst = this.booleanDataStore("preference_notifications_before_first"),
		notificationsBeforeFirstTime = this.intDataStore("preference_notifications_before_first_time"),
		notificationsVisibilitySubjects = this.stringDataStore("preference_notifications_visibility_subjects"),
		notificationsVisibilityRooms = this.stringDataStore("preference_notifications_visibility_rooms"),
		notificationsVisibilityTeachers = this.stringDataStore("preference_notifications_visibility_teachers"),
		notificationsVisibilityClasses = this.stringDataStore("preference_notifications_visibility_classes"),
		connectivityRefreshInBackground = this.booleanDataStore("preference_connectivity_refresh_in_background"),
		proxyHost = this.stringDataStore("preference_connectivity_proxy_host", defaultValue = ""),
		schoolBackground = this.stringSetDataStore(
			"preference_school_background",
			defaultValue = emptySet()
		),
	)

class DataStorePreferences(
	val doubleTapToExit: UntisPreferenceDataStore<Boolean>,
	val flingEnable: UntisPreferenceDataStore<Boolean>,
	val weekSnapToDays: UntisPreferenceDataStore<Boolean>,
	val weekCustomRange: UntisPreferenceDataStore<Set<String>>,
	val weekCustomLength: UntisPreferenceDataStore<Float>,
	val automuteEnable: UntisPreferenceDataStore<Boolean>,
	val automuteCancelledLessons: UntisPreferenceDataStore<Boolean>,
	val automuteMutePriority: UntisPreferenceDataStore<Boolean>,
	val automuteMinimumBreakLength: UntisPreferenceDataStore<Float>,
	val additionalErrorMessages: UntisPreferenceDataStore<Boolean>,
	val timetableItemTextLight: UntisPreferenceDataStore<Boolean>,
	val backgroundFuture: UntisPreferenceDataStore<Int>,
	val backgroundPast: UntisPreferenceDataStore<Int>,
	val marker: UntisPreferenceDataStore<Int>,
	val backgroundRegular: UntisPreferenceDataStore<Int>,
	val backgroundRegularPast: UntisPreferenceDataStore<Int>,
	val backgroundExam: UntisPreferenceDataStore<Int>,
	val backgroundExamPast: UntisPreferenceDataStore<Int>,
	val backgroundIrregular: UntisPreferenceDataStore<Int>,
	val backgroundIrregularPast: UntisPreferenceDataStore<Int>,
	val backgroundCancelled: UntisPreferenceDataStore<Int>,
	val backgroundCancelledPast: UntisPreferenceDataStore<Int>,
	val themeColor: UntisPreferenceDataStore<Int>,
	val darkTheme: UntisPreferenceDataStore<String>,
	val darkThemeOled: UntisPreferenceDataStore<Boolean>,
	val timetablePersonalTimetable: UntisPreferenceDataStore<String>,
	val timetableHideTimeStamps: UntisPreferenceDataStore<Boolean>,
	val timetableHideCancelled: UntisPreferenceDataStore<Boolean>,
	val timetableSubstitutionsIrregular: UntisPreferenceDataStore<Boolean>,
	val timetableBackgroundIrregular: UntisPreferenceDataStore<Boolean>,
	val timetableRange: UntisPreferenceDataStore<String>,
	val timetableRangeIndexReset: UntisPreferenceDataStore<Boolean>,
	val timetableItemPaddingOverlap: UntisPreferenceDataStore<Int>,
	val timetableItemPadding: UntisPreferenceDataStore<Int>,
	val timetableItemCornerRadius: UntisPreferenceDataStore<Int>,
	val timetableCenteredLessonInfo: UntisPreferenceDataStore<Boolean>,
	val timetableBoldLessonName: UntisPreferenceDataStore<Boolean>,
	val timetableLessonNameFontSize: UntisPreferenceDataStore<Int>,
	val timetableLessonInfoFontSize: UntisPreferenceDataStore<Int>,
	val notificationsEnable: UntisPreferenceDataStore<Boolean>,
	val notificationsInMultiple: UntisPreferenceDataStore<Boolean>,
	val notificationsBeforeFirst: UntisPreferenceDataStore<Boolean>,
	val notificationsBeforeFirstTime: UntisPreferenceDataStore<Int>,
	val notificationsVisibilitySubjects: UntisPreferenceDataStore<String>,
	val notificationsVisibilityRooms: UntisPreferenceDataStore<String>,
	val notificationsVisibilityTeachers: UntisPreferenceDataStore<String>,
	val notificationsVisibilityClasses: UntisPreferenceDataStore<String>,
	val connectivityRefreshInBackground: UntisPreferenceDataStore<Boolean>,
	val proxyHost: UntisPreferenceDataStore<String>,
	val schoolBackground: UntisPreferenceDataStore<Set<String>>,
)
