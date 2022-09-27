package com.sapuseven.untis.data.databases

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.sapuseven.untis.models.RoomFinderItem

private const val DATABASE_VERSION = 1
private const val DATABASE_NAME = "roomfinder-%d.db"

interface RoomFinderDatabase {
	companion object {
		private var instance: RoomFinderDatabase? = null

		fun createInstance(context: Context, profileId: Long): RoomFinderDatabase {
			return instance ?: RoomFinderDatabaseImpl(context, profileId)
		}
	}

	fun addRoom(room: RoomFinderItem)

	fun deleteRoom(id: Int): Boolean

	fun getRoom(id: Int): RoomFinderItem?

	fun getAllRooms(): List<RoomFinderItem>
}

class RoomFinderDatabaseImpl internal constructor(context: Context, profileId: Long) :
	RoomFinderDatabase,
	SQLiteOpenHelper(context, DATABASE_NAME.format(profileId), null, DATABASE_VERSION) {
	override fun onCreate(db: SQLiteDatabase) {
		db.execSQL(RoomfinderDatabaseContract.SQL_CREATE_ENTRIES)
	}

	override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
		// If you change the DATABASE_VERSION, insert logic to migrate the data
		db.execSQL(RoomfinderDatabaseContract.SQL_DELETE_ENTRIES)

		onCreate(db)
	}

	override fun addRoom(room: RoomFinderItem) {
		val db = writableDatabase

		db.delete(
			RoomfinderDatabaseContract.TABLE_NAME,
			RoomfinderDatabaseContract.COLUMN_NAME_ROOM_ID + "=?",
			arrayOf(room.id.toString())
		)
		db.insert(RoomfinderDatabaseContract.TABLE_NAME, null, room.generateValues())

		db.close()
	}

	override fun deleteRoom(id: Int): Boolean {
		val db = writableDatabase

		val affectedRows = db.delete(
			RoomfinderDatabaseContract.TABLE_NAME,
			RoomfinderDatabaseContract.COLUMN_NAME_ROOM_ID + "=?",
			arrayOf(id.toString())
		)

		db.close()

		return affectedRows > 0
	}

	override fun getRoom(id: Int): RoomFinderItem? {
		val db = this.readableDatabase

		val cursor = db.query(
			RoomfinderDatabaseContract.TABLE_NAME,
			arrayOf(
				RoomfinderDatabaseContract.COLUMN_NAME_ROOM_NAME,
				RoomfinderDatabaseContract.COLUMN_NAME_ROOM_ID,
				RoomfinderDatabaseContract.COLUMN_NAME_STATES
			),
			RoomfinderDatabaseContract.COLUMN_NAME_ROOM_ID + "=?",
			arrayOf(id.toString()), null, null, null
		)

		if (!cursor.moveToFirst())
			return null

		val room = RoomFinderItem(
			cursor.getInt(cursor.getColumnIndex("id")),
			RoomFinderItem.parseStateListFromString(cursor.getString(cursor.getColumnIndex("states")))
		)

		cursor.close()
		db.close()

		return room
	}

	override fun getAllRooms(): List<RoomFinderItem> {
		val rooms = ArrayList<RoomFinderItem>()
		val db = this.readableDatabase

		val cursor = db.query(
			RoomfinderDatabaseContract.TABLE_NAME,
			arrayOf(
				RoomfinderDatabaseContract.COLUMN_NAME_ROOM_NAME,
				RoomfinderDatabaseContract.COLUMN_NAME_ROOM_ID,
				RoomfinderDatabaseContract.COLUMN_NAME_STATES
			), null, null, null, null, null
		)

		if (cursor.moveToFirst()) {
			do {
				rooms.add(
					RoomFinderItem(
						cursor.getInt(cursor.getColumnIndex("id")),
						RoomFinderItem.parseStateListFromString(
							cursor.getString(
								cursor.getColumnIndex(
									"states"
								)
							)
						)
					)
				)
			} while (cursor.moveToNext())
		}

		cursor.close()
		db.close()

		return rooms
	}
}
