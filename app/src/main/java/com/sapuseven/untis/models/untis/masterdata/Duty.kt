package com.sapuseven.untis.models.untis.masterdata

import android.content.ContentValues
import android.database.Cursor
import com.sapuseven.untis.annotations.Table
import com.sapuseven.untis.annotations.TableColumn
import com.sapuseven.untis.data.databases.TABLE_NAME_DUTIES
import com.sapuseven.untis.interfaces.TableModel
import kotlinx.serialization.Serializable

@Serializable
@Table(TABLE_NAME_DUTIES)
data class Duty(
		@field:TableColumn("INTEGER NOT NULL") val id: Int,
		@field:TableColumn("VARCHAR(255) NOT NULL") val name: String,
		@field:TableColumn("VARCHAR(255) NOT NULL") val longName: String,
		@field:TableColumn("VARCHAR(255) NOT NULL") val type: String
) : TableModel {
	companion object {
		const val TABLE_NAME = TABLE_NAME_DUTIES
	}

	override val tableName = TABLE_NAME
	override val elementId = id

	override fun generateValues(): ContentValues {
		val values = ContentValues()

		values.put("id", id)
		values.put("name", name)
		values.put("longName", longName)
		values.put("type", type)

		return values
	}

	override fun parseCursor(cursor: Cursor): TableModel {
		return Duty(
				cursor.getInt(cursor.getColumnIndex("id")),
				cursor.getString(cursor.getColumnIndex("name")),
				cursor.getString(cursor.getColumnIndex("longName")),
				cursor.getString(cursor.getColumnIndex("type"))
		)
	}
}
