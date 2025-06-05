package com.sapuseven.untis.modules

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.google.protobuf.InvalidProtocolBufferException
import com.sapuseven.untis.api.model.untis.timetable.PeriodElement
import com.sapuseven.untis.data.settings.model.AbsencesTimeRange
import com.sapuseven.untis.data.settings.model.DarkTheme
import com.sapuseven.untis.data.settings.model.NotificationVisibility
import com.sapuseven.untis.data.settings.model.Settings
import com.sapuseven.untis.data.settings.model.UserSettings
import com.sapuseven.untis.ui.preferences.toTimetableElement
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Singleton

private const val DATA_STORE_FILE_NAME = "settings.pb"

private val Context.oldPreferenceDataStore by preferencesDataStore(name = "preferences")

@InstallIn(SingletonComponent::class)
@Module
object DataStoreModule {
	@Provides
	@Singleton
	fun provideProtoDataStore(@ApplicationContext appContext: Context): DataStore<Settings> {
		return DataStoreFactory.create(
			serializer = UserSettingsSerializer,
			produceFile = { appContext.dataStoreFile(DATA_STORE_FILE_NAME) },
			corruptionHandler = ReplaceFileCorruptionHandler {
				Settings.getDefaultInstance()
			},
			migrations = listOf(
				OldPreferenceDataStoreMigration(appContext)
			),
			scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
		)
	}
}

private class OldPreferenceDataStoreMigration(val context: Context) : DataMigration<Settings> {
	val oldDataStore: DataStore<Preferences> = context.oldPreferenceDataStore
	val oldDataStoreFile: File = File(context.filesDir, "datastore/preferences.preferences_pb")

	private val preferenceKeyRegex = "(\\d+)(?:_preference)?_(.+)".toRegex()

	override suspend fun shouldMigrate(currentData: Settings): Boolean {
		return oldDataStoreFile.exists()
	}

