package com.sapuseven.untis.data.databases

import android.content.Context
import androidx.room.*
import com.sapuseven.untis.data.databases.entities.User
import com.sapuseven.untis.data.databases.entities.UserDao
import com.sapuseven.untis.models.TimetableBookmark
import com.sapuseven.untis.models.untis.UntisSettings
import com.sapuseven.untis.models.untis.UntisUserData
import com.sapuseven.untis.models.untis.masterdata.*
import com.sapuseven.untis.models.untis.masterdata.Room
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Database(
	version = 8,
	entities = [
		User::class,
		AbsenceReason::class,
		Department::class,
		Duty::class,
		EventReason::class,
		EventReasonGroup::class,
		ExcuseStatus::class,
		Holiday::class,
		Klasse::class,
		Room::class,
		Subject::class,
		Teacher::class,
		TeachingMethod::class,
		SchoolYear::class,
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
					.allowMainThreadQueries() // TODO: Fix and delete this!
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
	fun encodeUntisUserData(untisUserData: UntisUserData?): String? = encode(untisUserData)

	@TypeConverter
	fun decodeUntisUserData(string: String?): UntisUserData? = decode(string)

	@TypeConverter
	fun encodeUntisSettings(untisSettings: UntisSettings?): String? = encode(untisSettings)

	@TypeConverter
	fun decodeUntisSettings(string: String?): UntisSettings? = decode(string)

	@TypeConverter
	fun encodeTimetableBookmarkSet(timetableBookmarks: Set<TimetableBookmark>?): String? =
		encode(timetableBookmarks)

	@TypeConverter
	fun decodeTimetableBookmarkSet(string: String?): Set<TimetableBookmark>? = decode(string)

	@TypeConverter
	fun encodeIntList(intList: List<Int>?): String? = encode(intList)

	@TypeConverter
	fun decodeIntList(string: String?): List<Int>? = decode(string)
}
