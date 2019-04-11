package com.sapuseven.untis.interfaces

import android.content.ContentValues
import android.database.Cursor

interface TableModel {
	fun generateValues(): ContentValues

	fun getTableName(): String

	fun parseCursor(cursor: Cursor): TableModel

	fun getElementId(): Int
}
