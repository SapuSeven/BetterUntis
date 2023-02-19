package com.sapuseven.untis.data.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sapuseven.untis.data.databases.entities.User
import com.sapuseven.untis.models.untis.masterdata.*

@Database(
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
	],
	version = 1
)
abstract class UserDatabase : RoomDatabase()
