package com.sapuseven.untis.models.untis.masterdata

import android.content.ContentValues
import android.database.Cursor
import com.sapuseven.untis.annotations.Table
import com.sapuseven.untis.annotations.TableColumn
import com.sapuseven.untis.data.databases.TABLE_NAME_TEACHERS
import com.sapuseven.untis.interfaces.TableModel
import kotlinx.serialization.Serializable

@Serializable
@Table(TABLE_NAME_TEACHERS)
data class Teacher(
		@field:TableColumn("INTEGER NOT NULL") val id: Int = 0,
		@field:TableColumn("VARCHAR(255) NOT NULL") val name: String = "",
		@field:TableColumn("VARCHAR(255) NOT NULL") val firstName: String = "",
		@field:TableColumn("VARCHAR(255) NOT NULL") val lastName: String = "",
		@field:TableColumn("VARCHAR(255) NOT NULL") val departmentIds: List<Int> = emptyList(),
		@field:TableColumn("VARCHAR(255)") val foreColor: String? = null,
		@field:TableColumn("VARCHAR(255)") val backColor: String? = null,
		@field:TableColumn("VARCHAR(255)") val entryDate: String? = null,
		@field:TableColumn("VARCHAR(255)") val exitDate: String? = null,
		@field:TableColumn("BOOLEAN NOT NULL") val active: Boolean = false,
		@field:TableColumn("BOOLEAN NOT NULL") val displayAllowed: Boolean = false
) : Comparable<String>, TableModel, java.io.Serializable {
	companion object {
		const val TABLE_NAME = TABLE_NAME_TEACHERS
	}

	override val tableName = TABLE_NAME
	override val elementId = id

	override fun generateValues(): ContentValues {
		val values = ContentValues()

		values.put("id", id)
		values.put("name", name)
		values.put("firstName", firstName)
		values.put("lastName", lastName)
		values.put("departmentIds", ""/*getJSON().encodeToString<departmentIds)*/) // TODO: Fix this to work with Lists
		values.put("foreColor", foreColor)
		values.put("backColor", backColor)
		values.put("entryDate", entryDate)
		values.put("exitDate", exitDate)
		values.put("active", active)
		values.put("displayAllowed", displayAllowed)

		return values
	}

	override fun parseCursor(cursor: Cursor) = Teacher(
			cursor.getInt(cursor.getColumnIndex("id")),
			cursor.getString(cursor.getColumnIndex("name")),
			cursor.getString(cursor.getColumnIndex("firstName")),
			cursor.getString(cursor.getColumnIndex("lastName")),
			listOf(cursor.getInt(cursor.getColumnIndex("departmentIds"))), // TODO: Probably doesn't work, but this value is always empty anyway (see TODO-Item above)
			cursor.getString(cursor.getColumnIndex("foreColor")),
			cursor.getString(cursor.getColumnIndex("backColor")),
			cursor.getString(cursor.getColumnIndex("entryDate")),
			cursor.getString(cursor.getColumnIndex("exitDate")),
			cursor.getInt(cursor.getColumnIndex("active")) != 0,
			cursor.getInt(cursor.getColumnIndex("displayAllowed")) != 0
	)

	override fun compareTo(other: String) = if (
			name.contains(other, true)
			|| firstName.contains(other, true)
			|| lastName.contains(other, true)
	) 0 else name.compareTo(other)
}
