package com.sapuseven.untis.models.untis.masterdata

import android.content.ContentValues
import android.database.Cursor
import com.sapuseven.untis.annotations.Table
import com.sapuseven.untis.annotations.TableColumn
import com.sapuseven.untis.data.databases.TABLE_NAME_KLASSEN
import com.sapuseven.untis.interfaces.TableModel
import kotlinx.serialization.Serializable

@Serializable
@Table(TABLE_NAME_KLASSEN)
data class Klasse(
		@field:TableColumn("INTEGER NOT NULL") val id: Int = 0,
		@field:TableColumn("VARCHAR(255) NOT NULL") val name: String = "",
		@field:TableColumn("VARCHAR(255) NOT NULL") val longName: String = "",
		@field:TableColumn("INTEGER NOT NULL") val departmentId: Int = 0,
		@field:TableColumn("VARCHAR(255) NOT NULL") val startDate: String = "",
		@field:TableColumn("VARCHAR(255) NOT NULL") val endDate: String = "",
		@field:TableColumn("VARCHAR(255)") val foreColor: String? = "",
		@field:TableColumn("VARCHAR(255)") val backColor: String? = "",
		@field:TableColumn("BOOLEAN NOT NULL") val active: Boolean = false,
		@field:TableColumn("BOOLEAN NOT NULL") val displayable: Boolean = false
) : Comparable<String>, TableModel, java.io.Serializable {
	companion object {
		const val TABLE_NAME = TABLE_NAME_KLASSEN
	}

	override val tableName = TABLE_NAME
	override val elementId = id

	override fun generateValues(): ContentValues {
		val values = ContentValues()

		values.put("id", id)
		values.put("name", name)
		values.put("longName", longName)
		values.put("departmentId", departmentId)
		values.put("startDate", startDate)
		values.put("endDate", endDate)
		values.put("foreColor", foreColor)
		values.put("backColor", backColor)
		values.put("active", active)
		values.put("displayable", displayable)

		return values
	}

	override fun parseCursor(cursor: Cursor) = Klasse(
			cursor.getInt(cursor.getColumnIndex("id")),
			cursor.getString(cursor.getColumnIndex("name")),
			cursor.getString(cursor.getColumnIndex("longName")),
			cursor.getInt(cursor.getColumnIndex("departmentId")),
			cursor.getString(cursor.getColumnIndex("startDate")),
			cursor.getString(cursor.getColumnIndex("endDate")),
			cursor.getString(cursor.getColumnIndex("foreColor")),
			cursor.getString(cursor.getColumnIndex("backColor")),
			cursor.getInt(cursor.getColumnIndex("active")) != 0,
			cursor.getInt(cursor.getColumnIndex("displayable")) != 0
	)

	override fun compareTo(other: String) = if (
			name.contains(other, true)
			|| longName.contains(other, true)
	) 0 else name.compareTo(other)
}
