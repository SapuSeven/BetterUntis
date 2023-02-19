package com.sapuseven.untis.data.databases

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.Cursor.FIELD_TYPE_INTEGER
import android.database.Cursor.FIELD_TYPE_STRING
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.sapuseven.untis.R
import com.sapuseven.untis.helpers.SerializationUtils.getJSON
import com.sapuseven.untis.helpers.UserDatabaseQueryHelper.COLUMN_NAME_USER_ID
import com.sapuseven.untis.helpers.UserDatabaseQueryHelper.generateCreateTable
import com.sapuseven.untis.helpers.UserDatabaseQueryHelper.generateDropTable
import com.sapuseven.untis.helpers.UserDatabaseQueryHelper.generateValues
import com.sapuseven.untis.interfaces.TableModel
import com.sapuseven.untis.models.TimetableBookmark
import com.sapuseven.untis.models.untis.UntisMasterData
import com.sapuseven.untis.models.untis.UntisSettings
import com.sapuseven.untis.models.untis.UntisUserData
import com.sapuseven.untis.models.untis.masterdata.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

private const val DATABASE_VERSION = 7
private const val DATABASE_NAME = "userdata.db"

@Deprecated("Use the new UserDatabase instead")
private class LegacyUserDatabase private constructor(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
	override fun onCreate(db: SQLiteDatabase) {
		db.execSQL(UserDatabaseContract.Users.SQL_CREATE_ENTRIES_V7)
		db.execSQL(generateCreateTable<AbsenceReason>())
		db.execSQL(generateCreateTable<Department>())
		db.execSQL(generateCreateTable<Duty>())
		db.execSQL(generateCreateTable<EventReason>())
		db.execSQL(generateCreateTable<EventReasonGroup>())
		db.execSQL(generateCreateTable<ExcuseStatus>())
		db.execSQL(generateCreateTable<Holiday>())
		db.execSQL(generateCreateTable<Klasse>())
		db.execSQL(generateCreateTable<Room>())
		db.execSQL(generateCreateTable<Subject>())
		db.execSQL(generateCreateTable<Teacher>())
		db.execSQL(generateCreateTable<TeachingMethod>())
		db.execSQL(generateCreateTable<SchoolYear>())
	}

	override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
		var currentVersion = oldVersion

		while (currentVersion < newVersion) {
			when (currentVersion) {
				1 -> {
					db.execSQL("ALTER TABLE ${UserDatabaseContract.Users.TABLE_NAME} RENAME TO ${UserDatabaseContract.Users.TABLE_NAME}_v1")
					db.execSQL(UserDatabaseContract.Users.SQL_CREATE_ENTRIES_V2)
					db.execSQL("INSERT INTO ${UserDatabaseContract.Users.TABLE_NAME} SELECT _id, apiUrl, NULL, user, ${UserDatabaseContract.Users.TABLE_NAME}_v1.\"key\", anonymous, timeGrid, masterDataTimestamp, userData, settings, time_created FROM ${UserDatabaseContract.Users.TABLE_NAME}_v1;")
					db.execSQL("DROP TABLE ${UserDatabaseContract.Users.TABLE_NAME}_v1")
				}
				2 -> {
					db.execSQL("ALTER TABLE ${UserDatabaseContract.Users.TABLE_NAME} RENAME TO ${UserDatabaseContract.Users.TABLE_NAME}_v2")
					db.execSQL(UserDatabaseContract.Users.SQL_CREATE_ENTRIES_V3)
					db.execSQL("INSERT INTO ${UserDatabaseContract.Users.TABLE_NAME} SELECT * FROM ${UserDatabaseContract.Users.TABLE_NAME}_v2;")
					db.execSQL("DROP TABLE ${UserDatabaseContract.Users.TABLE_NAME}_v2")
				}
				3 -> {
					db.execSQL("ALTER TABLE ${UserDatabaseContract.Users.TABLE_NAME} RENAME TO ${UserDatabaseContract.Users.TABLE_NAME}_v3")
					db.execSQL(UserDatabaseContract.Users.SQL_CREATE_ENTRIES_V4)
					db.execSQL("INSERT INTO ${UserDatabaseContract.Users.TABLE_NAME} SELECT * FROM ${UserDatabaseContract.Users.TABLE_NAME}_v3;")
					db.execSQL("DROP TABLE ${UserDatabaseContract.Users.TABLE_NAME}_v3")
				}
				4 -> {
					db.execSQL("ALTER TABLE ${UserDatabaseContract.Users.TABLE_NAME} RENAME TO ${UserDatabaseContract.Users.TABLE_NAME}_v4")
					db.execSQL(UserDatabaseContract.Users.SQL_CREATE_ENTRIES_V5)
					db.execSQL(
						"INSERT INTO ${UserDatabaseContract.Users.TABLE_NAME} SELECT " +
								"${BaseColumns._ID}," +
								"'', " +
								"${UserDatabaseContract.Users.COLUMN_NAME_APIURL}, " +
								"${UserDatabaseContract.Users.COLUMN_NAME_SCHOOL_ID}, " +
								"${UserDatabaseContract.Users.COLUMN_NAME_USER}, " +
								"${UserDatabaseContract.Users.COLUMN_NAME_KEY}, " +
								"${UserDatabaseContract.Users.COLUMN_NAME_ANONYMOUS}, " +
								"${UserDatabaseContract.Users.COLUMN_NAME_TIMEGRID}, " +
								"${UserDatabaseContract.Users.COLUMN_NAME_MASTERDATATIMESTAMP}, " +
								"${UserDatabaseContract.Users.COLUMN_NAME_USERDATA}, " +
								"${UserDatabaseContract.Users.COLUMN_NAME_SETTINGS}, " +
								"${UserDatabaseContract.Users.COLUMN_NAME_CREATED} " +
								"FROM ${UserDatabaseContract.Users.TABLE_NAME}_v4;"
					)
					db.execSQL("DROP TABLE ${UserDatabaseContract.Users.TABLE_NAME}_v4")
				}
				5 -> {
					db.execSQL("ALTER TABLE ${UserDatabaseContract.Users.TABLE_NAME} RENAME TO ${UserDatabaseContract.Users.TABLE_NAME}_v5")
					db.execSQL(UserDatabaseContract.Users.SQL_CREATE_ENTRIES_V6)
					db.execSQL(
						"INSERT INTO ${UserDatabaseContract.Users.TABLE_NAME} SELECT " +
								"${BaseColumns._ID}," +
								"${UserDatabaseContract.Users.COLUMN_NAME_PROFILENAME}, " +
								"'', " +
								"${UserDatabaseContract.Users.COLUMN_NAME_SCHOOL_ID}, " +
								"${UserDatabaseContract.Users.COLUMN_NAME_USER}, " +
								"${UserDatabaseContract.Users.COLUMN_NAME_KEY}, " +
								"${UserDatabaseContract.Users.COLUMN_NAME_ANONYMOUS}, " +
								"${UserDatabaseContract.Users.COLUMN_NAME_TIMEGRID}, " +
								"${UserDatabaseContract.Users.COLUMN_NAME_MASTERDATATIMESTAMP}, " +
								"${UserDatabaseContract.Users.COLUMN_NAME_USERDATA}, " +
								"${UserDatabaseContract.Users.COLUMN_NAME_SETTINGS}, " +
								"${UserDatabaseContract.Users.COLUMN_NAME_CREATED} " +
								"FROM ${UserDatabaseContract.Users.TABLE_NAME}_v5;"
					)
					db.execSQL("DROP TABLE ${UserDatabaseContract.Users.TABLE_NAME}_v5")
				}
				6 -> {
					db.execSQL("ALTER TABLE ${UserDatabaseContract.Users.TABLE_NAME} RENAME TO ${UserDatabaseContract.Users.TABLE_NAME}_v6")
					db.execSQL(UserDatabaseContract.Users.SQL_CREATE_ENTRIES_V7)
					db.execSQL(
							"INSERT INTO ${UserDatabaseContract.Users.TABLE_NAME} SELECT " +
									"${BaseColumns._ID}," +
									"'', " +
									"${UserDatabaseContract.Users.COLUMN_NAME_APIURL}, " +
									"${UserDatabaseContract.Users.COLUMN_NAME_SCHOOL_ID}, " +
									"${UserDatabaseContract.Users.COLUMN_NAME_USER}, " +
									"${UserDatabaseContract.Users.COLUMN_NAME_KEY}, " +
									"${UserDatabaseContract.Users.COLUMN_NAME_ANONYMOUS}, " +
									"${UserDatabaseContract.Users.COLUMN_NAME_TIMEGRID}, " +
									"${UserDatabaseContract.Users.COLUMN_NAME_MASTERDATATIMESTAMP}, " +
									"${UserDatabaseContract.Users.COLUMN_NAME_USERDATA}, " +
									"${UserDatabaseContract.Users.COLUMN_NAME_SETTINGS}, " +
									"${UserDatabaseContract.Users.COLUMN_NAME_CREATED}, " +
									"'[]' " +
									"FROM ${UserDatabaseContract.Users.TABLE_NAME}_v6;"
					)
					db.execSQL("DROP TABLE ${UserDatabaseContract.Users.TABLE_NAME}_v6")
				}
			}

			currentVersion++
		}
	}

	fun resetDatabase(db: SQLiteDatabase) {
		db.execSQL(UserDatabaseContract.Users.SQL_DELETE_ENTRIES)

		db.execSQL(generateDropTable<AbsenceReason>())
		db.execSQL(generateDropTable<Department>())
		db.execSQL(generateDropTable<Duty>())
		db.execSQL(generateDropTable<EventReason>())
		db.execSQL(generateDropTable<EventReasonGroup>())
		db.execSQL(generateDropTable<ExcuseStatus>())
		db.execSQL(generateDropTable<Holiday>())
		db.execSQL(generateDropTable<Klasse>())
		db.execSQL(generateDropTable<Room>())
		db.execSQL(generateDropTable<Subject>())
		db.execSQL(generateDropTable<Teacher>())
		db.execSQL(generateDropTable<TeachingMethod>())
		db.execSQL(generateDropTable<SchoolYear>())
	}
}

private fun Cursor.getIntOrNull(columnIndex: Int): Int? {
	return if (getType(columnIndex) == FIELD_TYPE_INTEGER)
		getInt(columnIndex)
	else null
}

private fun Cursor.getLongOrNull(columnIndex: Int): Long? {
	return if (getType(columnIndex) == FIELD_TYPE_INTEGER)
		getLong(columnIndex)
	else null
}

private fun Cursor.getStringOrNull(columnIndex: Int): String? {
	return if (getType(columnIndex) == FIELD_TYPE_STRING)
		getString(columnIndex)
	else null
}
