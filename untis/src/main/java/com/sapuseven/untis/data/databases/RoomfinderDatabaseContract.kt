package com.sapuseven.untis.data.databases


object RoomfinderDatabaseContract {
	const val TABLE_NAME = "roomFinder"
	const val COLUMN_NAME_ROOM_ID = "id"
	const val COLUMN_NAME_ROOM_NAME = "name"
	const val COLUMN_NAME_STATES = "states"

	const val SQL_CREATE_ENTRIES =
			"CREATE TABLE $TABLE_NAME (" +
					"$COLUMN_NAME_ROOM_ID INTEGER PRIMARY KEY," +
					"$COLUMN_NAME_ROOM_NAME VARCHAR(255) NOT NULL," +
					"$COLUMN_NAME_STATES VARCHAR(255) NOT NULL)"

	const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS $TABLE_NAME"
}
