package com.sapuseven.untis.models.untis.masterdata

import android.content.ContentValues
import android.database.Cursor
import com.sapuseven.untis.annotations.Table
import com.sapuseven.untis.annotations.TableColumn
import com.sapuseven.untis.data.databases.TABLE_NAME_SCHOOL_YEARS
import com.sapuseven.untis.interfaces.TableModel
import kotlinx.serialization.Serializable

@Serializable
@Table(TABLE_NAME_SCHOOL_YEARS)
data class SchoolYear(
		@field:TableColumn("INTEGER NOT NULL") val id: Int = 0,
		@field:TableColumn("VARCHAR(255) NOT NULL") val name: String = "",
		@field:TableColumn("VARCHAR(255) NOT NULL") val startDate: String = "",
		@field:TableColumn("VARCHAR(255) NOT NULL") val endDate: String = ""
) : TableModel {
	companion object {
		const val TABLE_NAME = TABLE_NAME_SCHOOL_YEARS
	}

	override val tableName = TABLE_NAME
	override val elementId = id

	override fun generateValues(): ContentValues {
		val values = ContentValues()

		values.put("id", id)
		values.put("name", name)
		values.put("startDate", startDate)
		values.put("endDate", endDate)

		return values
	}

	override fun parseCursor(cursor: Cursor): TableModel {
		return SchoolYear(
				cursor.getInt(cursor.getColumnIndex("id")),
				cursor.getString(cursor.getColumnIndex("name")),
				cursor.getString(cursor.getColumnIndex("startDate")),
				cursor.getString(cursor.getColumnIndex("endDate"))
		)
	}
}
