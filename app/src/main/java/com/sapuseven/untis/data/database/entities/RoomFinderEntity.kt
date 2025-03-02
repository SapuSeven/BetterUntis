package com.sapuseven.untis.data.database.entities

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(
	tableName = "rooms",
	primaryKeys = ["id", "userId"],
	indices = [Index("id"), Index("userId")]
)
data class RoomFinderEntity(
	val id: Long,
	val userId: Long
)

@Dao
interface RoomFinderDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertAll(vararg roomFinderEntities: RoomFinderEntity)

	@Delete
	suspend fun delete(roomFinderEntity: RoomFinderEntity)

	@Query("SELECT * FROM rooms WHERE userId LIKE :userId")
	fun getAllByUserId(userId: Long): Flow<List<RoomFinderEntity>?>
}
