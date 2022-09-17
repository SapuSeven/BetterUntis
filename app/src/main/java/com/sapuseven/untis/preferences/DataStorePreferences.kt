package com.sapuseven.untis.preferences

import androidx.compose.runtime.Composable
import com.sapuseven.untis.activities.BaseComposeActivity
import com.sapuseven.untis.helpers.config.*

val BaseComposeActivity.dataStorePreferences: DataStorePreferences
	get() = DataStorePreferences(this)

class DataStorePreferences(val context: BaseComposeActivity) {
	@Composable
	fun doubleTapToExit() = context.booleanDataStore(
		"preference_double_tap_to_exit"
	)

	@Composable
	fun flingEnable() = context.booleanDataStore(
		"preference_fling_enable"
	)

	@Composable
	fun weekSnapToDays() = context.booleanDataStore(
		"preference_week_snap_to_days"
	)

	@Composable
	fun weekCustomRange() = context.stringSetDataStore(
		"preference_week_custom_range",
		defaultValue = emptySet()
	)

	@Composable
	fun weekCustomLength() = context.floatDataStore(
		"preference_week_custom_display_length",
		defaultValue = 0f
	)

	@Composable
	fun automuteEnable() = context.booleanDataStore(
		"preference_automute_enable"
	)

	@Composable
	fun automuteCancelledLessons() = context.booleanDataStore(
		"preference_automute_cancelled_lessons"
	)

	@Composable
	fun automuteMutePriority() = context.booleanDataStore(
		"preference_automute_mute_priority"
	)

	@Composable
	fun automuteMinimumBreakLength() = context.floatDataStore(
		"preference_automute_minimum_break_length"
	)

	@Composable
	fun additionalErrorMessages() = context.booleanDataStore(
		"preference_additional_error_messages"
	)

	@Composable
	fun timetableItemTextLight() = context.booleanDataStore(
		"preference_timetable_item_text_light"
	)

	@Composable
	fun backgroundFuture() = context.intDataStore(
		"preference_background_future"
	)

	@Composable
	fun backgroundPast() = context.intDataStore(
		"preference_background_past"
	)

	@Composable
	fun marker() = context.intDataStore(
		"preference_marker"
	)

	@Composable
	fun schoolBackground(
		dependencyValue: (prefValue: Set<String>) -> Boolean = { it.isNotEmpty() },
		subDependency: UntisPreferenceDataStore<*>? = null
	) = context.stringSetDataStore(
		"preference_school_background",
		dependencyValue = dependencyValue,
		subDependency = subDependency,
		defaultValue = emptySet()
	)

	@Composable
	fun useThemeBackground(
		dependencyValue: (prefValue: Boolean) -> Boolean = { it }
	) = context.booleanDataStore(
		"preference_use_theme_background",
		dependencyValue = dependencyValue
	)

	@Composable
	fun backgroundRegular() = context.intDataStore(
		"preference_background_regular"
	)

	@Composable
	fun backgroundRegularPast() = context.intDataStore(
		"preference_background_regular_past"
	)

	@Composable
	fun backgroundExam() = context.intDataStore(
		"preference_background_exam"
	)

	@Composable
	fun backgroundExamPast() = context.intDataStore(
		"preference_background_exam_past"
	)

	@Composable
	fun backgroundIrregular() = context.intDataStore(
		"preference_background_irregular"
	)

	@Composable
	fun backgroundIrregularPast() = context.intDataStore(
		"preference_background_irregular_past"
	)

	@Composable
	fun backgroundCancelled() = context.intDataStore(
		"preference_background_cancelled"
	)

	@Composable
	fun backgroundCancelledPast() = context.intDataStore(
		"preference_background_cancelled_past"
	)

	@Composable
	fun theme() = context.stringDataStore(
		"preference_theme"
	)

	@Composable
	fun darkTheme() = context.stringDataStore(
		"preference_dark_theme",
		dependencyValue = { it != "off" }
	)

	@Composable
	fun darkThemeOled() = context.booleanDataStore(
		"preference_dark_theme_oled"
	)

	@Composable
	fun timetablePersonalTimetable() = context.stringDataStore(
		"preference_timetable_personal_timetable",
		defaultValue = ""
	)

	@Composable
	fun timetableHideTimeStamps() = context.booleanDataStore(
		"preference_timetable_hide_time_stamps"
	)

	@Composable
	fun timetableHideCancelled() = context.booleanDataStore(
		"preference_timetable_hide_cancelled"
	)

	@Composable
	fun timetableSubstitutionsIrregular() = context.booleanDataStore(
		"preference_timetable_substitutions_irregular"
	)

	@Composable
	fun timetableBackgroundIrregular() = context.booleanDataStore(
		"preference_timetable_background_irregular"
	)

	@Composable
	fun timetableRange() = context.stringDataStore(
		"preference_timetable_range",
		defaultValue = ""
	)

	@Composable
	fun timetableRangeIndexReset() = context.booleanDataStore(
		"preference_timetable_range_index_reset"
	)

	@Composable
	fun timetableItemPaddingOverlap() = context.intDataStore(
		"preference_timetable_item_padding_overlap"
	)

	@Composable
	fun timetableItemPadding() = context.intDataStore(
		"preference_timetable_item_padding"
	)

	@Composable
	fun timetableItemCornerRadius() = context.intDataStore(
		"preference_timetable_item_corner_radius"
	)

	@Composable
	fun timetableCenteredLessonInfo() = context.booleanDataStore(
		"preference_timetable_centered_lesson_info"
	)

	@Composable
	fun timetableBoldLessonName() = context.booleanDataStore(
		"preference_timetable_bold_lesson_name"
	)

	@Composable
	fun timetableLessonNameFontSize() = context.intDataStore(
		"preference_timetable_lesson_name_font_size"
	)

	@Composable
	fun timetableLessonInfoFontSize() = context.intDataStore(
		"preference_timetable_lesson_info_font_size"
	)

	@Composable
	fun notificationsEnable() = context.booleanDataStore(
		"preference_notifications_enable"
	)

	@Composable
	fun notificationsInMultiple() = context.booleanDataStore(
		"preference_notifications_in_multiple"
	)

	@Composable
	fun notificationsBeforeFirst() = context.booleanDataStore(
		"preference_notifications_before_first"
	)

	@Composable
	fun notificationsBeforeFirstTime() = context.intDataStore(
		"preference_notifications_before_first_time"
	)

	@Composable
	fun notificationsVisibilitySubjects() = context.stringDataStore(
		"preference_notifications_visibility_subjects"
	)

	@Composable
	fun notificationsVisibilityRooms() = context.stringDataStore(
		"preference_notifications_visibility_rooms"
	)

	@Composable
	fun notificationsVisibilityTeachers() = context.stringDataStore(
		"preference_notifications_visibility_teachers"
	)

	@Composable
	fun notificationsVisibilityClasses() = context.stringDataStore(
		"preference_notifications_visibility_classes"
	)

	@Composable
	fun connectivityRefreshInBackground() = context.booleanDataStore(
		"preference_connectivity_refresh_in_background"
	)

	@Composable
	fun proxyHost() = context.stringDataStore(
		"preference_connectivity_proxy_host",
		defaultValue = ""
	)
}
