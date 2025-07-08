package com.sapuseven.untis.data.database

import android.provider.BaseColumns
import androidx.room.DeleteColumn
import androidx.room.RenameColumn
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATIONS_USER_LEGACY = listOf(
	object : Migration(1, 2) {
		override fun migrate(db: SupportSQLiteDatabase) {
			db.execSQL("ALTER TABLE ${UserDatabaseLegacyContract.Users.TABLE_NAME} RENAME TO ${UserDatabaseLegacyContract.Users.TABLE_NAME}_v1")
			db.execSQL(UserDatabaseLegacyContract.Users.SQL_CREATE_ENTRIES_V2)
			db.execSQL("INSERT INTO ${UserDatabaseLegacyContract.Users.TABLE_NAME} SELECT _id, apiUrl, NULL, user, ${UserDatabaseLegacyContract.Users.TABLE_NAME}_v1.\"key\", anonymous, timeGrid, masterDataTimestamp, userData, settings, time_created FROM ${UserDatabaseLegacyContract.Users.TABLE_NAME}_v1;")
			db.execSQL("DROP TABLE ${UserDatabaseLegacyContract.Users.TABLE_NAME}_v1")
		}
	},
	object : Migration(2, 3) {
		override fun migrate(db: SupportSQLiteDatabase) {
			db.execSQL("ALTER TABLE ${UserDatabaseLegacyContract.Users.TABLE_NAME} RENAME TO ${UserDatabaseLegacyContract.Users.TABLE_NAME}_v2")
			db.execSQL(UserDatabaseLegacyContract.Users.SQL_CREATE_ENTRIES_V3)
			db.execSQL("INSERT INTO ${UserDatabaseLegacyContract.Users.TABLE_NAME} SELECT * FROM ${UserDatabaseLegacyContract.Users.TABLE_NAME}_v2;")
			db.execSQL("DROP TABLE ${UserDatabaseLegacyContract.Users.TABLE_NAME}_v2")
		}
	},
	object : Migration(3, 4) {
		override fun migrate(db: SupportSQLiteDatabase) {
			db.execSQL("ALTER TABLE ${UserDatabaseLegacyContract.Users.TABLE_NAME} RENAME TO ${UserDatabaseLegacyContract.Users.TABLE_NAME}_v3")
			db.execSQL(UserDatabaseLegacyContract.Users.SQL_CREATE_ENTRIES_V4)
			db.execSQL("INSERT INTO ${UserDatabaseLegacyContract.Users.TABLE_NAME} SELECT * FROM ${UserDatabaseLegacyContract.Users.TABLE_NAME}_v3;")
			db.execSQL("DROP TABLE ${UserDatabaseLegacyContract.Users.TABLE_NAME}_v3")
		}
	},
	object : Migration(4, 5) {
		override fun migrate(db: SupportSQLiteDatabase) {
			db.execSQL("ALTER TABLE ${UserDatabaseLegacyContract.Users.TABLE_NAME} RENAME TO ${UserDatabaseLegacyContract.Users.TABLE_NAME}_v4")
			db.execSQL(UserDatabaseLegacyContract.Users.SQL_CREATE_ENTRIES_V5)
			db.execSQL(
				"INSERT INTO ${UserDatabaseLegacyContract.Users.TABLE_NAME} SELECT " +
						"${BaseColumns._ID}," +
						"'', " +
						"${UserDatabaseLegacyContract.Users.COLUMN_NAME_APIURL}, " +
						"${UserDatabaseLegacyContract.Users.COLUMN_NAME_SCHOOL_ID}, " +
						"${UserDatabaseLegacyContract.Users.COLUMN_NAME_USER}, " +
						"${UserDatabaseLegacyContract.Users.COLUMN_NAME_KEY}, " +
						"${UserDatabaseLegacyContract.Users.COLUMN_NAME_ANONYMOUS}, " +
						"${UserDatabaseLegacyContract.Users.COLUMN_NAME_TIMEGRID}, " +
						"${UserDatabaseLegacyContract.Users.COLUMN_NAME_MASTERDATATIMESTAMP}, " +
						"${UserDatabaseLegacyContract.Users.COLUMN_NAME_USERDATA}, " +
						"${UserDatabaseLegacyContract.Users.COLUMN_NAME_SETTINGS}, " +
						"${UserDatabaseLegacyContract.Users.COLUMN_NAME_CREATED} " +
						"FROM ${UserDatabaseLegacyContract.Users.TABLE_NAME}_v4;"
			)
			db.execSQL("DROP TABLE ${UserDatabaseLegacyContract.Users.TABLE_NAME}_v4")
		}
	},
	object : Migration(5, 6) {
		override fun migrate(db: SupportSQLiteDatabase) {
			db.execSQL("ALTER TABLE ${UserDatabaseLegacyContract.Users.TABLE_NAME} RENAME TO ${UserDatabaseLegacyContract.Users.TABLE_NAME}_v5")
			db.execSQL(UserDatabaseLegacyContract.Users.SQL_CREATE_ENTRIES_V6)
			db.execSQL(
				"INSERT INTO ${UserDatabaseLegacyContract.Users.TABLE_NAME} SELECT " +
						"${BaseColumns._ID}," +
						"${UserDatabaseLegacyContract.Users.COLUMN_NAME_PROFILENAME}, " +
						"'', " +
						"${UserDatabaseLegacyContract.Users.COLUMN_NAME_SCHOOL_ID}, " +
						"${UserDatabaseLegacyContract.Users.COLUMN_NAME_USER}, " +
						"${UserDatabaseLegacyContract.Users.COLUMN_NAME_KEY}, " +
						"${UserDatabaseLegacyContract.Users.COLUMN_NAME_ANONYMOUS}, " +
						"${UserDatabaseLegacyContract.Users.COLUMN_NAME_TIMEGRID}, " +
						"${UserDatabaseLegacyContract.Users.COLUMN_NAME_MASTERDATATIMESTAMP}, " +
						"${UserDatabaseLegacyContract.Users.COLUMN_NAME_USERDATA}, " +
						"${UserDatabaseLegacyContract.Users.COLUMN_NAME_SETTINGS}, " +
						"${UserDatabaseLegacyContract.Users.COLUMN_NAME_CREATED} " +
						"FROM ${UserDatabaseLegacyContract.Users.TABLE_NAME}_v5;"
			)
			db.execSQL("DROP TABLE ${UserDatabaseLegacyContract.Users.TABLE_NAME}_v5")
		}
	},
	object : Migration(6, 7) {
		override fun migrate(db: SupportSQLiteDatabase) {
			db.execSQL("ALTER TABLE ${UserDatabaseLegacyContract.Users.TABLE_NAME} RENAME TO ${UserDatabaseLegacyContract.Users.TABLE_NAME}_v6")
			db.execSQL(UserDatabaseLegacyContract.Users.SQL_CREATE_ENTRIES_V7)
			db.execSQL(
				"INSERT INTO ${UserDatabaseLegacyContract.Users.TABLE_NAME} SELECT " +
						"${BaseColumns._ID}," +
						"'', " +
						"${UserDatabaseLegacyContract.Users.COLUMN_NAME_APIURL}, " +
						"${UserDatabaseLegacyContract.Users.COLUMN_NAME_SCHOOL_ID}, " +
						"${UserDatabaseLegacyContract.Users.COLUMN_NAME_USER}, " +
						"${UserDatabaseLegacyContract.Users.COLUMN_NAME_KEY}, " +
						"${UserDatabaseLegacyContract.Users.COLUMN_NAME_ANONYMOUS}, " +
						"${UserDatabaseLegacyContract.Users.COLUMN_NAME_TIMEGRID}, " +
						"${UserDatabaseLegacyContract.Users.COLUMN_NAME_MASTERDATATIMESTAMP}, " +
						"${UserDatabaseLegacyContract.Users.COLUMN_NAME_USERDATA}, " +
						"${UserDatabaseLegacyContract.Users.COLUMN_NAME_SETTINGS}, " +
						"${UserDatabaseLegacyContract.Users.COLUMN_NAME_CREATED}, " +
						"'[]' " +
						"FROM ${UserDatabaseLegacyContract.Users.TABLE_NAME}_v6;"
			)
			db.execSQL("DROP TABLE ${UserDatabaseLegacyContract.Users.TABLE_NAME}_v6")
		}
	}
)

