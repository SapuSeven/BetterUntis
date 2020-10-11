package com.sapuseven.untis.interfaces

import android.content.ContentValues
import android.database.Cursor

interface TableModel {
	val elementId: Int
	val tableName: String

	fun generateValues(): ContentValues

	fun parseCursor(cursor: Cursor): TableModel
}
