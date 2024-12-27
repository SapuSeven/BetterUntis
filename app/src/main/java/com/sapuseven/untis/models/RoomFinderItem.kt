package com.sapuseven.untis.models

import android.content.ContentValues
import android.database.Cursor
import android.provider.BaseColumns
import com.sapuseven.untis.data.database.RoomfinderDatabaseContract

data class RoomFinderItem(
		val id: Int,
		val states: List<Boolean>
) {
	companion object {
		fun parseStateListFromString(states: String): List<Boolean> {
			return states.toCharArray().map { it == '1' }
		}

		fun parseCursor(cursor: Cursor): RoomFinderItem {
			return RoomFinderItem(
					cursor.getInt(cursor.getColumnIndex(BaseColumns._ID)),
					parseStateListFromString(cursor.getString(cursor.getColumnIndex(RoomfinderDatabaseContract.COLUMN_NAME_STATES)))
			)
		}
	}

	fun generateValues(): ContentValues {
		val values = ContentValues()

		values.put(BaseColumns._ID, id)
		values.put(RoomfinderDatabaseContract.COLUMN_NAME_STATES, states.joinToString("") { if (it) "1" else "0" })

		return values
	}
}
