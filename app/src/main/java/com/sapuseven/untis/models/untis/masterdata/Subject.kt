package com.sapuseven.untis.models.untis.masterdata

import android.content.ContentValues
import android.database.Cursor
import com.sapuseven.untis.annotations.Table
import com.sapuseven.untis.annotations.TableColumn
import com.sapuseven.untis.data.databases.TABLE_NAME_SUBJECTS
import com.sapuseven.untis.interfaces.TableModel
import kotlinx.serialization.Serializable

@Serializable
@Table(TABLE_NAME_SUBJECTS)
data class Subject(
		@field:TableColumn("INTEGER NOT NULL") val id: Int = 0,
		@field:TableColumn("VARCHAR(255) NOT NULL") val name: String = "",
		@field:TableColumn("VARCHAR(255) NOT NULL") val longName: String = "",
		@field:TableColumn("VARCHAR(255) NOT NULL") val departmentIds: List<Int> = emptyList(),
		@field:TableColumn("VARCHAR(255)") val foreColor: String? = null,
		@field:TableColumn("VARCHAR(255)") val backColor: String? = null,
		@field:TableColumn("BOOLEAN NOT NULL") val active: Boolean = false,
		@field:TableColumn("BOOLEAN NOT NULL") val displayAllowed: Boolean = false
) : Comparable<String>, TableModel, java.io.Serializable {
	companion object {
		const val TABLE_NAME = TABLE_NAME_SUBJECTS
	}

	override val tableName = TABLE_NAME
	override val elementId = id

	override fun generateValues(): ContentValues {
		val values = ContentValues()

		values.put("id", id)
		values.put("name", name)
		values.put("longName", longName)
		values.put("departmentIds", ""/*getJSON().encodeToString<departmentIds)*/) // TODO: Fix this to work with Lists
		values.put("foreColor", foreColor)
		values.put("backColor", backColor)
		values.put("active", active)
		values.put("displayAllowed", displayAllowed)

		return values
	}

	override fun parseCursor(cursor: Cursor) = Subject(
			cursor.getInt(cursor.getColumnIndex("id")),
			cursor.getString(cursor.getColumnIndex("name")),
			cursor.getString(cursor.getColumnIndex("longName")),
			listOf(cursor.getInt(cursor.getColumnIndex("departmentIds"))), // TODO: Probably doesn't work, but this value is always empty anyway (see TODO-Item above)
			cursor.getString(cursor.getColumnIndex("foreColor")),
			cursor.getString(cursor.getColumnIndex("backColor")),
			cursor.getInt(cursor.getColumnIndex("active")) != 0,
			cursor.getInt(cursor.getColumnIndex("displayAllowed")) != 0
	)

	override fun compareTo(other: String) = if (
			name.contains(other, true)
			|| longName.contains(other, true)
	) 0 else name.compareTo(other)
}
