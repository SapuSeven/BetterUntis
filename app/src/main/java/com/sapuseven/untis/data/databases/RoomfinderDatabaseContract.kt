package com.sapuseven.untis.data.databases

import android.provider.BaseColumns


object RoomfinderDatabaseContract : BaseColumns {
	const val TABLE_NAME = "roomFinder"
	const val COLUMN_NAME_ROOM_NAME = "name"
	const val COLUMN_NAME_ROOM_ID = "id"
	const val COLUMN_NAME_STATES = "states"

	const val SQL_CREATE_ENTRIES =
			"CREATE TABLE $TABLE_NAME (" +
					"${BaseColumns._ID} INTEGER PRIMARY KEY," +
					"$COLUMN_NAME_ROOM_NAME VARCHAR(255) NOT NULL," +
					"$COLUMN_NAME_ROOM_ID INTEGER UNIQUE NOT NULL," +
					"$COLUMN_NAME_STATES VARCHAR(255) NOT NULL)"

	const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS $TABLE_NAME"
}
