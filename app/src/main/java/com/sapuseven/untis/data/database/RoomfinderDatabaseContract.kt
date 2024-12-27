package com.sapuseven.untis.data.database

import android.provider.BaseColumns


object RoomfinderDatabaseContract {
	const val TABLE_NAME = "roomFinder"
	private const val COLUMN_NAME_ROOM_ID = "id" // Only used for backwards compatibility
	private const val COLUMN_NAME_ROOM_NAME = "name" // Only used for backwards compatibility
	const val COLUMN_NAME_STATES = "states"

	const val SQL_CREATE_ENTRIES_V1 =
			"CREATE TABLE $TABLE_NAME (" +
					"$COLUMN_NAME_ROOM_ID INTEGER PRIMARY KEY," +
					"$COLUMN_NAME_ROOM_NAME VARCHAR(255) NOT NULL," +
					"$COLUMN_NAME_STATES VARCHAR(255) NOT NULL)"

	const val SQL_CREATE_ENTRIES_V2 =
			"CREATE TABLE $TABLE_NAME (" +
					"${BaseColumns._ID} INTEGER PRIMARY KEY," +
					"$COLUMN_NAME_STATES VARCHAR(255) NOT NULL)"

	const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS $TABLE_NAME"
}
