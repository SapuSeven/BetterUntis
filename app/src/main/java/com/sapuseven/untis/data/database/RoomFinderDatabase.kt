package com.sapuseven.untis.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.sapuseven.untis.data.database.entities.RoomFinderDao
import com.sapuseven.untis.data.database.entities.RoomFinderEntity


@Database(
	version = 1,
	entities = [
		RoomFinderEntity::class,
	]
)
@TypeConverters(RoomFinderConverters::class)
abstract class RoomFinderDatabase : RoomDatabase() {
	abstract fun roomFinderDao(): RoomFinderDao
}

internal class RoomFinderConverters {
	@TypeConverter
	fun encodeBooleanList(list: List<Boolean>?): String? = list?.joinToString("") { if (it) "1" else "0" }

	@TypeConverter
	fun decodeBooleanList(string: String?): List<Boolean>? = string?.toCharArray()?.map { it == '1' }
}
