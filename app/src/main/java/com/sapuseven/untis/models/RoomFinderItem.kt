package com.sapuseven.untis.models

import android.content.ContentValues
import android.database.Cursor
import com.sapuseven.untis.annotations.Table
import com.sapuseven.untis.annotations.TableColumn
import com.sapuseven.untis.data.databases.RoomfinderDatabaseContract
import com.sapuseven.untis.interfaces.TableModel
import kotlinx.serialization.Serializable

@Serializable
@Table(RoomfinderDatabaseContract.TABLE_NAME)
data class RoomFinderItem(
		@field:TableColumn("INTEGER NOT NULL") val id: Int,
		@field:TableColumn("VARCHAR(255) NOT NULL") val name: String,
		@field:TableColumn("VARCHAR(255) NOT NULL") val states: List<Boolean>
) : TableModel {
	companion object {
		fun parseStateListFromString(states: String): List<Boolean> {
			return states.toCharArray().map { it == '1' }
		}
	}

	override fun generateValues(): ContentValues {
		val values = ContentValues()

		values.put("id", id)
		values.put("name", name)
		values.put("states", states.joinToString("") { if (it) "1" else "0" })

		return values
	}

	override fun getTableName(): String {
		return RoomfinderDatabaseContract.TABLE_NAME
	}

	override fun parseCursor(cursor: Cursor): TableModel {
		return RoomFinderItem(
				cursor.getInt(cursor.getColumnIndex("id")),
				cursor.getString(cursor.getColumnIndex("name")),
				parseStateListFromString(cursor.getString(cursor.getColumnIndex("states")))
		)
	}

	override fun getElementId(): Int {
		return id
	}
}