val MIGRATION_USER_7_8 = object : Migration(7, 8) {
	fun SupportSQLiteDatabase.createIndices(tableName: String) {
		execSQL("CREATE INDEX IF NOT EXISTS `index_${tableName}_id` ON `${tableName}` (`id`)")
		execSQL("CREATE INDEX IF NOT EXISTS `index_${tableName}_userId` ON `${tableName}` (`userId`)")
	}

	override fun migrate(db: SupportSQLiteDatabase) {
		db.execSQL("CREATE TABLE IF NOT EXISTS `User` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `profileName` TEXT NOT NULL, `apiUrl` TEXT NOT NULL, `schoolId` TEXT NOT NULL, `user` TEXT, `key` TEXT, `anonymous` INTEGER NOT NULL, `timeGrid` TEXT NOT NULL, `masterDataTimestamp` INTEGER NOT NULL, `userData` TEXT NOT NULL, `settings` TEXT, `created` INTEGER, `bookmarks` TEXT NOT NULL)")
		db.execSQL("INSERT INTO User SELECT _id, profileName, apiUrl, schoolId, user, auth, anonymous, timeGrid, masterDataTimestamp, userData, settings, time_created, bookmarks FROM users")
		db.execSQL("DROP TABLE users")

		db.execSQL("CREATE TABLE IF NOT EXISTS `AbsenceReason` (`id` INTEGER NOT NULL, `userId` INTEGER NOT NULL, `name` TEXT NOT NULL, `longName` TEXT NOT NULL, `active` INTEGER NOT NULL, PRIMARY KEY(`id`, `userId`), FOREIGN KEY(`userId`) REFERENCES `User`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
		db.execSQL("INSERT INTO AbsenceReason SELECT id, _user_id, name, longName, active FROM absenceReasons")
		db.execSQL("DROP TABLE absenceReasons")
		db.createIndices("AbsenceReason")

		db.execSQL("CREATE TABLE IF NOT EXISTS `Department` (`id` INTEGER NOT NULL, `userId` INTEGER NOT NULL, `name` TEXT NOT NULL, `longName` TEXT NOT NULL, PRIMARY KEY(`id`, `userId`), FOREIGN KEY(`userId`) REFERENCES `User`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
		db.execSQL("INSERT INTO Department SELECT id, _user_id, name, longName FROM departments")
		db.execSQL("DROP TABLE departments")
		db.createIndices("Department")

		db.execSQL("CREATE TABLE IF NOT EXISTS `Duty` (`id` INTEGER NOT NULL, `userId` INTEGER NOT NULL, `name` TEXT NOT NULL, `longName` TEXT NOT NULL, `type` TEXT NOT NULL, PRIMARY KEY(`id`, `userId`), FOREIGN KEY(`userId`) REFERENCES `User`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
		db.execSQL("INSERT INTO Duty SELECT id, _user_id, name, longName, type FROM duties")
		db.execSQL("DROP TABLE duties")
		db.createIndices("Duty")

		db.execSQL("CREATE TABLE IF NOT EXISTS `EventReason` (`id` INTEGER NOT NULL, `userId` INTEGER NOT NULL, `name` TEXT NOT NULL, `longName` TEXT NOT NULL, `elementType` TEXT NOT NULL, `groupId` INTEGER NOT NULL, `active` INTEGER NOT NULL, PRIMARY KEY(`id`, `userId`), FOREIGN KEY(`userId`) REFERENCES `User`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
		db.execSQL("INSERT INTO EventReason SELECT id, _user_id, name, longName, elementType, groupId, active FROM eventReasons")
		db.execSQL("DROP TABLE eventReasons")
		db.createIndices("EventReason")

		db.execSQL("CREATE TABLE IF NOT EXISTS `EventReasonGroup` (`id` INTEGER NOT NULL, `userId` INTEGER NOT NULL, `name` TEXT NOT NULL, `longName` TEXT NOT NULL, `active` INTEGER NOT NULL, PRIMARY KEY(`id`, `userId`), FOREIGN KEY(`userId`) REFERENCES `User`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
		db.execSQL("INSERT INTO EventReasonGroup SELECT id, _user_id, name, longName, active FROM eventReasonGroups")
		db.execSQL("DROP TABLE eventReasonGroups")
		db.createIndices("EventReasonGroup")

		db.execSQL("CREATE TABLE IF NOT EXISTS `ExcuseStatus` (`id` INTEGER NOT NULL, `userId` INTEGER NOT NULL, `name` TEXT NOT NULL, `longName` TEXT NOT NULL, `excused` INTEGER NOT NULL, `active` INTEGER NOT NULL, PRIMARY KEY(`id`, `userId`), FOREIGN KEY(`userId`) REFERENCES `User`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
		db.execSQL("INSERT INTO ExcuseStatus SELECT id, _user_id, name, longName, excused, active FROM excuseStatuses")
		db.execSQL("DROP TABLE excuseStatuses")
		db.createIndices("ExcuseStatus")

		db.execSQL("CREATE TABLE IF NOT EXISTS `Holiday` (`id` INTEGER NOT NULL, `userId` INTEGER NOT NULL, `name` TEXT NOT NULL, `longName` TEXT NOT NULL, `startDate` TEXT NOT NULL, `endDate` TEXT NOT NULL, PRIMARY KEY(`id`, `userId`), FOREIGN KEY(`userId`) REFERENCES `User`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
		db.execSQL("INSERT INTO Holiday SELECT id, _user_id, name, longName, startDate, endDate FROM holidays")
		db.execSQL("DROP TABLE holidays")
		db.createIndices("Holiday")

		db.execSQL("CREATE TABLE IF NOT EXISTS `Klasse` (`id` INTEGER NOT NULL, `userId` INTEGER NOT NULL, `name` TEXT NOT NULL, `longName` TEXT NOT NULL, `departmentId` INTEGER NOT NULL, `startDate` TEXT NOT NULL, `endDate` TEXT NOT NULL, `foreColor` TEXT, `backColor` TEXT, `active` INTEGER NOT NULL, `displayable` INTEGER NOT NULL, PRIMARY KEY(`id`, `userId`), FOREIGN KEY(`userId`) REFERENCES `User`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
		db.execSQL("INSERT INTO Klasse SELECT id, _user_id, name, longName, departmentId, startDate, endDate, foreColor, backColor, active, displayable FROM klassen")
		db.execSQL("DROP TABLE klassen")
		db.createIndices("Klasse")

		db.execSQL("CREATE TABLE IF NOT EXISTS `Room` (`id` INTEGER NOT NULL, `userId` INTEGER NOT NULL, `name` TEXT NOT NULL, `longName` TEXT NOT NULL, `departmentId` INTEGER NOT NULL, `foreColor` TEXT, `backColor` TEXT, `active` INTEGER NOT NULL, `displayAllowed` INTEGER NOT NULL, PRIMARY KEY(`id`, `userId`), FOREIGN KEY(`userId`) REFERENCES `User`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
		db.execSQL("INSERT INTO Room SELECT id, _user_id, name, longName, departmentId, foreColor, backColor, active, displayAllowed FROM rooms")
		db.execSQL("DROP TABLE rooms")
		db.createIndices("Room")

		db.execSQL("CREATE TABLE IF NOT EXISTS `Subject` (`id` INTEGER NOT NULL, `userId` INTEGER NOT NULL, `name` TEXT NOT NULL, `longName` TEXT NOT NULL, `departmentIds` TEXT NOT NULL, `foreColor` TEXT, `backColor` TEXT, `active` INTEGER NOT NULL, `displayAllowed` INTEGER NOT NULL, PRIMARY KEY(`id`, `userId`), FOREIGN KEY(`userId`) REFERENCES `User`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
		db.execSQL("INSERT INTO Subject SELECT id, _user_id, name, longName, departmentIds, foreColor, backColor, active, displayAllowed FROM subjects")
		db.execSQL("UPDATE Subject SET departmentIds='[]' WHERE departmentIds=''")
		db.execSQL("DROP TABLE subjects")
		db.createIndices("Subject")

		db.execSQL("CREATE TABLE IF NOT EXISTS `Teacher` (`id` INTEGER NOT NULL, `userId` INTEGER NOT NULL, `name` TEXT NOT NULL, `firstName` TEXT NOT NULL, `lastName` TEXT NOT NULL, `departmentIds` TEXT NOT NULL, `foreColor` TEXT, `backColor` TEXT, `entryDate` TEXT, `exitDate` TEXT, `active` INTEGER NOT NULL, `displayAllowed` INTEGER NOT NULL, PRIMARY KEY(`id`, `userId`), FOREIGN KEY(`userId`) REFERENCES `User`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
		db.execSQL("INSERT INTO Teacher SELECT id, _user_id, name, firstName, lastName, departmentIds, foreColor, backColor, entryDate, exitDate, active, displayAllowed FROM teachers")
		db.execSQL("UPDATE Teacher SET departmentIds='[]' WHERE departmentIds=''")
		db.execSQL("DROP TABLE teachers")
		db.createIndices("Teacher")

		db.execSQL("CREATE TABLE IF NOT EXISTS `TeachingMethod` (`id` INTEGER NOT NULL, `userId` INTEGER NOT NULL, `name` TEXT NOT NULL, `longName` TEXT NOT NULL, PRIMARY KEY(`id`, `userId`), FOREIGN KEY(`userId`) REFERENCES `User`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
		db.execSQL("INSERT INTO TeachingMethod SELECT id, _user_id, name, longName FROM teachingMethods")
		db.execSQL("DROP TABLE teachingMethods")
		db.createIndices("TeachingMethod")

		db.execSQL("CREATE TABLE IF NOT EXISTS `SchoolYear` (`id` INTEGER NOT NULL, `userId` INTEGER NOT NULL, `name` TEXT NOT NULL, `startDate` TEXT NOT NULL, `endDate` TEXT NOT NULL, PRIMARY KEY(`id`, `userId`), FOREIGN KEY(`userId`) REFERENCES `User`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
		db.execSQL("INSERT INTO SchoolYear SELECT id, _user_id, name, startDate, endDate FROM schoolYears")
		db.execSQL("DROP TABLE schoolYears")
		db.createIndices("SchoolYear")
	}
}

@DeleteColumn.Entries(
	DeleteColumn(
		tableName = "User",
		columnName = "bookmarks"
	)
)
class MigrationSpec10to11 : AutoMigrationSpec

@RenameColumn(
	tableName = "User",
	fromColumnName = "apiUrl",
	toColumnName = "apiHost"
)
class MigrationSpec11to12 : AutoMigrationSpec