	override suspend fun migrate(currentData: Settings): Settings {
		val newDataBuilder = currentData.toBuilder()

		oldDataStore.data.first().asMap()
			.mapNotNull { entry -> preferenceKeyRegex.matchEntire(entry.key.name)?.let { it to entry.value } }
			.groupBy(
				keySelector = { it.first.groupValues[1].toLong() },
				valueTransform = { it.first.groupValues[2] to it.second }
			).forEach { (userId, data) ->
				//@formatter:off
				val userSettings = (currentData.userSettingsMap[userId] ?: UserSettings.getDefaultInstance()).toBuilder()
				data.forEach { (oldKey, value) ->
					try {
						when (oldKey) {
							"fling_enable" -> userSettings.flingEnable = value as Boolean
							"week_snap_to_days" -> userSettings.weekSnapToDays = value as Boolean
							"week_custom_range" -> userSettings.weekCustomRangeList.addAll((value as Set<*>).map { it.toString() })
							"week_custom_display_length" -> userSettings.weekCustomLength = value as Float
							"automute_enable" -> userSettings.automuteEnable = value as Boolean
							"automute_cancelled_lessons" -> userSettings.automuteCancelledLessons = value as Boolean
							"automute_minimum_break_length" -> userSettings.automuteMinimumBreakLength = value as Float
							"timetable_item_text_light" -> userSettings.timetableItemTextLight = value as Boolean
							"background_future" -> userSettings.backgroundFuture = value as Int
							"background_past" -> userSettings.backgroundPast = value as Int
							"marker" -> userSettings.marker = value as Int
							"background_regular" -> userSettings.backgroundRegular = value as Int
							"background_regular_past" -> userSettings.backgroundRegularPast = value as Int
							"background_exam" -> userSettings.backgroundExam = value as Int
							"background_exam_past" -> userSettings.backgroundExamPast = value as Int
							"background_irregular" -> userSettings.backgroundIrregular = value as Int
							"background_irregular_past" -> userSettings.backgroundIrregularPast = value as Int
							"background_cancelled" -> userSettings.backgroundCancelled = value as Int
							"background_cancelled_past" -> userSettings.backgroundCancelledPast = value as Int
							"theme_color" -> userSettings.themeColor = value as Int
							"dark_theme" -> userSettings.darkTheme = mapEnumDarkTheme(value as String)
							"dark_theme_oled" -> userSettings.darkThemeOled = value as Boolean
							"timetable_personal_timetable" -> userSettings.timetablePersonalTimetable = Json.decodeFromString<PeriodElement>(value as String).toTimetableElement()
							"timetable_hide_timestamps" -> userSettings.timetableHideTimeStamps = value as Boolean
							"timetable_hide_cancelled" -> userSettings.timetableHideCancelled = value as Boolean
							"timetable_substitutions_irregular" -> userSettings.timetableSubstitutionsIrregular = value as Boolean
							"timetable_background_irregular" -> userSettings.timetableBackgroundIrregular = value as Boolean
							"timetable_range" -> userSettings.timetableRange = value as String
							"timetable_range_index_reset" -> userSettings.timetableRangeIndexReset = value as Boolean
							"timetable_item_padding" -> userSettings.timetableItemPadding = value as Int
							"timetable_item_corner_radius" -> userSettings.timetableItemCornerRadius = value as Int
							"timetable_centered_lesson_info" -> userSettings.timetableCenteredLessonInfo = value as Boolean
							"timetable_bold_lesson_name" -> userSettings.timetableBoldLessonName = value as Boolean
							"timetable_lesson_name_font_size" -> userSettings.timetableLessonNameFontSize = value as Int
							"timetable_lesson_info_font_size" -> userSettings.timetableLessonInfoFontSize = value as Int
							"notifications_enable" -> userSettings.notificationsEnable = value as Boolean
							"notifications_in_multiple" -> userSettings.notificationsInMultiple = value as Boolean
							"notifications_before_first" -> userSettings.notificationsBeforeFirst = value as Boolean
							"notifications_before_first_time" -> userSettings.notificationsBeforeFirstTime = value as Int
							"notifications_visibility_subjects" -> userSettings.notificationsVisibilitySubjects = mapEnumNotificationVisibility(value as String)
							"notifications_visibility_rooms" -> userSettings.notificationsVisibilityRooms = mapEnumNotificationVisibility(value as String)
							"notifications_visibility_teachers" -> userSettings.notificationsVisibilityTeachers = mapEnumNotificationVisibility(value as String)
							"notifications_visibility_classes" -> userSettings.notificationsVisibilityClasses = mapEnumNotificationVisibility(value as String)
							"connectivity_refresh_in_background" -> userSettings.connectivityRefreshInBackground = value as Boolean
							"connectivity_proxy_host" -> userSettings.proxyHost = value as String
							"school_background" -> userSettings.schoolBackgroundList.addAll((value as Set<*>).map { it.toString() })
							"infocenter_absences_unexcused_only" -> userSettings.infocenterAbsencesOnlyUnexcused = value as Boolean
							"infocenter_absences_sort" -> userSettings.infocenterAbsencesSortReverse = value as Boolean
							"infocenter_absences_timerange" -> userSettings.infocenterAbsencesTimeRange = mapEnumAbsenceTimeRange(value as String)
						}
					} catch (_: Exception) {
						// Ignore all potential errors
					}
				}
				//@formatter:on

				newDataBuilder.putUserSettings(userId, userSettings.build())
			}

		return newDataBuilder.build()
	}

	private fun mapEnumNotificationVisibility(value: String): NotificationVisibility {
		return when (value) {
			"short" -> NotificationVisibility.SHORT
			"long" -> NotificationVisibility.LONG
			"hidden" -> NotificationVisibility.NONE
			else -> NotificationVisibility.UNRECOGNIZED
		}
	}

	private fun mapEnumAbsenceTimeRange(value: String): AbsencesTimeRange {
		return when (value) {
			"current_schoolyear" -> AbsencesTimeRange.CURRENT_SCHOOLYEAR
			"seven_days" -> AbsencesTimeRange.SEVEN_DAYS
			"fourteen_days" -> AbsencesTimeRange.FOURTEEN_DAYS
			"thirty_days" -> AbsencesTimeRange.THIRTY_DAYS
			"ninety_days" -> AbsencesTimeRange.NINETY_DAYS
			else -> AbsencesTimeRange.UNRECOGNIZED
		}
	}

	private fun mapEnumDarkTheme(value: String): DarkTheme {
		return when (value) {
			"auto" -> DarkTheme.AUTO
			"on" -> DarkTheme.DARK
			"off" -> DarkTheme.LIGHT
			else -> DarkTheme.UNRECOGNIZED
		}
	}

	override suspend fun cleanUp() {
		oldDataStoreFile.delete()
	}
}

internal object UserSettingsSerializer : Serializer<Settings> {
	override val defaultValue: Settings = Settings.getDefaultInstance()

	override suspend fun readFrom(input: InputStream): Settings {
		try {
			return Settings.parseFrom(input)
		} catch (exception: InvalidProtocolBufferException) {
			throw CorruptionException("Cannot read proto", exception)
		}
	}

	override suspend fun writeTo(
		t: Settings,
		output: OutputStream
	) = t.writeTo(output)
}
