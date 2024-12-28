package com.sapuseven.untis.data.database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.sapuseven.untis.api.model.untis.Settings
import com.sapuseven.untis.api.model.untis.UserData
import com.sapuseven.untis.api.model.untis.masterdata.TimeGrid
import com.sapuseven.untis.data.database.entities.AbsenceReasonEntity
import com.sapuseven.untis.data.database.entities.DepartmentEntity
import com.sapuseven.untis.data.database.entities.DutyEntity
import com.sapuseven.untis.data.database.entities.EventReasonEntity
import com.sapuseven.untis.data.database.entities.EventReasonGroupEntity
import com.sapuseven.untis.data.database.entities.ExcuseStatusEntity
import com.sapuseven.untis.data.database.entities.HolidayEntity
import com.sapuseven.untis.data.database.entities.KlasseEntity
import com.sapuseven.untis.data.database.entities.RoomEntity
import com.sapuseven.untis.data.database.entities.SchoolYearEntity
import com.sapuseven.untis.data.database.entities.SubjectEntity
import com.sapuseven.untis.data.database.entities.TeacherEntity
import com.sapuseven.untis.data.database.entities.TeachingMethodEntity
import com.sapuseven.untis.data.database.entities.User
import com.sapuseven.untis.data.database.entities.UserDao
import com.sapuseven.untis.models.TimetableBookmark
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Database(
	version = 10,
	entities = [
		User::class,
		AbsenceReasonEntity::class,
		DepartmentEntity::class,
		DutyEntity::class,
		EventReasonEntity::class,
		EventReasonGroupEntity::class,
		ExcuseStatusEntity::class,
		HolidayEntity::class,
		KlasseEntity::class,
		RoomEntity::class,
		SubjectEntity::class,
		TeacherEntity::class,
		TeachingMethodEntity::class,
		SchoolYearEntity::class,
	],
	autoMigrations = [
		AutoMigration(from = 8, to = 9),
		AutoMigration(from = 9, to = 10)
	]
)
@TypeConverters(Converters::class)
abstract class UserDatabase : RoomDatabase() {
	abstract fun userDao(): UserDao

	companion object {
		@Volatile
		private var instance: UserDatabase? = null

		fun getInstance(context: Context): UserDatabase =
			instance ?: synchronized(this) {
				instance ?: androidx.room.Room.databaseBuilder(
					context,
					UserDatabase::class.java, "userdata.db"
				)
					.addMigrations(
						*MIGRATIONS_LEGACY.toTypedArray(),
						MIGRATION_7_8,
					)
					.build()
					.also { instance = it }
			}
	}
}

internal class Converters {
	private inline fun <reified T> encode(value: T?): String? =
		value?.let { Json.encodeToString<T>(it) }

	private inline fun <reified T> decode(string: String?): T? =
		string?.let { Json.decodeFromString<T>(it) }

	@TypeConverter
	fun encodeTimeGrid(timeGrid: TimeGrid?): String? = encode(timeGrid)

	@TypeConverter
	fun decodeTimeGrid(string: String?): TimeGrid? = decode(string)

	@TypeConverter
	fun encodeUntisUserData(untisUserData: UserData?): String? = encode(untisUserData)

	@TypeConverter
	fun decodeUntisUserData(string: String?): UserData? = decode(string)

	@TypeConverter
	fun encodeUntisSettings(untisSettings: Settings?): String? = encode(untisSettings)

	@TypeConverter
	fun decodeUntisSettings(string: String?): Settings? = decode(string)

	@TypeConverter
	fun encodeTimetableBookmarkSet(timetableBookmarks: Set<TimetableBookmark>?): String? = encode(timetableBookmarks)

	@TypeConverter
	fun decodeTimetableBookmarkSet(string: String?): Set<TimetableBookmark>? = decode(string)

	@TypeConverter
	fun encodeLongList(intList: List<Long>?): String? = encode(intList)

	@TypeConverter
	fun decodeLongList(string: String?): List<Long>? = decode(string)

	@TypeConverter
	fun encodeDate(date: LocalDate?): String? = date?.format(DateTimeFormatter.ISO_LOCAL_DATE)

	@TypeConverter
	fun decodeDate(date: String?): LocalDate? =
		date?.let { runCatching { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) }.getOrNull() }

	@TypeConverter
	fun encodeTime(date: LocalTime?): String? = date?.format(DateTimeFormatter.ISO_LOCAL_TIME)

	@TypeConverter
	fun decodeTime(date: String?): LocalTime? =
		date?.let { runCatching { LocalTime.parse(it, DateTimeFormatter.ISO_LOCAL_TIME) }.getOrNull() }
}
