package com.sapuseven.untis.models.untis.masterdata

import android.content.ContentValues
import android.database.Cursor
import com.sapuseven.untis.annotations.Table
import com.sapuseven.untis.annotations.TableColumn
import com.sapuseven.untis.data.databases.TABLE_NAME_DEPARTMENTS
import com.sapuseven.untis.interfaces.TableModel
import kotlinx.serialization.Serializable

@Serializable
@Table(TABLE_NAME_DEPARTMENTS)
data class Department(
		@field:TableColumn("INTEGER NOT NULL") val id: Int,
		@field:TableColumn("VARCHAR(255) NOT NULL") val name: String,
		@field:TableColumn("VARCHAR(255) NOT NULL") val longName: String
) : TableModel {
	companion object {
		const val TABLE_NAME = TABLE_NAME_DEPARTMENTS
	}

	override val tableName = TABLE_NAME
	override val elementId = id

	override fun generateValues(): ContentValues {
		val values = ContentValues()

		values.put("id", id)
		values.put("name", name)
		values.put("longName", longName)

		return values
	}

	override fun parseCursor(cursor: Cursor): TableModel {
		return Department(
				cursor.getInt(cursor.getColumnIndex("id")),
				cursor.getString(cursor.getColumnIndex("name")),
				cursor.getString(cursor.getColumnIndex("longName"))
		)
	}
}
