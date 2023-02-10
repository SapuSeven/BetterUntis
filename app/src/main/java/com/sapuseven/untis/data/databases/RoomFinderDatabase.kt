package com.sapuseven.untis.data.databases

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import com.sapuseven.untis.models.RoomFinderItem

private const val DATABASE_VERSION = 2
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
		db.execSQL(RoomfinderDatabaseContract.SQL_CREATE_ENTRIES_V2)
	}

	override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
		var currentVersion = oldVersion

		while (currentVersion < newVersion) {
			when (currentVersion) {
				1 -> {
					db.execSQL("ALTER TABLE ${RoomfinderDatabaseContract.TABLE_NAME} RENAME TO ${RoomfinderDatabaseContract.TABLE_NAME}_v1")
					db.execSQL(RoomfinderDatabaseContract.SQL_CREATE_ENTRIES_V2)
					db.execSQL("INSERT INTO ${RoomfinderDatabaseContract.TABLE_NAME} SELECT id, states FROM ${RoomfinderDatabaseContract.TABLE_NAME}_v1;")
					db.execSQL("DROP TABLE ${RoomfinderDatabaseContract.TABLE_NAME}_v1")
				}
			}

			currentVersion++
		}
	}

	override fun addRoom(room: RoomFinderItem) {
		val db = writableDatabase

		db.delete(
			RoomfinderDatabaseContract.TABLE_NAME,
			BaseColumns._ID + "=?",
			arrayOf(room.id.toString())
		)
		db.insert(RoomfinderDatabaseContract.TABLE_NAME, null, room.generateValues())

		db.close()
	}

	override fun deleteRoom(id: Int): Boolean {
		val db = writableDatabase

		val affectedRows = db.delete(
			RoomfinderDatabaseContract.TABLE_NAME,
			BaseColumns._ID + "=?",
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
				BaseColumns._ID,
				RoomfinderDatabaseContract.COLUMN_NAME_STATES
			),
			BaseColumns._ID + "=?",
			arrayOf(id.toString()), null, null, null
		)

		if (!cursor.moveToFirst())
			return null

		val room = RoomFinderItem(
			cursor.getInt(cursor.getColumnIndex(BaseColumns._ID)),
			RoomFinderItem.parseStateListFromString(cursor.getString(cursor.getColumnIndex(RoomfinderDatabaseContract.COLUMN_NAME_STATES)))
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
				BaseColumns._ID,
				RoomfinderDatabaseContract.COLUMN_NAME_STATES
			), null, null, null, null, null
		)

		if (cursor.moveToFirst()) {
			do {
				rooms.add(
					RoomFinderItem(
						cursor.getInt(cursor.getColumnIndex(BaseColumns._ID)),
						RoomFinderItem.parseStateListFromString(
							cursor.getString(
								cursor.getColumnIndex(RoomfinderDatabaseContract.COLUMN_NAME_STATES)
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
