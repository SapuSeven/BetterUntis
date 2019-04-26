package com.sapuseven.untis.models

import android.content.ContentValues
import android.database.Cursor

data class RoomFinderItem(
		val id: Int,
		val name: String,
		val states: List<Boolean>
) {
	companion object {
		fun parseStateListFromString(states: String): List<Boolean> {
			return states.toCharArray().map { it == '1' }
		}

		fun parseCursor(cursor: Cursor): RoomFinderItem {
			return RoomFinderItem(
					cursor.getInt(cursor.getColumnIndex("id")),
					cursor.getString(cursor.getColumnIndex("name")),
					parseStateListFromString(cursor.getString(cursor.getColumnIndex("states")))
			)
		}
	}

	fun generateValues(): ContentValues {
		val values = ContentValues()

		values.put("id", id)
		values.put("name", name)
		values.put("states", states.joinToString("") { if (it) "1" else "0" })

		return values
	}
}
